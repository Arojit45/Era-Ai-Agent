package com.beacon.spring_agent_mcp.repo;

import com.beacon.spring_agent_mcp.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepo extends JpaRepository<Refund,Long> {
}
