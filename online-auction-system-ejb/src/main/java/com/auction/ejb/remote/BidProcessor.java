package com.auction.ejb.remote;


import jakarta.ejb.Remote;

import java.math.BigDecimal;

@Remote
public interface BidProcessor {
    boolean submitBid(String itemId, String userId, BigDecimal amount);
}