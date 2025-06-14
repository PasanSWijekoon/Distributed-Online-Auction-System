package com.auction.ejb.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Bid implements Serializable {
    private static final long serialVersionUID = 1L;

    private String bidId;
    private String itemId;
    private String userId;
    private BigDecimal amount;
    private LocalDateTime bidTime;

    public Bid(String bidId, String itemId, String userId, BigDecimal amount) {
        this.bidId = bidId;
        this.itemId = itemId;
        this.userId = userId;
        this.amount = amount;
        this.bidTime = LocalDateTime.now();
    }

    public String getBidId() { return bidId; }
    public String getItemId() { return itemId; }
    public String getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getBidTime() { return bidTime; }

    @Override
    public String toString() {
        return "Bid{" +
                "bidId='" + bidId + '\'' +
                ", itemId='" + itemId + '\'' +
                ", userId='" + userId + '\'' +
                ", amount=" + amount +
                ", bidTime=" + bidTime +
                '}';
    }
}
