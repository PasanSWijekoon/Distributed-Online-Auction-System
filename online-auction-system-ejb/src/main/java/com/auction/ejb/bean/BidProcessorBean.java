package com.auction.ejb.bean;

import com.auction.ejb.remote.AuctionManager;
import com.auction.ejb.remote.BidProcessor;
import jakarta.ejb.*;

import java.math.BigDecimal;

@Stateless
public class BidProcessorBean implements BidProcessor {

    @EJB
    private AuctionManager auctionManager;

    @Override
    public boolean submitBid(String itemId, String userId, BigDecimal amount) {
        if (itemId == null || userId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("Bid submission failed: Invalid parameters.");
            return false;
        }
        System.out.println("BidProcessorBean: Attempting to place bid for item " + itemId + " by user " + userId + " for " + amount);
        return auctionManager.placeBid(itemId, userId, amount);
    }
}

