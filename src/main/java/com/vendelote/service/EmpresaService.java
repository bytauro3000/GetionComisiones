package com.vendelote.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vendelote.dto.CuentaBancariaDTO;
import com.vendelote.dto.EmpresaDTO;
import com.vendelote.entity.Empresa;
import com.vendelote.entity.VendedorEmpresa;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.EmpresaRepository;
import com.vendelote.repository.LoteRepository;
import com.vendelote.repository.VendedorEmpresaRepository;
import com.vendelote.repository.VentaRepository;
import com.vendelote.security.TenantContext;

import lombok.RequiredArgsConstructor;

/** Empresas del tenant (con aislamiento total: mismo RUC en dos tenants OK). */
@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final VendedorEmpresaRepository vinculoRepository;
    private final VentaRepository ventaRepository;
    private final LoteRepository loteRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<Empresa> listar() {
        List<Empresa> lista =
                empresaRepository.findByIdVendedorOrderByRazonSocialAsc(TenantContext.idVendedorRequerido());
        lista.forEach(this::parsearCuentas);
        return lista;
    }

    @Transactional
    public Empresa crear(EmpresaDTO dto) {
        Integer idV = TenantContext.idVendedorRequerido();

        if (empresaRepository.existsByIdVendedorAndRuc(idV, dto.getRuc())) {
            throw new NegocioException("El RUC ya está registrado");
        }

        Empresa empresa = new Empresa();
        empresa.setIdVendedor(idV);
        aplicar(empresa, dto);
        Empresa guardada = empresaRepository.save(empresa);

        // Vínculo vendedor ↔ empresa (para reportes y validaciones)
        if (!vinculoRepository.existsByIdVendedorAndIdEmpresa(idV, guardada.getId())) {
            VendedorEmpresa vinculo = new VendedorEmpresa();
            vinculo.setIdVendedor(idV);
            vinculo.setIdEmpresa(guardada.getId());
            vinculoRepository.save(vinculo);
        }
        parsearCuentas(guardada);
        return guardada;
    }

    @Transactional
    public Empresa editar(Integer id, EmpresaDTO dto) {
        Empresa empresa = buscar(id);
        if (empresaRepository.existsByIdVendedorAndRucAndIdNot(
                empresa.getIdVendedor(), dto.getRuc(), empresa.getId())) {
            throw new NegocioException("El RUC ya está registrado");
        }
        aplicar(empresa, dto);
        Empresa guardada = empresaRepository.save(empresa);
        parsearCuentas(guardada);
        return guardada;
    }

    @Transactional
    public void eliminar(Integer id) {
        Empresa empresa = buscar(id);
        Integer idV = empresa.getIdVendedor();

        if (ventaRepository.existsByIdVendedorAndIdEmpresa(idV, empresa.getId())) {
            throw new NegocioException("No se puede eliminar: la empresa tiene ventas registradas");
        }
        if (loteRepository.existsByIdVendedorAndIdEmpresa(idV, empresa.getId())) {
            throw new NegocioException("No se puede eliminar: la empresa tiene lotes registrados");
        }
        vinculoRepository.deleteByIdVendedorAndIdEmpresa(idV, empresa.getId());
        empresaRepository.delete(empresa);
    }

    /** Busca por id DENTRO del tenant; si no existe (o es de otro tenant) → 404-like. */
    @Transactional(readOnly = true)
    public Empresa buscar(Integer id) {
        return empresaRepository.findByIdAndIdVendedor(id, TenantContext.idVendedorRequerido())
                .orElseThrow(() -> new NegocioException("Empresa no encontrada"));
    }

    private void aplicar(Empresa empresa, EmpresaDTO dto) {
        empresa.setRuc(dto.getRuc().trim());
        empresa.setRazonSocial(dto.getRazonSocial().trim());
        empresa.setNombreComercial(dto.getNombreComercial());
        empresa.setDireccion(dto.getDireccion());
        empresa.setDistrito(dto.getDistrito());
        empresa.setProvincia(dto.getProvincia());
        empresa.setDepartamento(dto.getDepartamento());
        empresa.setTelefono(dto.getTelefono());
        empresa.setCorreo(dto.getCorreo());
        empresa.setCuentasBancarias(serializarCuentas(dto.getCuentas()));
    }

    /** Cuentas del DTO → JSON en la columna (null/[] → null). */
    private String serializarCuentas(List<CuentaBancariaDTO> cuentas) {
        if (cuentas == null || cuentas.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(cuentas);
        } catch (Exception e) {
            throw new NegocioException("No se pudieron guardar las cuentas bancarias");
        }
    }

    /** JSON de la columna → lista expuesta en {@code empresa.cuentas}. */
    private void parsearCuentas(Empresa empresa) {
        String json = empresa.getCuentasBancarias();
        if (json == null || json.isBlank()) {
            empresa.setCuentas(List.of());
            return;
        }
        try {
            empresa.setCuentas(objectMapper.readValue(json,
                    new TypeReference<List<CuentaBancariaDTO>>() { }));
        } catch (Exception e) {
            empresa.setCuentas(List.of());
        }
    }
}
