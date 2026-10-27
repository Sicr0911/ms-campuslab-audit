package com.campuslab.audit.dto;

import com.campuslab.audit.model.SolicitudInformacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Respuesta de un Admin a una solicitud de informacion: RESPONDIDA o RECHAZADA. */
public record RespuestaSolicitudDTO(
        @NotNull(message = "indica si la solicitud queda RESPONDIDA o RECHAZADA")
        SolicitudInformacion.Estado estado,

        @NotBlank(message = "la respuesta es obligatoria")
        @Size(max = 2000, message = "la respuesta no puede superar 2000 caracteres")
        String respuesta
) {
}
