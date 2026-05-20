package com.uade.order.domain.port.in;

import com.uade.order.domain.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderUseCase {
    List<Order> getAllOrders();
    Optional<Order> getOrderById(Long id);
    Order createOrder(Order order);
    Optional<Order> updateOrder(Long id, Order order);
    boolean deleteOrder(Long id);
}
