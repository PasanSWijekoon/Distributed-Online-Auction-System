package com.auction.ejb.remote;


import com.auction.ejb.model.Item;
import com.auction.ejb.model.WinnerSummary;
import jakarta.ejb.Remote;
import java.math.BigDecimal;
import java.util.List;

@Remote
public interface AuctionManager {
    String createAuction(String itemName, String description, String imageUrl, BigDecimal startingPrice, int durationInMinutes, String sellerUserId);
    Item getAuctionItem(String itemId);
    List<Item> getAllActiveAuctions();
    List<Item> getAllFinishedAuctions();
    List<Item> getRecentlyFinishedAuctions(int count);
    List<WinnerSummary> getTopWinners(int count);
    List<Item> getWonItems(String userId);
    boolean placeBid(String itemId, String userId, BigDecimal bidAmount);
    void checkAndCloseAuctions();
}
