package com.uade.order.infrastructure.adapter.out.messaging;

import com.uade.order.domain.event.OrderCreatedEvent;
import com.uade.order.domain.port.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!rabbitmq & !kafka")
public class NoOpEventPublisherAdapter implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(NoOpEventPublisherAdapter.class);

    @Override
    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("[NoOp] Evento ignorado (sin broker activo): OrderCreated [id={}, productName={}]",
                event.getOrderId(), event.getProductName());
    }
}
