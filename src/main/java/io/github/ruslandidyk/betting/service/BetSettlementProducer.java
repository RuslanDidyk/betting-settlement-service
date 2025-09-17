package io.github.ruslandidyk.betting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ruslandidyk.betting.event.BetSettlementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BetSettlementProducer {

    @Value("${app.rocketmq.bet-settlements-topic}")
    private String topic;
    @Value("${app.rocketmq.send-timeout}")
    private int sendTimeout;

    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;

    public void produce(BetSettlementEvent event, String correlationId) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            Message<String> message = MessageBuilder
                    .withPayload(jsonPayload)
                    .setHeader("correlationId", correlationId)
                    .build();
            rocketMQTemplate.asyncSend(topic, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("Successfully sent bet settlement to RocketMQ - Correlation ID: {}, BetId: {}, SendStatus: {}",
                            correlationId, event.getBetId(), sendResult.getSendStatus());
                }

                @Override
                public void onException(Throwable throwable) {
                    log.error("Failed to send bet settlement to RocketMQ - Correlation ID: {}, BetId: {}",
                            correlationId, event.getBetId(), throwable);
                }
            }, sendTimeout);
            log.info("Sent bet settlement to RocketMQ for bet ID: {}", event.getBetId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize bet settlement: {}", event, e);
        } catch (Exception e) {
            log.error("Failed to send bet settlement to RocketMQ: {}", event, e);
        }
    }
}
