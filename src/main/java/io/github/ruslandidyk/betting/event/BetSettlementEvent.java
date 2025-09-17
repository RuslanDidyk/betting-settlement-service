package io.github.ruslandidyk.betting.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetSettlementEvent {
    private Long betId;
    private Long userId;
    private Long eventId;
    private Long eventMarketId;
    private Long eventWinnerId;
    private BigDecimal betAmount;
    private boolean isWinner;
    private BigDecimal payoutAmount;
}