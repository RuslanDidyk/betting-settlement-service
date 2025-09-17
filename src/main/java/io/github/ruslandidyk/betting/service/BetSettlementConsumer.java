package io.github.ruslandidyk.betting.service;

import io.github.ruslandidyk.betting.event.BetSettlementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "${app.rocketmq.bet-settlements-topic}",
        consumerGroup = "${app.rocketmq.consumer-group}"
)
public class BetSettlementConsumer implements RocketMQListener<BetSettlementEvent> {

    private final BetSettlementService betSettlementService;

    @Override
    public void onMessage(BetSettlementEvent event) {
        log.info("Received bet settlement message from RocketMQ: {}", event);
        try {
            betSettlementService.settleBet(event);
            log.info("Successfully processed bet settlement for bet ID: {}", event.getBetId());
        } catch (Exception e) {
            log.error("Error processing bet settlement message: {}", event, e);
        }
    }
}