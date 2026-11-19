package org.chistyakov.tradesystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chistyakov.tradesystem.dto.OrderFilter;
import org.chistyakov.tradesystem.dto.OrderRequest;
import org.chistyakov.tradesystem.dto.OrderResponse;
import org.chistyakov.tradesystem.dto.UpdateOrderRequest;
import org.chistyakov.tradesystem.service.OrderQueryService;
import org.chistyakov.tradesystem.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderQueryService orderQueryService;

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(
            OrderFilter filter,
            @PageableDefault(size = 20, sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(orderQueryService.getOrders(filter, pageable));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody OrderRequest request) {
        OrderService.Result result = orderService.createOrder(request, idempotencyKey);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(status).body(OrderResponse.from(result.order()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody UpdateOrderRequest request,
            @PathVariable Long id) {
        OrderService.Result result = orderService.updateOrder(request, idempotencyKey, id);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(status).body(OrderResponse.from(result.order()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);

        return ResponseEntity.noContent().build();
    }
}
