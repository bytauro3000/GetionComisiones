package com.vendelote.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.ClienteDTO;
import com.vendelote.dto.SeguimientoClienteDTO;
import com.vendelote.entity.Cliente;
import com.vendelote.enums.EstadoSeguimiento;
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
    private final AsociadoService asociadoService;

    @Transactional(readOnly = true)
    public List<Cliente> listar(String q) {
        Integer idV = TenantContext.idVendedorRequerido();
        Integer idAsociado = TenantContext.esAsociado() ? TenantContext.idUsuarioActual() : null;
        List<Cliente> todos = clienteRepository.findByIdVendedorOrderByNombresAsc(idV).stream()
                .filter(c -> idAsociado == null
                        || idAsociado.equals(c.getIdVendedorAsociado()))
                .toList();
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
        Integer idAsociado = asociadoService.resolverAsociado(dto.getIdVendedorAsociado());
        if (clienteRepository.existsByIdVendedorAndIdVendedorAsociadoAndNumDocumento(
                idV, idAsociado, doc)) {
            throw new NegocioException("El cliente con documento " + doc + " ya está registrado");
        }

        Cliente cliente = new Cliente();
        cliente.setIdVendedor(idV);
        cliente.setIdVendedorAsociado(idAsociado);
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
        Integer idAsociado = cliente.getIdVendedorAsociado();
        if (TenantContext.esAsociado()) {
            idAsociado = TenantContext.idUsuarioActual();
        } else if (dto.getIdVendedorAsociado() != null) {
            idAsociado = asociadoService.resolverAsociado(dto.getIdVendedorAsociado());
        }
        if (clienteRepository.existsByIdVendedorAndIdVendedorAsociadoAndNumDocumentoAndIdNot(
                cliente.getIdVendedor(), idAsociado, doc, cliente.getId())) {
            throw new NegocioException("El cliente con documento " + doc + " ya está registrado");
        }

        cliente.setIdVendedorAsociado(idAsociado);
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
        Cliente cliente = clienteRepository.findByIdAndIdVendedor(
                        id, TenantContext.idVendedorRequerido())
                .orElseThrow(() -> new NegocioException("Cliente no encontrado"));
        if (TenantContext.esAsociado()
                && !TenantContext.idUsuarioActual().equals(cliente.getIdVendedorAsociado())) {
            throw new NegocioException("Cliente no encontrado");
        }
        return cliente;
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorDocumento(String numDocumento) {
        if (numDocumento == null || numDocumento.isBlank()) {
            return Optional.empty();
        }
        Integer idV = TenantContext.idVendedorRequerido();
        if (TenantContext.esAsociado()) {
            return clienteRepository.findByIdVendedorAndIdVendedorAsociadoAndNumDocumento(
                    idV, TenantContext.idUsuarioActual(), numDocumento.trim());
        }
        // El jefe puede reutilizar clientes de cualquier asociado del equipo.
        return clienteRepository.findByIdVendedorAndNumDocumento(idV, numDocumento.trim());
    }

    /** Actualiza el seguimiento comercial (estado, nota, próximo contacto). */
    @Transactional
    public Cliente actualizarSeguimiento(Integer id, SeguimientoClienteDTO dto) {
        Cliente cliente = buscar(id);
        cliente.setEstadoSeguimiento(estadoSeguimientoDesde(dto.getEstado()));
        cliente.setNotaSeguimiento(limpiar(dto.getNota()));
        cliente.setProximoContacto(dto.getProximoContacto());
        return clienteRepository.save(cliente);
    }

    /** Marca al prospecto como cliente (registro completo). */
    @Transactional
    public Cliente completar(Integer id) {
        Cliente cliente = buscar(id);
        cliente.setProspecto(false);
        return clienteRepository.save(cliente);
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
        if (dto.getProspecto() != null) {
            cliente.setProspecto(dto.getProspecto());
        }
        cliente.setEstadoSeguimiento(estadoSeguimientoDesde(dto.getEstadoSeguimiento()));
        cliente.setNotaSeguimiento(limpiar(dto.getNotaSeguimiento()));
        cliente.setProximoContacto(dto.getProximoContacto());
    }

    private EstadoSeguimiento estadoSeguimientoDesde(String valor) {
        if (valor == null || valor.isBlank()) {
            return EstadoSeguimiento.NUEVO;
        }
        try {
            return EstadoSeguimiento.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new NegocioException(
                    "Estado de seguimiento no válido: use NUEVO, CONTACTADO, INTERESADO, NO_INTERESADO o GANADO");
        }
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
