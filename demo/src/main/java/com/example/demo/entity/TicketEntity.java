package com.example.demo.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter@Setter
@Table(name = "TicketBooking")
public class TicketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String issue;
    private String priority; //HIGH,LOW,URGENT
    private String contactNumber;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime ect;


}
