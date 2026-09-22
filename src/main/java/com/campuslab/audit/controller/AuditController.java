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

import java.time.Instant;

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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
            @PageableDefault
            Pageable pageable) {

        Specification<AuditEntry> spec = Specification
                .where(AuditEntrySpecifications.conEventType(eventType))
                .and(AuditEntrySpecifications.conFechaDesde(desde))
                .and(AuditEntrySpecifications.conFechaHasta(hasta));

        Page<AuditEntryResponseDTO> resultado = repository.findAll(spec, pageable)
                .map(AuditEntryResponseDTO::fromEntity);
        return ResponseEntity.ok(resultado);
    }
}
