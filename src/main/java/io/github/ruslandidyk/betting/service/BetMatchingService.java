package io.github.ruslandidyk.betting.service;

import io.github.ruslandidyk.betting.event.BetSettlementEvent;
import io.github.ruslandidyk.betting.event.OutcomeEvent;
import io.github.ruslandidyk.betting.model.Bet;
import io.github.ruslandidyk.betting.model.BetStatus;
import io.github.ruslandidyk.betting.repository.BetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BetMatchingService {
    private static final BigDecimal PAYOUT_MULTIPLIER = new BigDecimal("2.0"); // Simple 2x payout for winners

    private final BetRepository betRepository;

    @Transactional
    public List<BetSettlementEvent> matchBets(OutcomeEvent outcomeEvent) {
        log.info("Matching bets for event: {} with winner: {}",
                outcomeEvent.getEventId(), outcomeEvent.getEventWinnerId());
        List<Bet> pendingBets = betRepository.findByEventIdAndStatus(outcomeEvent.getEventId(), BetStatus.PENDING);
        if (pendingBets.isEmpty()) {
            log.info("No pending bets found for event: {}", outcomeEvent.getEventId());
            return List.of();
        }
        log.info("Found {} pending bets for event: {}", pendingBets.size(), outcomeEvent.getEventId());
        return pendingBets.parallelStream()
                .map(bet -> createBetSettlement(bet, outcomeEvent))
                .collect(Collectors.toList());
    }

    private BetSettlementEvent createBetSettlement(Bet bet, OutcomeEvent outcomeEvent) {
        boolean isWinner = bet.getEventWinnerId().equals(outcomeEvent.getEventWinnerId());
        BigDecimal payoutAmount = isWinner ?
                bet.getBetAmount().multiply(PAYOUT_MULTIPLIER) : BigDecimal.ZERO;
        return BetSettlementEvent.builder()
                .betId(bet.getBetId())
                .userId(bet.getUserId())
                .eventId(bet.getEventId())
                .eventMarketId(bet.getEventMarketId())
                .eventWinnerId(bet.getEventWinnerId())
                .betAmount(bet.getBetAmount())
                .isWinner(isWinner)
                .payoutAmount(payoutAmount)
                .build();
    }
}
