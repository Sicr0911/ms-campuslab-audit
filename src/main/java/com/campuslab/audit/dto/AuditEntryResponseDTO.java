package com.campuslab.audit.dto;

import com.campuslab.audit.model.AuditEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuditEntryResponseDTO {

    private static final ObjectMapper MAPPER = new ObjectMapper();

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
    /**
     * Payload original del evento (aprobadoPor, fechaAprobacion,
     * motivoCancelacion, fechas de la reserva...): es lo que el Auditor
     * necesita para justificar cada transicion. Vacio si no se pudo leer.
     */
    private Map<String, Object> detalle;

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
                entry.getRecibidoEn(),
                leerDetalle(entry.getPayloadJson())
        );
    }

    private static Map<String, Object> leerDetalle(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return Map.of();
        }
        try {
            return MAPPER.readValue(payloadJson, new TypeReference<Map<String, Object>>() { });
        } catch (Exception ex) {
            return Map.of();
        }
    }
}
