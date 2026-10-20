package com.campuslab.audit.messaging;

import com.campuslab.audit.model.AuditEntry;
import com.campuslab.audit.repository.AuditEntryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consume bookings.events (fuente de verdad del ciclo de vida de una
 * reserva) y persiste cada evento como una fila del timeline de auditoria.
 * Un error de deserializacion o de negocio propaga la excepcion: el
 * DefaultErrorHandler configurado en KafkaConsumerConfig se encarga de
 * reintentar y, si sigue fallando, mandar el mensaje a la dead-letter topic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingsEventListener {

    private final AuditEntryRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "bookings.events", groupId = "${spring.kafka.consumer.group-id}",
            autoStartup = "${campuslab.kafka.listener.auto-startup:true}")
    @Transactional
    public void onMessage(String message) {
        EventEnvelope envelope;
        try {
            envelope = objectMapper.readValue(message, EventEnvelope.class);
        } catch (Exception ex) {
            log.error("Evento de bookings.events con formato invalido, se descarta a la DLT: {}", message, ex);
            throw new IllegalArgumentException("Payload invalido en bookings.events", ex);
        }

        if (repository.existsByEventId(envelope.eventId())) {
            log.info("Evento {} ya procesado antes, se descarta (idempotencia).", envelope.eventId());
            return;
        }

        JsonNode payload = envelope.payload();
        AuditEntry entry = AuditEntry.builder()
                .eventId(envelope.eventId())
                .eventType(envelope.type())
                .reservaId(leerLong(payload, "reservaId"))
                .recursoId(leerLong(payload, "recursoId"))
                .usuarioId(leerLong(payload, "usuarioSolicitanteId"))
                .estado(leerTexto(payload, "estado"))
                .traceId(envelope.traceId())
                .correlationId(envelope.correlationId())
                .payloadJson(payload != null ? payload.toString() : null)
                .eventTimestamp(envelope.timestamp())
                .build();

        try {
            repository.save(entry);
            log.info("Evento {} ({}) guardado en el timeline de auditoria, reservaId={}",
                    envelope.type(), envelope.eventId(), entry.getReservaId());
        } catch (DataIntegrityViolationException ex) {
            // Carrera con otra instancia/reintento que ya inserto este eventId primero: no es un error real.
            log.info("Evento {} ya insertado por otra instancia (constraint EVENT_ID), se descarta.", envelope.eventId());
        }
    }

    private Long leerLong(JsonNode payload, String campo) {
        if (payload == null || !payload.hasNonNull(campo)) {
            return null;
        }
        return payload.get(campo).asLong();
    }

    private String leerTexto(JsonNode payload, String campo) {
        if (payload == null || !payload.hasNonNull(campo)) {
            return null;
        }
        return payload.get(campo).asText();
    }
}
