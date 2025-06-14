package com.auction.ejb.bean;

import com.auction.ejb.remote.UserSessionManager;
import jakarta.ejb.Remove;
import jakarta.ejb.Stateful;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Stateful
public class UserSessionManagerBean implements UserSessionManager, Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private List<String> watchedAuctionIds = new ArrayList<>();

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
        System.out.println("UserSessionManagerBean: User set to " + userId);
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void watchAuction(String itemId) {
        if (userId == null) {
            System.err.println("UserSessionManagerBean: Cannot watch auction, user not set.");
            return;
        }
        if (!watchedAuctionIds.contains(itemId)) {
            watchedAuctionIds.add(itemId);
            System.out.println("UserSessionManagerBean: User " + userId + " is now watching item " + itemId);
        }
    }

    @Override
    public List<String> getWatchedAuctionIds() {
        return new ArrayList<>(watchedAuctionIds);
    }

    @Remove
    public void logout() {
        System.out.println("UserSessionManagerBean: User " + userId + " logged out. Bean instance will be removed.");
        this.userId = null;
        this.watchedAuctionIds.clear();
    }
}

