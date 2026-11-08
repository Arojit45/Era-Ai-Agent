package com.example.demo.service;


import com.example.demo.dto.TicketBookingDto;
import com.example.demo.entity.TicketEntity;
import com.example.demo.repo.TicketRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TicketBookingService {
    private final TicketRepo ticketRepo;
    @Transactional
        public TicketEntity create(TicketBookingDto ticketBookingDto,String contact,String priority){
        TicketEntity ticketEntity = new TicketEntity();
        ticketEntity.setIssue(ticketBookingDto.issue());
        ticketEntity.setUsername(ticketBookingDto.username());
        ticketEntity.setStatus("OPEN");
        ticketEntity.setPriority(priority);
        ticketEntity.setContactNumber(contact);
        ticketEntity.setEct(LocalDateTime.now().plusDays(7));
        ticketEntity.setCreatedAt(LocalDateTime.now());
        return ticketRepo.save(ticketEntity);
    }
    public List<TicketEntity> getAllTicket(String username){
        System.out.println("Come For GetAll");
        return ticketRepo.findTicketEntitiesByUsername(username);
    }
}
