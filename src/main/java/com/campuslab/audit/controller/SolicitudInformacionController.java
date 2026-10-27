package com.campuslab.audit.controller;

import com.campuslab.audit.dto.RespuestaSolicitudDTO;
import com.campuslab.audit.dto.SolicitudRequestDTO;
import com.campuslab.audit.dto.SolicitudResponseDTO;
import com.campuslab.audit.model.SolicitudInformacion;
import com.campuslab.audit.repository.SolicitudInformacionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.NoSuchElementException;

/**
 * Formulario "Solicitar informacion" del panel de auditoria. El Auditor crea
 * solicitudes y ve solo las suyas; el Admin ve todas y las responde.
 */
@RestController
@RequestMapping("/api/audit/solicitudes")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
public class SolicitudInformacionController {

    private final SolicitudInformacionRepository repository;

    @PostMapping
    public ResponseEntity<SolicitudResponseDTO> crear(@Valid @RequestBody SolicitudRequestDTO request,
                                                      Authentication authentication) {
        if (request.periodoDesde() != null && request.periodoHasta() != null
                && request.periodoHasta().isBefore(request.periodoDesde())) {
            throw new IllegalArgumentException("el periodo 'hasta' no puede ser anterior a 'desde'");
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();

        SolicitudInformacion solicitud = SolicitudInformacion.builder()
                .solicitanteSub(jwt.getSubject())
                .solicitanteNombre(nombreDe(jwt))
                .categoria(request.categoria())
                .asunto(request.asunto().trim())
                .descripcion(request.descripcion().trim())
                .reservaId(request.reservaId())
                .periodoDesde(request.periodoDesde() == null ? null : request.periodoDesde().atStartOfDay())
                .periodoHasta(request.periodoHasta() == null ? null : request.periodoHasta().atStartOfDay())
                .build();

        SolicitudInformacion guardada = repository.save(solicitud);
        log.info("Solicitud de informacion {} creada por {} ({})",
                guardada.getId(), guardada.getSolicitanteNombre(), guardada.getCategoria());
        return ResponseEntity.status(HttpStatus.CREATED).body(SolicitudResponseDTO.fromEntity(guardada));
    }

    /**
     * Admin: todas (filtrables por estado). Auditor: solo las que envio el.
     */
    @GetMapping
    public ResponseEntity<Page<SolicitudResponseDTO>> listar(
            @RequestParam(required = false) SolicitudInformacion.Estado estado,
            @PageableDefault(sort = "creadaEn", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        Page<SolicitudInformacion> pagina;
        if (esAdmin(authentication)) {
            pagina = estado == null ? repository.findAll(pageable) : repository.findByEstado(estado, pageable);
        } else {
            pagina = repository.findBySolicitanteSub(((Jwt) authentication.getPrincipal()).getSubject(), pageable);
        }
        return ResponseEntity.ok(pagina.map(SolicitudResponseDTO::fromEntity));
    }

    @PutMapping("/{id}/respuesta")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SolicitudResponseDTO> responder(@PathVariable Long id,
                                                          @Valid @RequestBody RespuestaSolicitudDTO request,
                                                          Authentication authentication) {
        if (request.estado() == SolicitudInformacion.Estado.PENDIENTE) {
            throw new IllegalArgumentException("la respuesta debe dejar la solicitud RESPONDIDA o RECHAZADA");
        }
        SolicitudInformacion solicitud = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe la solicitud " + id));
        if (solicitud.getEstado() != SolicitudInformacion.Estado.PENDIENTE) {
            throw new IllegalStateException("La solicitud " + id + " ya fue " + solicitud.getEstado());
        }

        solicitud.setEstado(request.estado());
        solicitud.setRespuesta(request.respuesta().trim());
        solicitud.setRespondidaPor(nombreDe((Jwt) authentication.getPrincipal()));
        solicitud.setRespondidaEn(Instant.now());
        return ResponseEntity.ok(SolicitudResponseDTO.fromEntity(repository.save(solicitud)));
    }

    private static boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    /** Azure AD v2 entrega "name" y "preferred_username"; si faltan, queda el sub. */
    private static String nombreDe(Jwt jwt) {
        for (String claim : new String[]{"name", "preferred_username", "email"}) {
            String valor = jwt.getClaimAsString(claim);
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }
        return jwt.getSubject();
    }
}
