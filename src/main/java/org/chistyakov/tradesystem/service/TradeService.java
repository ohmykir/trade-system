package org.chistyakov.tradesystem.service;

import lombok.AllArgsConstructor;
import org.chistyakov.tradesystem.dto.TradeFilter;
import org.chistyakov.tradesystem.dto.TradeResponse;
import org.chistyakov.tradesystem.entity.Trade;
import org.chistyakov.tradesystem.repository.TradeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final TradeSpecificationBuilder specificationBuilder;

    @Transactional(readOnly = true)
    public Page<TradeResponse> getTrades(TradeFilter filter, Pageable pageable) {
        Specification<Trade> specification = specificationBuilder.build(filter);
        Page<Trade> trades = tradeRepository.findAll(specification, pageable);

        return trades.map(TradeResponse::from);
    }
}
