package com.auction.ejb.model;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    private String itemId;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal startingPrice;
    private BigDecimal currentHighestBid;
    private String currentHighestBidderUserId;
    private LocalDateTime auctionEndTime;
    private boolean active;
    private String sellerUserId;

    public Item(String itemId, String name, String description, String imageUrl, BigDecimal startingPrice, LocalDateTime auctionEndTime, String sellerUserId) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.imageUrl = (imageUrl == null || imageUrl.trim().isEmpty()) ? "images/placeholder.png" : imageUrl;
        this.startingPrice = startingPrice;
        this.currentHighestBid = startingPrice;
        this.currentHighestBidderUserId = null;
        this.auctionEndTime = auctionEndTime;
        this.active = true;
        this.sellerUserId = sellerUserId;
    }

    public String getItemId() { return itemId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public BigDecimal getStartingPrice() { return startingPrice; }
    public BigDecimal getCurrentHighestBid() { return currentHighestBid; }
    public String getCurrentHighestBidderUserId() { return currentHighestBidderUserId; }
    public LocalDateTime getAuctionEndTime() { return auctionEndTime; }
    public boolean isActive() { return active; }
    public String getSellerUserId() { return sellerUserId; }

    public void setCurrentHighestBid(BigDecimal currentHighestBid) { this.currentHighestBid = currentHighestBid; }
    public void setCurrentHighestBidderUserId(String currentHighestBidderUserId) { this.currentHighestBidderUserId = currentHighestBidderUserId; }
    public void setActive(boolean active) { this.active = active; }
    public void setAuctionEndTime(LocalDateTime auctionEndTime) { this.auctionEndTime = auctionEndTime; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(itemId, item.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }

    @Override
    public String toString() {
        return "Item{" +
                "itemId='" + itemId + '\'' +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", currentHighestBid=" + currentHighestBid +
                ", auctionEndTime=" + auctionEndTime +
                ", active=" + active +
                '}';
    }
}