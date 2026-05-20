package com.uade.order.infrastructure.config;

import com.uade.order.domain.model.Order;
import com.uade.order.infrastructure.adapter.out.persistence.OrderRepositoryAdapter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public ApplicationRunner initializeData(OrderRepositoryAdapter orderRepository) {
        return args -> {
            // Crear algunas órdenes iniciales
            Order order1 = new Order("Laptop", 2, 999.99);
            order1.setStatus("CONFIRMED");
            orderRepository.save(order1);

            Order order2 = new Order("Mouse", 10, 29.99);
            order2.setStatus("SHIPPED");
            orderRepository.save(order2);

            Order order3 = new Order("Keyboard", 5, 79.99);
            order3.setStatus("PENDING");
            orderRepository.save(order3);
        };
    }
}
