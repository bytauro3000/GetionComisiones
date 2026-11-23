package com.vendelote.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vendelote.entity.Auditoria;
import com.vendelote.enums.AccionAuditoria;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    Page<Auditoria> findAllByOrderByFechaHoraDesc(Pageable pageable);

    Page<Auditoria> findByAccionOrderByFechaHoraDesc(AccionAuditoria accion, Pageable pageable);

    @Query("SELECT a FROM Auditoria a WHERE LOWER(a.usuarioEmail) LIKE LOWER(CONCAT('%', :buscar, '%')) ORDER BY a.fechaHora DESC")
    Page<Auditoria> buscarPorUsuario(@Param("buscar") String buscar, Pageable pageable);

    @Query("SELECT a FROM Auditoria a WHERE a.accion = :accion AND LOWER(a.usuarioEmail) LIKE LOWER(CONCAT('%', :buscar, '%')) ORDER BY a.fechaHora DESC")
    Page<Auditoria> buscarPorAccionYUsuario(@Param("accion") AccionAuditoria accion,
                                            @Param("buscar") String buscar,
                                            Pageable pageable);
}
