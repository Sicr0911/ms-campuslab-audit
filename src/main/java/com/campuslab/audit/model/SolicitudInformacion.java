package com.campuslab.audit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Pedido de informacion que un Auditor envia desde el panel de auditoria
 * cuando los datos del timeline no le alcanzan. Lo responde (o rechaza) un
 * Admin; el Auditor ve el estado y la respuesta en la misma pantalla.
 */
@Entity
@Table(name = "SOLICITUDES_INFORMACION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudInformacion {

    public enum Categoria { RESERVAS, CANCELACIONES, APROBACIONES, INVENTARIO, ACCESOS, OTRO }

    public enum Estado { PENDIENTE, RESPONDIDA, RECHAZADA }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "solicitudes_info_seq_gen")
    @SequenceGenerator(name = "solicitudes_info_seq_gen", sequenceName = "SOLICITUDES_INFO_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "SOLICITANTE_SUB", nullable = false, length = 64)
    private String solicitanteSub;

    @Column(name = "SOLICITANTE_NOMBRE", length = 200)
    private String solicitanteNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORIA", nullable = false, length = 30)
    private Categoria categoria;

    @Column(name = "ASUNTO", nullable = false, length = 150)
    private String asunto;

    @Column(name = "DESCRIPCION", nullable = false, length = 2000)
    private String descripcion;

    @Column(name = "RESERVA_ID")
    private Long reservaId;

    // TIMESTAMP (inicio del dia) y no DATE: el driver de Oracle informa DATE
    // como TIMESTAMP y ddl-auto=validate lo rechazaria contra un LocalDate.
    @Column(name = "PERIODO_DESDE")
    private LocalDateTime periodoDesde;

    @Column(name = "PERIODO_HASTA")
    private LocalDateTime periodoHasta;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private Estado estado;

    @Column(name = "RESPUESTA", length = 2000)
    private String respuesta;

    @Column(name = "RESPONDIDA_POR", length = 200)
    private String respondidaPor;

    @Column(name = "CREADA_EN", nullable = false, updatable = false)
    private Instant creadaEn;

    @Column(name = "RESPONDIDA_EN")
    private Instant respondidaEn;

    @PrePersist
    protected void alCrear() {
        this.creadaEn = Instant.now();
        if (this.estado == null) {
            this.estado = Estado.PENDIENTE;
        }
    }
}
