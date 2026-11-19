package org.chistyakov.tradesystem.repository;

import org.chistyakov.tradesystem.dto.QuoteLevel;
import org.chistyakov.tradesystem.entity.Order;
import org.chistyakov.tradesystem.entity.OrderStatus;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    @Query("SELECT o.seccode FROM Order o WHERE o.id = :id")
    Optional<String> findSeccodeById(Long id);

    @Query("SELECT o FROM Order o WHERE o.accountId = :accountId AND o.idempotencyKey = :idempotencyKey")
    Optional<Order> findByAccountIdAndIdempotencyKey(Long accountId, String idempotencyKey);

    @Query("""
            SELECT o
            FROM Order o
            WHERE o.seccode = :seccode AND
                  o.price >= :price AND
                  o.status IN :statuses AND
                  o.type = org.chistyakov.tradesystem.entity.OrderType.BUY
            ORDER BY o.price DESC, o.createdAt ASC, o.id ASC
            """)
    List<Order> findBestBuys(String seccode, BigDecimal price, List<OrderStatus> statuses, Limit batchSize);

    @Query("""
            SELECT o
            FROM Order o
            WHERE o.seccode = :seccode AND
                  o.price <= :price AND
                  o.status IN :statuses AND
                  o.type = org.chistyakov.tradesystem.entity.OrderType.SELL
            ORDER BY o.price ASC, o.createdAt ASC, o.id ASC
            """)
    List<Order> findBestSells(String seccode, BigDecimal price, List<OrderStatus> statuses, Limit batchSize);


    @Query("""
            SELECT new org.chistyakov.tradesystem.dto.QuoteLevel(o.price, SUM(o.quantity - o.filledQuantity), COUNT(o))
            FROM Order o
            WHERE o.seccode = :seccode AND
                  o.type = org.chistyakov.tradesystem.entity.OrderType.BUY AND
                  o.status IN :statuses
            GROUP BY o.price
            ORDER BY o.price DESC
""")
    List<QuoteLevel> findBidLevels(String seccode, List<OrderStatus> statuses, Limit depth);

    @Query("""
            SELECT new org.chistyakov.tradesystem.dto.QuoteLevel(o.price, SUM(o.quantity - o.filledQuantity), COUNT(o))
            FROM Order o
            WHERE o.seccode = :seccode AND
                  o.type = org.chistyakov.tradesystem.entity.OrderType.SELL AND
                  o.status IN :statuses
            GROUP BY o.price
            ORDER BY o.price ASC
""")
    List<QuoteLevel> findAskLevels(String seccode, List<OrderStatus> statuses, Limit depth);
}
