package com.vendelote.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.ClienteDTO;
import com.vendelote.entity.Cliente;
import com.vendelote.enums.TipoDocumento;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.ClienteRepository;
import com.vendelote.repository.VentaClienteRepository;
import com.vendelote.security.TenantContext;

import lombok.RequiredArgsConstructor;

/**
 * Clientes del tenant. El autocompletado DNI/RUC lo hace el frontend llamando
 * a /api/decolecta/*; aquí solo validamos y persistimos.
 */
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VentaClienteRepository ventaClienteRepository;

    @Transactional(readOnly = true)
    public List<Cliente> listar(String q) {
        List<Cliente> todos = clienteRepository.findByIdVendedorOrderByNombresAsc(TenantContext.idVendedorRequerido());
        if (q == null || q.isBlank()) {
            return todos;
        }
        String filtro = q.trim().toLowerCase(Locale.ROOT);
        return todos.stream()
                .filter(c -> (c.getNumDocumento() != null && c.getNumDocumento().contains(filtro))
                        || (c.getNombres() != null && c.getNombres().toLowerCase(Locale.ROOT).contains(filtro))
                        || (c.getApellidos() != null && c.getApellidos().toLowerCase(Locale.ROOT).contains(filtro)))
                .toList();
    }

    @Transactional
    public Cliente crear(ClienteDTO dto) {
        Integer idV = TenantContext.idVendedorRequerido();
        TipoDocumento tipo = tipoDesde(dto.getTipoDocumento());
        String doc = dto.getNumDocumento().trim();

        validarDocumento(tipo, doc);
        if (clienteRepository.existsByIdVendedorAndNumDocumento(idV, doc)) {
            throw new NegocioException("El cliente con documento " + doc + " ya está registrado");
        }

        Cliente cliente = new Cliente();
        cliente.setIdVendedor(idV);
        cliente.setTipoDocumento(tipo);
        aplicar(cliente, dto, doc);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente editar(Integer id, ClienteDTO dto) {
        Cliente cliente = buscar(id);
        TipoDocumento tipo = tipoDesde(dto.getTipoDocumento());
        String doc = dto.getNumDocumento().trim();

        validarDocumento(tipo, doc);
        if (clienteRepository.existsByIdVendedorAndNumDocumentoAndIdNot(
                cliente.getIdVendedor(), doc, cliente.getId())) {
            throw new NegocioException("El cliente con documento " + doc + " ya está registrado");
        }

        cliente.setTipoDocumento(tipo);
        aplicar(cliente, dto, doc);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public void eliminar(Integer id) {
        Cliente cliente = buscar(id);
        if (ventaClienteRepository.existsByVenta_IdVendedorAndIdCliente(cliente.getIdVendedor(), cliente.getId())) {
            throw new NegocioException("No se puede eliminar: el cliente tiene ventas registradas");
        }
        clienteRepository.delete(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente buscar(Integer id) {
        return clienteRepository.findByIdAndIdVendedor(id, TenantContext.idVendedorRequerido())
                .orElseThrow(() -> new NegocioException("Cliente no encontrado"));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private TipoDocumento tipoDesde(String valor) {
        if (valor == null || valor.isBlank()) {
            return TipoDocumento.DNI;
        }
        try {
            return TipoDocumento.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new NegocioException("Tipo de documento no válido: use DNI, CE, RUC u OTRO");
        }
    }

    private void validarDocumento(TipoDocumento tipo, String doc) {
        switch (tipo) {
            case DNI -> {
                if (!doc.matches("\\d{8}")) {
                    throw new NegocioException("El DNI debe tener 8 dígitos");
                }
            }
            case RUC -> {
                if (!doc.matches("\\d{11}")) {
                    throw new NegocioException("El RUC debe tener 11 dígitos");
                }
            }
            default -> {
                if (doc.isBlank()) {
                    throw new NegocioException("El número de documento es obligatorio");
                }
            }
        }
    }

    private void aplicar(Cliente cliente, ClienteDTO dto, String doc) {
        cliente.setNumDocumento(doc);
        cliente.setNombres(dto.getNombres().trim());
        cliente.setApellidos(dto.getApellidos());
        cliente.setTelefono(dto.getTelefono());
        cliente.setCorreo(dto.getCorreo());
        cliente.setDireccion(dto.getDireccion());
    }
}
