package com.beacon.spring_agent_mcp.entity;

import com.beacon.spring_agent_mcp.Enum.Intent;
import com.beacon.spring_agent_mcp.Enum.SupportChannel;
import com.beacon.spring_agent_mcp.Enum.TicketSentiment;
import com.beacon.spring_agent_mcp.Enum.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportChannel channel;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(name = "raw_message", nullable = false, columnDefinition = "TEXT")
    private String rawMessage;

    @Column(name = "detected_language", length = 50)
    private String detectedLanguage;

    // Examples: ORDER_TRACKING, REFUND_REQUEST, PRODUCT_QUERY
    @Column(length = 100)
    private Intent intent;

    @Enumerated(EnumType.STRING)
    private TicketSentiment sentiment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = TicketStatus.OPEN;
        }
    }
}
