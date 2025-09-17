package io.github.ruslandidyk.betting.repository;

import io.github.ruslandidyk.betting.model.Bet;
import io.github.ruslandidyk.betting.model.BetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BetRepository extends JpaRepository<Bet, Long> {
    List<Bet> findByEventIdAndStatus(Long eventId, BetStatus status);
}