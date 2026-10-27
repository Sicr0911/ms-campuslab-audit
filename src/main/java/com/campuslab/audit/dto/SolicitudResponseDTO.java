package com.campuslab.audit.dto;

import com.campuslab.audit.model.SolicitudInformacion;

import java.time.Instant;
import java.time.LocalDate;

public record SolicitudResponseDTO(
        Long id,
        String solicitanteNombre,
        SolicitudInformacion.Categoria categoria,
        String asunto,
        String descripcion,
        Long reservaId,
        LocalDate periodoDesde,
        LocalDate periodoHasta,
        SolicitudInformacion.Estado estado,
        String respuesta,
        String respondidaPor,
        Instant creadaEn,
        Instant respondidaEn
) {
    public static SolicitudResponseDTO fromEntity(SolicitudInformacion s) {
        return new SolicitudResponseDTO(
                s.getId(), s.getSolicitanteNombre(), s.getCategoria(), s.getAsunto(), s.getDescripcion(),
                s.getReservaId(),
                s.getPeriodoDesde() == null ? null : s.getPeriodoDesde().toLocalDate(),
                s.getPeriodoHasta() == null ? null : s.getPeriodoHasta().toLocalDate(),
                s.getEstado(),
                s.getRespuesta(), s.getRespondidaPor(), s.getCreadaEn(), s.getRespondidaEn());
    }
}
