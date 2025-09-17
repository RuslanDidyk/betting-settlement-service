package io.github.ruslandidyk.betting.service;

import io.github.ruslandidyk.betting.event.BetSettlementEvent;
import io.github.ruslandidyk.betting.model.BetStatus;
import io.github.ruslandidyk.betting.repository.BetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BetSettlementService {

    private final BetRepository betRepository;

    @Transactional
    public void settleBet(BetSettlementEvent settlement) {
        log.info("Settling bet ID: {} for user: {}", settlement.getBetId(), settlement.getUserId());
        betRepository.findById(settlement.getBetId()).ifPresent(bet -> {
            if (settlement.isWinner()) {
                bet.setStatus(BetStatus.WON);
                log.info("Bet {} WON. Payout: {}", bet.getBetId(), settlement.getPayoutAmount());
                // In a real system, this would trigger a payment to the user
            } else {
                bet.setStatus(BetStatus.LOST);
                log.info("Bet {} LOST", bet.getBetId());
            }
            betRepository.save(bet);
            log.info("Bet {} settled successfully", bet.getBetId());
        });
    }
}
