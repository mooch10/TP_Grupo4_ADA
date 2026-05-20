package com.uade.order.infrastructure.adapter.out.messaging;

import com.uade.order.domain.event.OrderCreatedEvent;
import com.uade.order.domain.port.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class KafkaPublisherAdapter implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaPublisherAdapter.class);
    private static final String TOPIC = "order-created";

    @Autowired(required = false)
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Override
    public void publishOrderCreated(OrderCreatedEvent event) {
        if (kafkaTemplate != null) {
            kafkaTemplate.send(TOPIC, String.valueOf(event.getOrderId()), event);
            log.info("Evento publicado (Kafka): OrderCreated [id={}, productName={}]", 
                     event.getOrderId(), event.getProductName());
        } else {
            log.warn("KafkaTemplate no está disponible");
        }
    }
}
