package com.campuslab.audit.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El microservicio recibe el mismo token que reenvia el BFF. Aqui el claim
 * "roles" va con los valores reales del App Registration ("Auditor",
 * "Admin"...) y pasa por el conversor de SecurityConfig, no por jwt().
 * Mismo conversor que bookings, report, rabbitmq-admin y kafka-admin.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RolesDeAzureTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void tokensConLaFormaDeAzure() {
        when(jwtDecoder.decode(anyString())).thenAnswer(inv -> {
            String rol = inv.<String>getArgument(0).substring("token-".length());
            return Jwt.withTokenValue("t").header("alg", "RS256").subject("sub-" + rol)
                    .claim("roles", List.of(rol))
                    .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(3600)).build();
        });
    }

    @ParameterizedTest(name = "rol {0} -> {1}")
    @CsvSource({"Admin, 200", "Auditor, 200", "Tecnico, 403", "Estudiante, 403"})
    void soloAdminYAuditorLeenLaAuditoria(String rol, int esperado) throws Exception {
        mockMvc.perform(get("/api/audit/resumen").header("Authorization", "Bearer token-" + rol))
                .andExpect(status().is(esperado));
        mockMvc.perform(get("/api/audit/solicitudes").header("Authorization", "Bearer token-" + rol))
                .andExpect(status().is(esperado));
    }
}
