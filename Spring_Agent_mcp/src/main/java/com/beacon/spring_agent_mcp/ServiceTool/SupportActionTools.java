package com.beacon.spring_agent_mcp.ServiceTool;

import com.beacon.spring_agent_mcp.Enum.*;
import com.beacon.spring_agent_mcp.dto.SupportSystemDto.RefundResult;
import com.beacon.spring_agent_mcp.dto.SupportSystemDto.TicketLogResult;
import com.beacon.spring_agent_mcp.entity.*;
import com.beacon.spring_agent_mcp.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class SupportActionTools {

    private final OrderRepo orderRepo;
    private final PaymentRepo paymentRepo;
    private final RefundRepo refundRepo;
    private final CustomerRepo customerRepo;
    private final SupportTicketRepo tickets;
    private final ProductRepo productRepo;

    @McpTool(name = "issue_refund",
            description = "Issue a refund against an order and record it. Optionally tie it to a "
                    + "specific charge by transaction reference (e.g. when reversing one half of "
                    + "a duplicate charge), which marks that payment as REFUNDED. This takes real "
                    + "action — only call once you have decided a refund is warranted.")
    public RefundResult issueRefund(
            @McpToolParam(description = "The order number to refund against, e.g. 4471")
            String orderNumber,
            @McpToolParam(description = "The amount to refund, e.g. 199.99")
            BigDecimal amount,
            @McpToolParam(description = "Why the money is going back")
            RefundType refundType,
            @McpToolParam(description = "A short human-readable reason for the refund")
            String reason,
            @McpToolParam(required = false, description = "Transaction reference of the specific "
                    + "charge being reversed; optional. If given, that payment is marked REFUNDED.")
            String transactionRef) {
        Order order = orderRepo.getOrderByOrderNumber(orderNumber);

        Refund refund = new Refund();
        refund.setOrder(order);
        refund.setAmount(amount);
        refund.setCurrency(order.getCurrency());
        refund.setReason(reason);
        refund.setRefundType(refundType);
        refund.setStatus(RefundStatus.PROCESSING);

        if (transactionRef != null && !transactionRef.isBlank()) {
            Payment payment = paymentRepo.findByTransactionRef(transactionRef.trim())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No payment found with transaction reference: " + transactionRef));
            payment.setStatus(PaymentStatus.REFUNDED);
            refund.setPayment(payment);
        }

        Refund saved = refundRepo.save(refund);
        String summary = "Refund of %s %s processed on order %s (%s): %s"
                .formatted(amount, order.getCurrency(), orderNumber, refundType, reason);
        return new RefundResult(saved.getId(), orderNumber, amount, order.getCurrency(),
                refundType.name(), saved.getStatus().name(), summary);
    }

    @McpTool(name = "log_support_ticket",
            description = "Record this email interaction as a support ticket, capturing the raw "
                    + "message, detected language, classified intent/sentiment, and the "
                    + "resolution. Call this last to log what was decided and done.")
    public TicketLogResult logSupportTicket(
            @McpToolParam(description = "Email of the customer the ticket is for")
            String customerEmail,
            @McpToolParam(description = "The customer's original message, verbatim")
            String rawMessage,
            @McpToolParam(description = "What the email is about")
            Intent intent,
            @McpToolParam(description = "The customer's tone")
            TicketSentiment sentiment,
            @McpToolParam(description = "Short subject line summarising the email")
            String subject,
            @McpToolParam(description = "Detected language code(s), e.g. en, hi, or en+hi")
            String detectedLanguage,
            @McpToolParam(description = "What was decided/done and the gist of the reply sent back")
            String resolution,
            @McpToolParam(required = false, description = "Related order number, if any")
            String orderNumber,
            @McpToolParam(required = false, description = "Related product SKU, if any")
            String sku) {
        Customer customer = customerRepo.findByEmailIgnoreCase(customerEmail.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No customer account found for email: " + customerEmail));

        SupportTicket ticket = new SupportTicket();
        ticket.setCustomer(customer);
        ticket.setChannel(SupportChannel.EMAIL);
        ticket.setRawMessage(rawMessage);
        ticket.setIntent(intent);
        ticket.setSentiment(sentiment);
        ticket.setSubject(subject);
        ticket.setDetectedLanguage(detectedLanguage);
        ticket.setResolution(resolution);
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());

        if (orderNumber != null && !orderNumber.isBlank()) {
           Order order = orderRepo.getOrderByOrderNumber(orderNumber.trim());
           if(order!=null) ticket.setOrder(order);
        }
        if (sku != null && !sku.isBlank()) {
            Product product = productRepo.findProductBySkuIgnoreCase(sku.trim());
            if(product!=null) ticket.setProduct(product);
        }

        SupportTicket saved = tickets.save(ticket);
        return new TicketLogResult(saved.getId(), saved.getStatus().name(),
                "Logged ticket #%d for %s.".formatted(saved.getId(), customerEmail));
    }

}
