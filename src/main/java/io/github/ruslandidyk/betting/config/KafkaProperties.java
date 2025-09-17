package io.github.ruslandidyk.betting.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaProperties {
    private String bootstrapServers;
    private String groupId;
    private String autoOffsetReset;
    private int sessionTimeoutMs;
    private int maxPollRecords;
    private int maxPollIntervalMs;
    private boolean enableAutoCommit;
    private String ackMode;
    private int concurrency;
    // Producer properties
    private String acks;
    private int retries;
    private int batchSize;
    private int lingerMs;
    private int bufferMemory;
    private String compressionType;
    // Topic properties
    private String eventOutcomesTopic;
    private int topicPartitions;
    private short topicReplicationFactor;
}