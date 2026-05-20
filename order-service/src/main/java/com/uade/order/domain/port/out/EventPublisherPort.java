package com.uade.order.domain.port.out;

import com.uade.order.domain.event.OrderCreatedEvent;

public interface EventPublisherPort {
    void publishOrderCreated(OrderCreatedEvent event);
}
