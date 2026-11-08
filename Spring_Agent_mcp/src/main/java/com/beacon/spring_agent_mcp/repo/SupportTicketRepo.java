package com.beacon.spring_agent_mcp.repo;

import com.beacon.spring_agent_mcp.dto.SupportSystemDto;
import com.beacon.spring_agent_mcp.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketRepo extends JpaRepository<SupportTicket,Long> {
    List<SupportTicket> findSupportTicketByCustomer_EmailOrderByCreatedAtAsc(String customer_email);
}
