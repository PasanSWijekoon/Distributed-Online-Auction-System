package com.auction.ejb.bean;

import com.auction.ejb.model.Item;
import com.auction.ejb.model.WinnerSummary;
import com.auction.ejb.remote.AuctionManager;
import jakarta.ejb.Timer;
import jakarta.jms.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
@Startup
public class AuctionManagerBean implements AuctionManager {

    private final Map<String, Item> auctionItems = new ConcurrentHashMap<>();

    @Resource
    private TimerService timerService;

    @Resource(lookup = "jms/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "jms/topic/AuctionUpdatesTopic")
    private Topic auctionUpdatesTopic;

    @PostConstruct
    public void init() {
        System.out.println("AuctionManagerBean Initialized. Scheduling auction checks.");
        timerService.createIntervalTimer(0, 60000, new TimerConfig("auctionCheckTimer", false));

        createAuction("Vintage Rolex Watch", "A beautiful vintage Rolex from 1960, good condition.", "images/watch.jpg", new BigDecimal("500.00"), 1, "seller001");
        createAuction("Antique Painting", "18th-century oil painting, landscape.", "images/painting.jpg", new BigDecimal("1200.00"), 180, "seller002");
        createAuction("Rare Coin Collection", "A collection of rare 19th-century coins.", "images/coins.jpg", new BigDecimal("300.00"), 5, "seller001");

        Item endedItem1 = new Item("ITEM-ENDED1", "Old Radio", "A classic radio from the 1940s, non-functional.", "images/radio.jpg", new BigDecimal("50.00"), LocalDateTime.now().minusHours(2), "seller003");
        endedItem1.setActive(false);
        endedItem1.setCurrentHighestBid(new BigDecimal("75.00"));
        endedItem1.setCurrentHighestBidderUserId("Elon Musk");
        auctionItems.put(endedItem1.getItemId(), endedItem1);

        Item endedItem2 = new Item("ITEM-ENDED2", "Signed Baseball", "Baseball signed by a famous player.", "images/baseball.jpg", new BigDecimal("150.00"), LocalDateTime.now().minusHours(1), "seller002");
        endedItem2.setActive(false);
        endedItem2.setCurrentHighestBid(new BigDecimal("200.00"));
        endedItem2.setCurrentHighestBidderUserId("Suzie Kur");
        auctionItems.put(endedItem2.getItemId(), endedItem2);

        Item endedItem3 = new Item("ITEM-ENDED3", "Vintage Camera", "A working vintage film camera.", "images/camera.jpg", new BigDecimal("90.00"), LocalDateTime.now().minusMinutes(30), "seller001");
        endedItem3.setActive(false);
        endedItem3.setCurrentHighestBid(new BigDecimal("110.00"));
        endedItem3.setCurrentHighestBidderUserId("Elon Musk");
        auctionItems.put(endedItem3.getItemId(), endedItem3);
    }

    @Override
    @Lock(LockType.WRITE)
    public String createAuction(String itemName, String description, String imageUrl, BigDecimal startingPrice, int durationInMinutes, String sellerUserId) {
        String itemId = "ITEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDateTime endTime = LocalDateTime.now().plusMinutes(durationInMinutes);
        String finalImageUrl = (imageUrl == null || imageUrl.trim().isEmpty()) ? "images/placeholder.png" : imageUrl;
        Item newItem = new Item(itemId, itemName, description, finalImageUrl, startingPrice, endTime, sellerUserId);
        auctionItems.put(itemId, newItem);
        System.out.println("Auction created: " + newItem);
        broadcastAuctionUpdate("AUCTION_CREATED", newItem);
        return itemId;
    }

    @Override
    @Lock(LockType.READ)
    public Item getAuctionItem(String itemId) {
        return auctionItems.get(itemId);
    }

    @Override
    @Lock(LockType.READ)
    public List<Item> getAllActiveAuctions() {
        return auctionItems.values().stream()
                .filter(Item::isActive)
                .sorted(Comparator.comparing(Item::getAuctionEndTime))
                .collect(Collectors.toList());
    }

    @Override
    @Lock(LockType.READ)
    public List<Item> getAllFinishedAuctions() {
        return auctionItems.values().stream()
                .filter(item -> !item.isActive())
                .sorted(Comparator.comparing(Item::getAuctionEndTime).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Lock(LockType.READ)
    public List<Item> getRecentlyFinishedAuctions(int count) {
        return getAllFinishedAuctions().stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    @Lock(LockType.READ)
    public List<WinnerSummary> getTopWinners(int count) {
        Map<String, Long> winsByUser = auctionItems.values().stream()
                .filter(item -> !item.isActive() && item.getCurrentHighestBidderUserId() != null && !item.getCurrentHighestBidderUserId().isEmpty() && !item.getCurrentHighestBidderUserId().equalsIgnoreCase("N/A"))
                .collect(Collectors.groupingBy(Item::getCurrentHighestBidderUserId, Collectors.counting()));

        return winsByUser.entrySet().stream()
                .map(entry -> new WinnerSummary(entry.getKey(), entry.getValue().intValue()))
                .sorted(Comparator.comparingInt(WinnerSummary::getAuctionsWonCount).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    @Lock(LockType.READ)
    public List<Item> getWonItems(String userId) {
        if (userId == null) return new ArrayList<>();
        return auctionItems.values().stream()
                .filter(item -> !item.isActive() && userId.equals(item.getCurrentHighestBidderUserId()))
                .sorted(Comparator.comparing(Item::getAuctionEndTime).reversed())
                .collect(Collectors.toList());
    }


    @Override
    @Lock(LockType.WRITE)
    public boolean placeBid(String itemId, String userId, BigDecimal bidAmount) {
        Item item = auctionItems.get(itemId);
        if (item == null || !item.isActive()) {
            return false;
        }
        if (item.getAuctionEndTime().isBefore(LocalDateTime.now())) {
            item.setActive(false);
            broadcastAuctionUpdate("AUCTION_ENDED", item);
            return false;
        }
        if (bidAmount.compareTo(item.getCurrentHighestBid()) > 0) {
            item.setCurrentHighestBid(bidAmount);
            item.setCurrentHighestBidderUserId(userId);
            broadcastAuctionUpdate("NEW_HIGH_BID", item);
            return true;
        } else {
            return false;
        }
    }

    @Timeout
    @Lock(LockType.WRITE)
    public void programmaticTimeout(Timer timer) {
        if ("auctionCheckTimer".equals(timer.getInfo())) {
            checkAndCloseAuctions();
        }
    }

    @Override
    @Lock(LockType.WRITE)
    public void checkAndCloseAuctions() {
        LocalDateTime now = LocalDateTime.now();
        for (Item item : auctionItems.values()) {
            if (item.isActive() && item.getAuctionEndTime().isBefore(now)) {
                item.setActive(false);
                System.out.println("Auction for item " + item.getItemId() + " ('" + item.getName() + "') has ended. Winning bid: " + item.getCurrentHighestBid() + " by " + (item.getCurrentHighestBidderUserId() != null ? item.getCurrentHighestBidderUserId() : "N/A"));
                broadcastAuctionUpdate("AUCTION_ENDED", item);
            }
        }
    }

    private void broadcastAuctionUpdate(String updateType, Item item) {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            MapMessage message = context.createMapMessage();
            message.setString("updateType", updateType);
            message.setString("itemId", item.getItemId());
            message.setString("name", item.getName());
            message.setString("imageUrl", item.getImageUrl());
            message.setString("currentHighestBid", item.getCurrentHighestBid().toPlainString());
            message.setString("currentHighestBidderUserId", item.getCurrentHighestBidderUserId() == null ? "N/A" : item.getCurrentHighestBidderUserId());
            message.setBoolean("active", item.isActive());
            message.setString("auctionEndTime", item.getAuctionEndTime().toString());
            message.setString("description", item.getDescription());
            message.setString("startingPrice", item.getStartingPrice().toPlainString());

            JMSProducer producer = context.createProducer();
            producer.send(auctionUpdatesTopic, message);
            System.out.println("JMS Broadcast: " + updateType + " for " + item.getItemId() + ", active: " + item.isActive());
        } catch (JMSException e) {
            System.err.println("Error broadcasting JMS message: " + e.getMessage());
        }
    }
}
