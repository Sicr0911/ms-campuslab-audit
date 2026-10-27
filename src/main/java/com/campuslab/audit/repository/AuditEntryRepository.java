package com.campuslab.audit.repository;

import com.campuslab.audit.model.AuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long>, JpaSpecificationExecutor<AuditEntry> {

    boolean existsByEventId(String eventId);

    /** Filas [eventType, cantidad] del periodo, para el resumen del panel del auditor. */
    @Query("select e.eventType, count(e) from AuditEntry e "
            + "where e.eventTimestamp >= :desde and e.eventTimestamp <= :hasta group by e.eventType")
    List<Object[]> contarPorTipo(@Param("desde") Instant desde, @Param("hasta") Instant hasta);

    @Query("select count(distinct e.reservaId) from AuditEntry e "
            + "where e.eventTimestamp >= :desde and e.eventTimestamp <= :hasta")
    long contarReservasDistintas(@Param("desde") Instant desde, @Param("hasta") Instant hasta);
}
