package io.github.ruslandidyk.betting.config;

import io.github.ruslandidyk.betting.event.OutcomeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableKafka
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        return new KafkaAdmin(configs);
    }

    // Automatically create topics on startup
    @Bean
    public NewTopic eventOutcomesTopic() {
        return TopicBuilder
                .name(kafkaProperties.getEventOutcomesTopic())
                .partitions(kafkaProperties.getTopicPartitions())
                .replicas(kafkaProperties.getTopicReplicationFactor())
                .config("retention.ms", "604800000") // 7 days
                .config("segment.ms", "86400000") // 1 day
                .config("compression.type", kafkaProperties.getCompressionType())
                .build();
    }

    @Bean
    public ProducerFactory<String, OutcomeEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        // Bootstrap servers
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        // Serializers
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Performance and reliability settings
        configProps.put(ProducerConfig.ACKS_CONFIG, kafkaProperties.getAcks());
        configProps.put(ProducerConfig.RETRIES_CONFIG, kafkaProperties.getRetries());
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaProperties.getBatchSize());
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, kafkaProperties.getLingerMs());
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, kafkaProperties.getBufferMemory());
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafkaProperties.getCompressionType());
        // Idempotence for exactly-once semantics
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        // Request timeout
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        log.info("Kafka Producer configured with bootstrap servers: {}", kafkaProperties.getBootstrapServers());
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, OutcomeEvent> kafkaTemplate() {
        KafkaTemplate<String, OutcomeEvent> template = new KafkaTemplate<>(producerFactory());
        template.setDefaultTopic(kafkaProperties.getEventOutcomesTopic());
        return template;
    }

    @Bean
    public ConsumerFactory<String, OutcomeEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        // Bootstrap servers
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getGroupId());
        // Deserializers
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        // Jackson specific configurations
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OutcomeEvent.class);
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        // Consumer performance settings
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaProperties.isEnableAutoCommit());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaProperties.getSessionTimeoutMs());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaProperties.getMaxPollRecords());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafkaProperties.getMaxPollIntervalMs());
        // Optimization settings
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        log.info("Kafka Consumer configured for group: {}", kafkaProperties.getGroupId());
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(OutcomeEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OutcomeEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OutcomeEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(kafkaProperties.getConcurrency());
        // Container properties
        ContainerProperties containerProperties = factory.getContainerProperties();
        containerProperties.setPollTimeout(3000);
        // Set acknowledgment mode based on configuration
        switch (kafkaProperties.getAckMode()) {
            case "MANUAL":
                containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL);
                break;
            case "MANUAL_IMMEDIATE":
                containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
                break;
            default:
                containerProperties.setAckMode(ContainerProperties.AckMode.BATCH);
        }
        // Error handling
        factory.setCommonErrorHandler(new DefaultErrorHandler());
        log.info("Kafka Listener Container configured with concurrency: {}", kafkaProperties.getConcurrency());
        return factory;
    }
}
