package com.campuslab.audit.dto;

import com.campuslab.audit.model.SolicitudInformacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Cuerpo del formulario "Solicitar informacion" del panel de auditoria. */
public record SolicitudRequestDTO(
        @NotNull(message = "elige una categoria")
        SolicitudInformacion.Categoria categoria,

        @NotBlank(message = "el asunto es obligatorio")
        @Size(min = 5, max = 150, message = "el asunto debe tener entre 5 y 150 caracteres")
        String asunto,

        @NotBlank(message = "describe que informacion necesitas")
        @Size(min = 20, max = 2000, message = "la descripcion debe tener entre 20 y 2000 caracteres")
        String descripcion,

        @Positive(message = "el id de reserva debe ser positivo")
        Long reservaId,

        LocalDate periodoDesde,
        LocalDate periodoHasta
) {
}
