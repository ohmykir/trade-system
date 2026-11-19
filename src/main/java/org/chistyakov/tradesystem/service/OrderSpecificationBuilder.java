package org.chistyakov.tradesystem.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.chistyakov.tradesystem.dto.OrderFilter;
import org.chistyakov.tradesystem.entity.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderSpecificationBuilder {

    public Specification<Order> build(OrderFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            addPredicate(predicates, cb, root.get("accountId"), filter.accountId());
            addPredicate(predicates, cb, root.get("seccode"), filter.seccode());
            addPredicate(predicates, cb, root.get("type"), filter.type());
            addPredicate(predicates, cb, root.get("status"), filter.status());

            if (filter.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.from()));
            }
            if (filter.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.to()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private <T> void addPredicate(List<Predicate> predicates, CriteriaBuilder cb, Path<T> field, T value) {
        if (value != null) {
            predicates.add(cb.equal(field, value));
        }
    }
}
