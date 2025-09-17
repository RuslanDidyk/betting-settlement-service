package io.github.ruslandidyk.betting.service;


import io.github.ruslandidyk.betting.dto.CreateBetRequest;
import io.github.ruslandidyk.betting.event.BetSettlementEvent;
import io.github.ruslandidyk.betting.event.OutcomeEvent;
import io.github.ruslandidyk.betting.model.Bet;
import io.github.ruslandidyk.betting.model.BetStatus;
import io.github.ruslandidyk.betting.repository.BetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BetService {

    private final BetRepository betRepository;

    @Transactional(readOnly = true)
    public Optional<Bet> getById(Long betId) {
        return betRepository.findById(betId);
    }

    @Transactional(readOnly = true)
    public Page<Bet> getAllBets(Pageable pageable) {
        return betRepository.findAll(pageable);
    }

    @Transactional
    public Bet create(CreateBetRequest request) {
        Bet bet = Bet.builder()
                .userId(request.getUserId())
                .eventId(request.getEventId())
                .eventMarketId(request.getEventMarketId())
                .eventWinnerId(request.getEventWinnerId())
                .betAmount(request.getBetAmount())
                .status(BetStatus.PENDING)
                .build();
        Bet savedBet = betRepository.save(bet);
        log.info("Created bet with ID: {}", savedBet.getBetId());
        return savedBet;
    }
}
