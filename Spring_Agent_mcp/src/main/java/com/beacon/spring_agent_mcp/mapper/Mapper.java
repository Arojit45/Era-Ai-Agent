package com.beacon.spring_agent_mcp.mapper;

import com.beacon.spring_agent_mcp.dto.SupportSystemDto.TicketInfo;
import com.beacon.spring_agent_mcp.dto.SupportSystemDto.PaymentInfo;
import com.beacon.spring_agent_mcp.dto.SupportSystemDto.ProductInfo;
import com.beacon.spring_agent_mcp.dto.SupportSystemDto.OrderDetails;
import com.beacon.spring_agent_mcp.dto.SupportSystemDto.CustomerInfo;
import com.beacon.spring_agent_mcp.entity.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class Mapper {
    public CustomerInfo maptoCustomerInfo(Customer customer){
        return new CustomerInfo(customer.getId(), customer.getFullName(), customer.getEmail(), customer.getPhone(), customer.getPreferredLanguage(),
                customer.getLoyaltyTier().toString());
    }
    public OrderDetails mapToOrderDetails(Order o){
        return new OrderDetails(
                o.getShippingAddress(),
                o.getTotalAmount(),
                o.getCurrency(),
                o.getItems(),
                o.getPayments()
        );
    }
    public ProductInfo mapToProductInfo(Product product){
        return new ProductInfo(product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getCurrency(),
                product.getSpecifications(),
                product.getWarrantyMonth(),
                product.getStockQuantity());
    }
    public PaymentInfo mapToPaymentInfo(Payment payment){
        return new PaymentInfo(payment.getId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod().toString(),
                payment.getTransactionRef(),
                payment.getStatus().toString(),
                payment.getChargedAt());
    }

    public TicketInfo ticketInfo(SupportTicket s){
        return new TicketInfo(s.getId(),s.getSubject(),s.getIntent().name(),s.getSentiment().name(),s.getStatus().name(),
                s.getResolution(),s.getCreatedAt());
    }
}
