package com.beacon.spring_agent_mcp.repo;

import com.beacon.spring_agent_mcp.entity.Order_items;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Order_itemsRepo extends JpaRepository<Order_items,Long> {
}
