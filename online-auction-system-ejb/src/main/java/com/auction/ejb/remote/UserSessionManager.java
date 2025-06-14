package com.auction.ejb.remote;


import jakarta.ejb.Remote;

import java.util.List;

@Remote
public interface UserSessionManager {
    void setUserId(String userId);
    String getUserId();
    void watchAuction(String itemId);
    List<String> getWatchedAuctionIds();
}