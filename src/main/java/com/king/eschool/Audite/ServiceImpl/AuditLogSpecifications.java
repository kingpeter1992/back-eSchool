package com.king.eschool.Audite.ServiceImpl;

import org.springframework.data.jpa.domain.Specification;

import com.king.eschool.Audite.models.AuditLog;

import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuditLogSpecifications {

    public static Specification<AuditLog> filterLogs(UUID schoolId, UUID campusId, String actionType, Instant fromDate, Instant toDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (schoolId != null) predicates.add(cb.equal(root.get("schoolId"), schoolId));
            if (campusId != null) predicates.add(cb.equal(root.get("campusId"), campusId));
            if (actionType != null) predicates.add(cb.equal(root.get("actionType"), actionType));
            if (fromDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), fromDate));
            if (toDate != null) predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), toDate));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}