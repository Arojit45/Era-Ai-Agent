package com.beacon.spring_agent_mcp.repo;

import com.beacon.spring_agent_mcp.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepo extends JpaRepository<Payment,Long> {
    List<Payment> findPaymentByOrder_OrderNumberOrderByChargedAtAsc(String orderOrderNumber);
}
