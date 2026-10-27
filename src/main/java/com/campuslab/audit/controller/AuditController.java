package com.campuslab.audit.controller;

import com.campuslab.audit.dto.AuditEntryResponseDTO;
import com.campuslab.audit.model.AuditEntry;
import com.campuslab.audit.repository.AuditEntryRepository;
import com.campuslab.audit.repository.AuditEntrySpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.campuslab.audit.dto.ResumenAuditoriaDTO;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.TreeMap;

/**
 * API de solo lectura del timeline de auditoria. Restringida a ADMIN/AUDITOR:
 * son los unicos roles del caso con responsabilidad de trazabilidad/control.
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
public class AuditController {

    private final AuditEntryRepository repository;

    /**
     * GET /api/audit/reservas/{reservaId}
     * Timeline completo (todas las transiciones) de una reserva puntual, en orden cronologico.
     */
    @GetMapping("/reservas/{reservaId}")
    public ResponseEntity<Page<AuditEntryResponseDTO>> timelineDeReserva(
            @PathVariable Long reservaId,
            @PageableDefault
            Pageable pageable) {

        Specification<AuditEntry> spec =
                Specification.where(AuditEntrySpecifications.conReservaId(reservaId));

        Page<AuditEntryResponseDTO> resultado = repository.findAll(spec, pageable)
                .map(AuditEntryResponseDTO::fromEntity);
        return ResponseEntity.ok(resultado);
    }

    /**
     * GET /api/audit
     * Listado general con filtros opcionales (tipo de evento, rango de fechas).
     */
    @GetMapping
    public ResponseEntity<Page<AuditEntryResponseDTO>> listar(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) Long reservaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
            @PageableDefault
            Pageable pageable) {

        Specification<AuditEntry> spec = Specification
                .where(AuditEntrySpecifications.conEventType(eventType))
                .and(AuditEntrySpecifications.conReservaId(reservaId))
                .and(AuditEntrySpecifications.conFechaDesde(desde))
                .and(AuditEntrySpecifications.conFechaHasta(hasta));

        Page<AuditEntryResponseDTO> resultado = repository.findAll(spec, pageable)
                .map(AuditEntryResponseDTO::fromEntity);
        return ResponseEntity.ok(resultado);
    }

    /**
     * GET /api/audit/resumen?desde=&hasta=
     * Cantidad de eventos por tipo y reservas involucradas en el periodo
     * (por defecto, los ultimos 30 dias).
     */
    @GetMapping("/resumen")
    public ResponseEntity<ResumenAuditoriaDTO> resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta) {

        Instant fin = hasta != null ? hasta : Instant.now();
        Instant inicio = desde != null ? desde : fin.minus(30, ChronoUnit.DAYS);

        Map<String, Long> porTipo = new TreeMap<>();
        for (Object[] fila : repository.contarPorTipo(inicio, fin)) {
            porTipo.put((String) fila[0], ((Number) fila[1]).longValue());
        }
        long total = porTipo.values().stream().mapToLong(Long::longValue).sum();

        return ResponseEntity.ok(new ResumenAuditoriaDTO(
                inicio, fin, total, repository.contarReservasDistintas(inicio, fin), porTipo));
    }
}
