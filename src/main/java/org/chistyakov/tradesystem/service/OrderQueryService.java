package org.chistyakov.tradesystem.service;

import lombok.RequiredArgsConstructor;
import org.chistyakov.tradesystem.dto.OrderFilter;
import org.chistyakov.tradesystem.dto.OrderResponse;
import org.chistyakov.tradesystem.entity.Order;
import org.chistyakov.tradesystem.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderSpecificationBuilder specificationBuilder;

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(OrderFilter filter, Pageable pageable) {
        Specification<Order> specification = specificationBuilder.build(filter);
        Page<Order> orders = orderRepository.findAll(specification, pageable);

        return orders.map(OrderResponse::from);
    }
}
