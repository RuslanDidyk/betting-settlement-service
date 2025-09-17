package io.github.ruslandidyk.betting.config;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@ImportAutoConfiguration(RocketMQAutoConfiguration.class)
public class RocketMQConfig {

}
