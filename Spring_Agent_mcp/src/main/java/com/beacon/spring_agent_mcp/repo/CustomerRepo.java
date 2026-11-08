package com.beacon.spring_agent_mcp.repo;

import com.beacon.spring_agent_mcp.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepo extends JpaRepository<Customer,Long> {
    Optional<Customer> findByEmailIgnoreCase(String email);

}
