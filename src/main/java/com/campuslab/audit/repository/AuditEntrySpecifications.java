package com.campuslab.audit.repository;

import com.campuslab.audit.model.AuditEntry;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class AuditEntrySpecifications {

    private AuditEntrySpecifications() {
    }

    public static Specification<AuditEntry> conReservaId(Long reservaId) {
        return (root, query, cb) -> reservaId == null ? null : cb.equal(root.get("reservaId"), reservaId);
    }

    public static Specification<AuditEntry> conEventType(String eventType) {
        return (root, query, cb) -> eventType == null ? null : cb.equal(root.get("eventType"), eventType);
    }

    public static Specification<AuditEntry> conFechaDesde(Instant desde) {
        return (root, query, cb) -> desde == null ? null : cb.greaterThanOrEqualTo(root.get("eventTimestamp"), desde);
    }

    public static Specification<AuditEntry> conFechaHasta(Instant hasta) {
        return (root, query, cb) -> hasta == null ? null : cb.lessThanOrEqualTo(root.get("eventTimestamp"), hasta);
    }
}
