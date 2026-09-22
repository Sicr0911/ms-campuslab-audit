-- ============================================================
-- V1__create_audit_entries_table.sql
-- Tabla principal del microservicio ms-campuslab-audit
-- Motor: Oracle Database
-- ============================================================

CREATE SEQUENCE AUDIT_ENTRIES_SEQ
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE TABLE AUDIT_ENTRIES (
    ID                  NUMBER(19)      DEFAULT AUDIT_ENTRIES_SEQ.NEXTVAL NOT NULL,
    EVENT_ID            VARCHAR2(64)    NOT NULL,
    EVENT_TYPE          VARCHAR2(40)    NOT NULL,
    RESERVA_ID          NUMBER(19)      NOT NULL,
    RECURSO_ID          NUMBER(19),
    USUARIO_ID          NUMBER(19),
    ESTADO              VARCHAR2(20),
    TRACE_ID            VARCHAR2(64),
    CORRELATION_ID      VARCHAR2(64),
    PAYLOAD_JSON        CLOB,
    EVENT_TIMESTAMP     TIMESTAMP       NOT NULL,
    RECIBIDO_EN         TIMESTAMP       NOT NULL,
    CONSTRAINT PK_AUDIT_ENTRIES PRIMARY KEY (ID),
    -- Idempotencia: Kafka es at-least-once, esta constraint es la defensa
    -- final contra un mismo evento insertado dos veces.
    CONSTRAINT UQ_AUDIT_ENTRIES_EVENT_ID UNIQUE (EVENT_ID)
);

CREATE INDEX IX_AUDIT_ENTRIES_RESERVA_ID  ON AUDIT_ENTRIES (RESERVA_ID);
CREATE INDEX IX_AUDIT_ENTRIES_EVENT_TYPE  ON AUDIT_ENTRIES (EVENT_TYPE);
CREATE INDEX IX_AUDIT_ENTRIES_TIMESTAMP   ON AUDIT_ENTRIES (EVENT_TIMESTAMP);

COMMENT ON TABLE AUDIT_ENTRIES IS 'Timeline de auditoria: eventos del ciclo de vida de una reserva, consumidos de Kafka (bookings.events)';
COMMENT ON COLUMN AUDIT_ENTRIES.EVENT_ID IS 'Id del evento tal como lo genero bookings; unico, protege contra reprocesamiento por Kafka at-least-once';
