package com.beacon.spring_agent_mcp.ServiceTool;

import com.beacon.spring_agent_mcp.Enum.PaymentStatus;
import com.beacon.spring_agent_mcp.dto.SupportSystemDto.TicketHistory;
import com.beacon.spring_agent_mcp.dto.SupportSystemDto.TicketInfo;
import com.beacon.spring_agent_mcp.dto.SupportSystemDto.WarrantyStatus;
import com.beacon.spring_agent_mcp.dto.SupportSystemDto.DuplicateChargeResult;
import com.beacon.spring_agent_mcp.dto.SupportSystemDto.ProductInfo;
import com.beacon.spring_agent_mcp.dto.SupportSystemDto.OrderDetails;
import com.beacon.spring_agent_mcp.dto.SupportSystemDto.CustomerInfo;

import com.beacon.spring_agent_mcp.entity.*;
import com.beacon.spring_agent_mcp.mapper.Mapper;
import com.beacon.spring_agent_mcp.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupportQueryTools {
    private final CustomerRepo customerRepo;
    private final Mapper mapper;
    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;
    private final PaymentRepo paymentRepo;
    private final SupportTicketRepo supportTicketRepo;

    //    --------------------------------------------------------------
//    IDENTIFY THE CUSTOMER THROUGH CUSTOMER EMAIL
//    --------------------------------------------------------------
    @McpTool(name = "lookup_customer_by_email",description = "Look up a customer account by their email address.Return"+
    "name contact details, preferred language and loyalty tier. " +
            "Use this first to identity who send the email and hoe to address them.")
    public CustomerInfo lookUpCustomerByEmail(@McpToolParam(description = "The Customer's Email address.example:- 'Arojit@gmail.com'") String email){
        Customer c = customerRepo.findByEmailIgnoreCase(email.trim()).orElseThrow(()->new RuntimeException("Customer Not Found of This email: "+email));
        return mapper.maptoCustomerInfo(c);
    }

    @McpTool(name = "get_customer_orders",
            description = "List all orders placed by a customer (most recent first" +
                    "each with its line and payments. Use this to find the order an email is about,"+
                    " or to see the customer's purchase history")
    public List<OrderDetails> getCustomerOrders(@McpToolParam(description = "Customer's email address") String email){
        return orderRepo.findOrderByCustomer_EmailOrderByOrderDateDesc(email).stream().map(mapper::mapToOrderDetails).toList();
    }

    @McpTool(name = "get_order_by_orderNumber",
    description = "Fetch a single order by its order number (the reference customers quote, e.g. \"#4471\")" +
            "including line items and every payment charged against it.")
    public OrderDetails getOrderBYOrderNumber(@McpToolParam(description = "The order number exactly as reference, digital only, e.g") String orderNumber){
        return mapper.mapToOrderDetails(orderRepo.getOrderByOrderNumber(orderNumber));
    }

    //Product Search Tools
    @McpTool(name = "search_products",
            description ="Search the product catalog by name or SKU fragment, Use for pre-sales"+
    "Question or to identify which product a customer is describing.")
    public List<ProductInfo> serachProducts(@McpToolParam(description = "A product name or SKU fragment, e.g. \"X200\" or \" blende\"") String query){
        String q = query.trim();
        return productRepo.findProductByNameIgnoreCaseOrSkuContainingIgnoreCase(q,q).stream().map(mapper::mapToProductInfo).toList();
    }

    @McpTool(name = "get_product_by_sku",
    description = "Get full details for one products by its SKU, including the"+
    "specifications JSON (voltage, dimensions, materials, etc). Use this to answer spec question such as whether a peoduct support European voltage")
    public ProductInfo getProductInfo(@McpToolParam(description = "A product SKU fragment, e.g. X200 or BLND-300") String sku){
        return mapper.mapToProductInfo(productRepo.findProductBySkuIgnoreCase(sku.trim()));
    }


    @McpTool(name = "detect_duplicate_charge",
    description = "Analyse the Payments on an order to detect a duplicate/double charge" +
            "Compare the total captured against the order total and flages repeated" +
            "charges of the same amount. Use when user says they were charged twice")
    public DuplicateChargeResult detectDuplicateCharge(@McpToolParam(description = "Order Number to inspect, eg. 4471") String OrderNumer){
        Order order = orderRepo.getOrderByOrderNumber(OrderNumer);
        List<Payment> captured = paymentRepo.findPaymentByOrder_OrderNumberOrderByChargedAtAsc(OrderNumer.trim())
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.CAPTURED)
                .toList();
        //Actually Charge Total to be
        BigDecimal expected = order.getTotalAmount();
        //Current Total Amount
        BigDecimal totalCharged = captured.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //If ZERO then correct No Duplicate
        BigDecimal overcharged = totalCharged.subtract(expected).max(BigDecimal.ZERO);
        boolean duplicate = captured.size() > 1 && overcharged.signum() > 0;
        String summary = duplicate
                ? "Duplicate charge detected: order %s was charged %d times totalling %s %s, but the order total is only %s %s — overcharged by %s %s. Refund one charge."
                .formatted(OrderNumer, captured.size(), totalCharged, order.getCurrency(),
                        expected, order.getCurrency(), overcharged, order.getCurrency())
                : "No duplicate detected: order %s has %d captured charge(s) totalling %s %s against an order total of %s %s."
                .formatted(OrderNumer, captured.size(), totalCharged, order.getCurrency(),
                        expected, order.getCurrency());

        return new DuplicateChargeResult(OrderNumer, duplicate, captured.size(), totalCharged,
                expected, overcharged, captured.stream().map(mapper::mapToPaymentInfo).toList(), summary);
    }
    @McpTool(name = "check_warranty",
            description = "Check weather a product on an order is still within its warranty" +
                    "window, based on the order date plus the product's warranty length. Use" +
                    " before approving a warranty-based refund or replacement.")
    public WarrantyStatus check(@McpToolParam(description = "The order number the product was bought on, e.g. 4198")
                                String orderNumber,
                                @McpToolParam(description = "The product SKU(STOCK KEEPING UNIT) to check; optional if" +
                                        "the order has a single line item")String SKU){

        Order order = orderRepo.getOrderByOrderNumber(orderNumber);
        Order_items orderItems = resolveItem(order,SKU);
        Product product = orderItems.getProduct();
        //calculate Time of expire the warranty
        LocalDateTime end = order.getOrderDate().plusMonths(product.getWarrantyMonth());
        boolean inWarranty = !LocalDateTime.now().isAfter(end);
        String summary = inWarranty?"%s on order %s is IN warranty (purchased %s, %d-month warranty ends %s)."
                .formatted(product.getName(), orderNumber, order.getOrderDate(),
                        product.getWarrantyMonth(), end)
                : "%s on order %s is OUT of warranty (purchased %s, warranty expired %s)."
                .formatted(product.getName(), orderNumber, order.getOrderDate(), end);
        return new WarrantyStatus(orderNumber, product.getSku(), product.getName(),
                order.getOrderDate()
        , product.getWarrantyMonth(), end.toLocalDate(),inWarranty,summary);
    }

    @McpTool(name = "get-customer-tickets",
            description = "Retrieve the customer's past support tickets (most recent first) so "
                    + "you can recognise repeat failures or recurring complaints — e.g. the same "
                    + "product breaking for the third time, which warrants goodwill.")
    public TicketHistory customer_ticket_History(@McpToolParam(description = "Customer email to get All the support Tickets of an Unique Customer")
                                                    String email){
        List<SupportTicket> supportTicket = supportTicketRepo.findSupportTicketByCustomer_EmailOrderByCreatedAtAsc(email);
        List<TicketInfo> ticketInfos = supportTicketRepo.findSupportTicketByCustomer_EmailOrderByCreatedAtAsc(email).stream().map(mapper::ticketInfo).toList();
        String summary = "Customer %s has %d prior ticket(s) on record.".formatted(email, supportTicket.size());
        return new TicketHistory(email,supportTicket.size(),ticketInfos,summary);
    }
    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------


    private Order_items resolveItem(Order order, String sku) {
        List<Order_items> items = order.getItems();
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order " + order.getOrderNumber() + " has no line items.");
        }
        if (sku == null || sku.isBlank()) {
            if (items.size() > 1) {
                throw new IllegalArgumentException("Order " + order.getOrderNumber()
                        + " has multiple items; specify a SKU to check warranty.");
            }
            return items.get(0);
        }
        return items.stream()
                .filter(i -> i.getProduct().getSku().equalsIgnoreCase(sku.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order " + order.getOrderNumber() + " does not contain SKU " + sku));
    }
}
