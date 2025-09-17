package io.github.ruslandidyk.betting.controller;

import io.github.ruslandidyk.betting.dto.CreateBetRequest;
import io.github.ruslandidyk.betting.model.Bet;
import io.github.ruslandidyk.betting.service.BetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/bets")
@RequiredArgsConstructor
public class BetController {

    private final BetService betService;

    @GetMapping
    public ResponseEntity<Page<Bet>> getAllBets(Pageable pageable) {
        log.info("Getting all bets with pagination: {}", pageable);
        return ResponseEntity.ok(betService.getAllBets(pageable));
    }

    @PostMapping
    public ResponseEntity<Bet> createBet(@Valid @RequestBody CreateBetRequest request) {
        log.info("Creating new bet: {}", request);
        Bet savedBet = betService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBet);
    }

    // Add more endpoints for bet management like cancel, etc.
}
