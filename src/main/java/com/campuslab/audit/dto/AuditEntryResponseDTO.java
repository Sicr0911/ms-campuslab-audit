package com.campuslab.audit.dto;

import com.campuslab.audit.model.AuditEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuditEntryResponseDTO {

    private Long id;
    private String eventId;
    private String eventType;
    private Long reservaId;
    private Long recursoId;
    private Long usuarioId;
    private String estado;
    private String traceId;
    private String correlationId;
    private Instant eventTimestamp;
    private Instant recibidoEn;

    public static AuditEntryResponseDTO fromEntity(AuditEntry entry) {
        return new AuditEntryResponseDTO(
                entry.getId(),
                entry.getEventId(),
                entry.getEventType(),
                entry.getReservaId(),
                entry.getRecursoId(),
                entry.getUsuarioId(),
                entry.getEstado(),
                entry.getTraceId(),
                entry.getCorrelationId(),
                entry.getEventTimestamp(),
                entry.getRecibidoEn()
        );
    }
}
