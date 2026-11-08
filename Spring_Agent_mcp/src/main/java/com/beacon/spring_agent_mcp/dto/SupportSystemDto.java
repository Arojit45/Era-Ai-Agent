package com.beacon.spring_agent_mcp.dto;

import com.beacon.spring_agent_mcp.Enum.OrderStatus;
import com.beacon.spring_agent_mcp.entity.Order_items;
import com.beacon.spring_agent_mcp.entity.Payment;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class SupportSystemDto {
    private record SupportDto(){}
    public record CustomerInfo(
            Long id,
            String fullName,
            String email,
            String phone,
            String preferredLanguage,
            String loyaltyTier
    ){}

    public record ProductInfo(
            Long id,
            String sku,
            String name,
            String description,
            String category,
            BigDecimal price,
            String currency,
            String specifications,
            Integer warrantyMonths,
            Integer stockQuantity
    ){}
    public record OrderDetails(
            String shippingAddress,
            BigDecimal totalAmount,
            String currency,
            List<Order_items> items,
            List<Payment> payments
    ){}
    public record DuplicateChargeResult(
            String orderNumber,
            boolean duplicateDetected,
            int chargeCount,
            BigDecimal totalCharged,
            BigDecimal expectedAmount,
            BigDecimal overchargeAmount,
            List<PaymentInfo> charges,
            String summary
    ){}

    public record PaymentInfo(
            Long id,
            BigDecimal amount,
            String currency,
            String method,
            String transactionRef,
            String status,
            LocalDateTime chargedAt
    ){}

    public record WarrantyStatus(
            String orderNumber,
            String sku,
            String productName,
            LocalDateTime orderDate,
            int warrantyMonths,
            LocalDate warrantyEndDate,
            boolean inWarranty,
            String summary) {
    }
    public record TicketInfo(
            Long id,
            String subject,
            String intent,
            String sentiment,
            String status,
            String resolution,
            LocalDateTime createdAt) {
    }

    public record TicketHistory(
            String customerEmail,
            int totalTickets,
            List<TicketInfo> tickets,
            String summary) {
    }

    public record RefundResult(
            Long refundId,
            String orderNumber,
            BigDecimal amount,
            String currency,
            String refundType,
            String status,
            String summary) {
    }

    public record TicketLogResult(
            Long ticketId,
            String status,
            String summary) {
    }
}
