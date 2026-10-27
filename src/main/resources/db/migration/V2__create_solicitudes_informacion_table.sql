-- ============================================================
-- V2__create_solicitudes_informacion_table.sql
-- Solicitudes de informacion que el Auditor envia desde la web cuando
-- le faltan datos en el panel de auditoria; las responde un Admin.
-- Motor: Oracle Database
-- ============================================================

CREATE SEQUENCE SOLICITUDES_INFO_SEQ
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE TABLE SOLICITUDES_INFORMACION (
    ID                  NUMBER(19)      DEFAULT SOLICITUDES_INFO_SEQ.NEXTVAL NOT NULL,
    SOLICITANTE_SUB     VARCHAR2(64)    NOT NULL,
    SOLICITANTE_NOMBRE  VARCHAR2(200),
    CATEGORIA           VARCHAR2(30)    NOT NULL,
    ASUNTO              VARCHAR2(150)   NOT NULL,
    DESCRIPCION         VARCHAR2(2000)  NOT NULL,
    RESERVA_ID          NUMBER(19),
    PERIODO_DESDE       TIMESTAMP,
    PERIODO_HASTA       TIMESTAMP,
    ESTADO              VARCHAR2(20)    NOT NULL,
    RESPUESTA           VARCHAR2(2000),
    RESPONDIDA_POR      VARCHAR2(200),
    CREADA_EN           TIMESTAMP       NOT NULL,
    RESPONDIDA_EN       TIMESTAMP,
    CONSTRAINT PK_SOLICITUDES_INFORMACION PRIMARY KEY (ID),
    CONSTRAINT CK_SOLICITUDES_INFO_ESTADO CHECK (ESTADO IN ('PENDIENTE', 'RESPONDIDA', 'RECHAZADA'))
);

CREATE INDEX IX_SOLICITUDES_INFO_SUB    ON SOLICITUDES_INFORMACION (SOLICITANTE_SUB);
CREATE INDEX IX_SOLICITUDES_INFO_ESTADO ON SOLICITUDES_INFORMACION (ESTADO);

COMMENT ON TABLE SOLICITUDES_INFORMACION IS 'Pedidos de informacion del Auditor (formulario del panel de auditoria) y su respuesta';
COMMENT ON COLUMN SOLICITUDES_INFORMACION.SOLICITANTE_SUB IS 'Claim sub del JWT de Azure AD de quien envio la solicitud';
