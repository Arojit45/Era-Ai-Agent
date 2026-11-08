package com.example.demo.repo;



import com.example.demo.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepo extends JpaRepository<TicketEntity,Long> {
    List<TicketEntity> findTicketEntitiesByUsername(String username);
}
