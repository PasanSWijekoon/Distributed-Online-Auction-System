package com.auction.ejb.model;

import java.io.Serializable;

public class WinnerSummary implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    private int auctionsWonCount;

    public WinnerSummary(String userId, int auctionsWonCount) {
        this.userId = userId;
        this.auctionsWonCount = auctionsWonCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getAuctionsWonCount() {
        return auctionsWonCount;
    }

    public void setAuctionsWonCount(int auctionsWonCount) {
        this.auctionsWonCount = auctionsWonCount;
    }

    public void incrementAuctionsWon() {
        this.auctionsWonCount++;
    }
}
