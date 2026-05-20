package com.uade.order.infrastructure.adapter.out.messaging;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@Profile("kafka")
@EnableKafka
public class KafkaConfig {
}
