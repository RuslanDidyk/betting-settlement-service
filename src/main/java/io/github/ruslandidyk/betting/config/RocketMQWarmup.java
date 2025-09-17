package io.github.ruslandidyk.betting.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ruslandidyk.betting.event.BetSettlementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

//This class is specifically needed to warm up the RocketMQ broker before the application starts for demo purposes.
//because of Consumer Group Registration Lag (10-20 seconds)
//RocketMQ needs time to register the consumer group with all brokers
//During this time, messages are sent but not tracked
@Slf4j
@Component
@RequiredArgsConstructor
public class RocketMQWarmup {

    @Value("${app.rocketmq.bet-settlements-topic}")
    private String topic;
    @Value("${app.rocketmq.send-timeout}")
    private int sendTimeout;

    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            //wait for partitions to be assigned
            Thread.sleep(10000);
            String jsonPayload = objectMapper.writeValueAsString(BetSettlementEvent.builder()
                    .userId(0L).eventId(0L).eventMarketId(0L).eventWinnerId(0L).betId(0L)
                    .build());
            Message<String> message = MessageBuilder
                    .withPayload(jsonPayload)
                    .build();
            rocketMQTemplate.asyncSend(topic, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("Successfully sent warmup message to RocketMQ - SendStatus: {}", sendResult.getSendStatus());
                }

                @Override
                public void onException(Throwable throwable) {
                    log.error("Failed to send warmup to RocketMQ", throwable);
                }
            }, sendTimeout);
            log.info("Sent warmup to RocketMQ");
        } catch (
                JsonProcessingException e) {
            log.error("Failed to serialize warmup: {}", event, e);
        } catch (Exception e) {
            log.error("Failed to send bet settlement to RocketMQ: {}", event, e);
        }
    }
}