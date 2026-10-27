package com.campuslab.audit.controller;

import com.campuslab.audit.repository.SolicitudInformacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SolicitudInformacionControllerTest {

    private static final String SOLICITUD = """
            {"categoria":"CANCELACIONES","asunto":"Cancelaciones de octubre",
             "descripcion":"Necesito el listado de cancelaciones con su motivo y quien las ejecuto.",
             "periodoDesde":"2026-10-01","periodoHasta":"2026-10-31"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SolicitudInformacionRepository repository;

    @MockBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    @Test
    void auditorCreaSolicitudYSoloVeLasSuyas() throws Exception {
        mockMvc.perform(post("/api/audit/solicitudes").with(auditor("aud-1", "Ana Auditora"))
                        .contentType(MediaType.APPLICATION_JSON).content(SOLICITUD))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.solicitanteNombre").value("Ana Auditora"));

        mockMvc.perform(get("/api/audit/solicitudes").with(auditor("aud-1", "Ana Auditora")))
                .andExpect(jsonPath("$.content.length()").value(1));
        mockMvc.perform(get("/api/audit/solicitudes").with(auditor("aud-2", "Otro Auditor")))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void formularioIncompletoDevuelve400ConDetallePorCampo() throws Exception {
        mockMvc.perform(post("/api/audit/solicitudes").with(auditor("aud-1", "Ana"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"asunto\":\"x\",\"descripcion\":\"corta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores", hasKey("categoria")))
                .andExpect(jsonPath("$.errores", hasKey("asunto")))
                .andExpect(jsonPath("$.errores", hasKey("descripcion")));
    }

    @Test
    void soloAdminRespondeYNoSePuedeResponderDosVeces() throws Exception {
        mockMvc.perform(post("/api/audit/solicitudes").with(auditor("aud-1", "Ana"))
                .contentType(MediaType.APPLICATION_JSON).content(SOLICITUD));
        Long id = repository.findAll().get(0).getId();
        String respuesta = "{\"estado\":\"RESPONDIDA\",\"respuesta\":\"Adjunto en el timeline, filtro CANCELADA.\"}";

        mockMvc.perform(put("/api/audit/solicitudes/{id}/respuesta", id).with(auditor("aud-1", "Ana"))
                        .contentType(MediaType.APPLICATION_JSON).content(respuesta))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/audit/solicitudes/{id}/respuesta", id).with(admin())
                        .contentType(MediaType.APPLICATION_JSON).content(respuesta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESPONDIDA"))
                .andExpect(jsonPath("$.respondidaPor").value("Admin Lab"));

        mockMvc.perform(put("/api/audit/solicitudes/{id}/respuesta", id).with(admin())
                        .contentType(MediaType.APPLICATION_JSON).content(respuesta))
                .andExpect(status().isConflict());
    }

    @Test
    void estudianteNoAccedeALasSolicitudes() throws Exception {
        mockMvc.perform(get("/api/audit/solicitudes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ESTUDIANTE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void resumenDevuelveConteosDelPeriodo() throws Exception {
        mockMvc.perform(get("/api/audit/resumen").with(auditor("aud-1", "Ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEventos").isNumber())
                .andExpect(jsonPath("$.eventosPorTipo").isMap());
    }

    private static JwtRequestPostProcessor auditor(String sub, String nombre) {
        return jwt().jwt(j -> j.subject(sub).claim("name", nombre))
                .authorities(new SimpleGrantedAuthority("ROLE_AUDITOR"));
    }

    private static JwtRequestPostProcessor admin() {
        return jwt().jwt(j -> j.subject("adm-1").claim("name", "Admin Lab"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
