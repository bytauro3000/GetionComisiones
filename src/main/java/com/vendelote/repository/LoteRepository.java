package com.vendelote.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vendelote.entity.Lote;
import com.vendelote.enums.EstadoLote;

public interface LoteRepository extends JpaRepository<Lote, Integer> {

    Optional<Lote> findByIdAndIdVendedor(Integer id, Integer idVendedor);

    List<Lote> findByIdVendedorOrderByFechaRegistroDesc(Integer idVendedor);

    List<Lote> findByIdVendedorAndIdEmpresaOrderByManzanaAscNumeroAsc(Integer idVendedor, Integer idEmpresa);

    boolean existsByIdVendedorAndIdEmpresaAndProyectoAndManzanaAndNumero(
            Integer idVendedor, Integer idEmpresa, String proyecto, String manzana, String numero);

    /** Igual que el anterior pero excluye un lote (para edición sin falso positivo). */
    boolean existsByIdVendedorAndIdEmpresaAndProyectoAndManzanaAndNumeroAndIdNot(
            Integer idVendedor, Integer idEmpresa, String proyecto, String manzana,
            String numero, Integer id);

    boolean existsByIdVendedorAndIdEmpresa(Integer idVendedor, Integer idEmpresa);

    boolean existsByIdProyecto(Integer idProyecto);

    /** Lotes en un estado cuya fecha de separación ya venció (job de expiración). */
    List<Lote> findByEstadoAndFechaVinceSeparacionBefore(EstadoLote estado, LocalDate fecha);

    /** Separaciones propias que vencen en una fecha exacta (job de avisos). */
    List<Lote> findByEstadoAndSeparacionAjenaFalseAndFechaVinceSeparacion(
            EstadoLote estado, LocalDate fecha);

    /** Sincroniza el texto de proyecto en los lotes al renombrar el proyecto. */
    @Modifying
    @Query("UPDATE Lote l SET l.proyecto = :nombre WHERE l.idProyecto = :idProyecto")
    void actualizarNombreProyecto(@Param("idProyecto") Integer idProyecto,
                                  @Param("nombre") String nombre);


    /** Borrado en cascada: elimina las filas del vendedor indicado. */
    void deleteByIdVendedor(Integer idVendedor);
}
