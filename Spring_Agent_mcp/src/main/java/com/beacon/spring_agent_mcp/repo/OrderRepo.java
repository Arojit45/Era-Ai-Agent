package com.beacon.spring_agent_mcp.repo;

import com.beacon.spring_agent_mcp.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.ScopedValue;
import java.util.List;

public interface OrderRepo extends JpaRepository<Order,Long> {
    List<Order> findOrderByCustomer_EmailOrderByOrderDateDesc(String customerEmail);

    Order getOrderByOrderNumber(String orderNumber);

}
