package org.chistyakov.tradesystem.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.chistyakov.tradesystem.dto.TradeFilter;
import org.chistyakov.tradesystem.entity.Order;
import org.chistyakov.tradesystem.entity.Trade;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TradeSpecificationBuilder {

    public Specification<Trade> build(TradeFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            addPredicate(predicates, cb, root.get("seccode"), filter.seccode());

            if (filter.accountId() != null) {
                Join<Trade, Order> buyOrder = root.join("buyOrder");
                Join<Trade, Order> sellOrder = root.join("sellOrder");
                predicates.add(cb.or(
                        cb.equal(buyOrder.get("accountId"), filter.accountId()),
                        cb.equal(sellOrder.get("accountId"), filter.accountId())));
            }

            if (filter.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("executedAt"), filter.from()));
            }
            if (filter.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("executedAt"), filter.to()));
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
