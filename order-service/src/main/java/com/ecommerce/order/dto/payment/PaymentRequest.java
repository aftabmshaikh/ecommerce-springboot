package com.ecommerce.order.dto.payment;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class PaymentRequest {
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethodId; // Stripe, PayPal, etc.
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code")
    private String currency;
    
    private String description;
    
    // Credit card details (if not using payment method ID)
    private CardDetails card;
    
    // Billing address
    private AddressRequest billingAddress;
    
    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getPaymentMethodId() {
        return paymentMethodId;
    }
    
    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public CardDetails getCard() {
        return card;
    }
    
    public void setCard(CardDetails card) {
        this.card = card;
    }
    
    public AddressRequest getBillingAddress() {
        return billingAddress;
    }
    
    public void setBillingAddress(AddressRequest billingAddress) {
        this.billingAddress = billingAddress;
    }
    
    public static class CardDetails {
        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "^[0-9]{13,19}$", message = "Invalid card number")
        private String number;
        
        @NotBlank(message = "Expiry month is required")
        @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Invalid expiry month")
        private String expMonth;
        
        @NotBlank(message = "Expiry year is required")
        @Pattern(regexp = "^[0-9]{4}$", message = "Invalid expiry year")
        private String expYear;
        
        @NotBlank(message = "CVC is required")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "Invalid CVC")
        private String cvc;
        
        @NotBlank(message = "Cardholder name is required")
        private String name;
        
        public String getNumber() { return number; }
        public void setNumber(String number) { this.number = number; }
        
        public String getExpMonth() { return expMonth; }
        public void setExpMonth(String expMonth) { this.expMonth = expMonth; }
        
        public String getExpYear() { return expYear; }
        public void setExpYear(String expYear) { this.expYear = expYear; }
        
        public String getCvc() { return cvc; }
        public void setCvc(String cvc) { this.cvc = cvc; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    
    public static class AddressRequest {
        @NotBlank(message = "City is required")
        private String city;
        
        @NotBlank(message = "Country is required")
        private String country;
        
        @NotBlank(message = "Line 1 is required")
        private String line1;
        
        private String line2;
        
        @NotBlank(message = "Postal code is required")
        private String postalCode;
        
        private String state;
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getLine1() { return line1; }
        public void setLine1(String line1) { this.line1 = line1; }
        
        public String getLine2() { return line2; }
        public void setLine2(String line2) { this.line2 = line2; }
        
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
    }
}
