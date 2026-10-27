package com.campuslab.audit.dto;

import java.time.Instant;
import java.util.Map;

/** Cifras del periodo para la pestana "Resumen" del panel del auditor. */
public record ResumenAuditoriaDTO(
        Instant desde,
        Instant hasta,
        long totalEventos,
        long reservasInvolucradas,
        Map<String, Long> eventosPorTipo
) {
}
