package com.campuslab.audit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Una entrada del timeline de auditoria: un evento del ciclo de vida de una
 * reserva (bookings.events), tal como fue publicado por ms-campuslab-bookings.
 * EVENT_ID tiene una constraint UNIQUE: Kafka es at-least-once, asi que el
 * mismo evento puede llegar duplicado tras un rebalanceo/reintento; el
 * listener detecta el duplicado por la violacion de esa constraint y lo
 * descarta sin volver a insertarlo.
 */
@Entity
@Table(name = "AUDIT_ENTRIES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_entries_seq_gen")
    @SequenceGenerator(name = "audit_entries_seq_gen", sequenceName = "AUDIT_ENTRIES_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "EVENT_ID", nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(name = "EVENT_TYPE", nullable = false, length = 40)
    private String eventType;

    @Column(name = "RESERVA_ID", nullable = false)
    private Long reservaId;

    @Column(name = "RECURSO_ID")
    private Long recursoId;

    @Column(name = "USUARIO_ID")
    private Long usuarioId;

    @Column(name = "ESTADO", length = 20)
    private String estado;

    @Column(name = "TRACE_ID", length = 64)
    private String traceId;

    @Column(name = "CORRELATION_ID", length = 64)
    private String correlationId;

    @Lob
    @Column(name = "PAYLOAD_JSON")
    private String payloadJson;

    @Column(name = "EVENT_TIMESTAMP", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "RECIBIDO_EN", nullable = false, updatable = false)
    private Instant recibidoEn;

    @PrePersist
    protected void alCrear() {
        this.recibidoEn = Instant.now();
    }
}
