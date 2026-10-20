package com.campuslab.audit;

import com.campuslab.audit.messaging.BookingsEventListener;
import com.campuslab.audit.repository.AuditEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Carga el contexto completo con el perfil "test" (H2 en memoria, sin
 * Kafka) y ejercita el listener directamente, sin pasar por el broker.
 */
@SpringBootTest
@ActiveProfiles("test")
class MsCampuslabAuditApplicationTests {

    private static final String EVENTO = """
            {"type":"RESERVA_APROBADA","eventId":"evt-audit-1","timestamp":"2026-10-19T10:00:00Z",
             "traceId":"t-1","correlationId":"reserva-5",
             "payload":{"reservaId":5,"recursoId":10,"usuarioSolicitanteId":20,"estado":"APROBADA"}}
            """;

    @Autowired
    private BookingsEventListener listener;

    @Autowired
    private AuditEntryRepository repository;

    @Test
    void contextLoads() {
        assertThat(listener).isNotNull();
    }

    @Test
    void eventoRepetido_seGuardaUnaSolaVez() {
        listener.onMessage(EVENTO);
        listener.onMessage(EVENTO);

        assertThat(repository.existsByEventId("evt-audit-1")).isTrue();
        assertThat(repository.findAll())
                .filteredOn(e -> "evt-audit-1".equals(e.getEventId()))
                .hasSize(1)
                .first()
                .satisfies(e -> {
                    assertThat(e.getReservaId()).isEqualTo(5L);
                    assertThat(e.getEstado()).isEqualTo("APROBADA");
                });
    }

    @Test
    void eventoConFormatoInvalido_lanzaExcepcionParaQueVayaALaDlt() {
        assertThatThrownBy(() -> listener.onMessage("no es json"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
