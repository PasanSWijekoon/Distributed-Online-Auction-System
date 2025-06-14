package com.auction.web.servlet;

import com.auction.ejb.model.Item;
import com.auction.ejb.remote.AuctionManager;
import com.auction.ejb.remote.BidProcessor;
import com.auction.web.util.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import java.util.Map;
import java.util.UUID;


@WebServlet("/auction-servlet")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 1,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 15
)
public class AuctionServlet extends HttpServlet {
    private static final String UPLOAD_DIRECTORY = "uploads" + File.separator + "auction_images";
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    @EJB
    private AuctionManager auctionManager;

    @EJB
    private BidProcessor bidProcessor;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Object responseObject = Map.of("success", false, "message", "Invalid GET action.");

        try {
            if ("listActive".equals(action)) {
                responseObject = auctionManager.getAllActiveAuctions();
            } else if ("listFinished".equals(action)) {
                responseObject = auctionManager.getAllFinishedAuctions();
            } else if ("listRecentlyFinished".equals(action)) {
                int count = 3;
                try {
                    if (request.getParameter("count") != null) {
                        count = Integer.parseInt(request.getParameter("count"));
                    }
                } catch (NumberFormatException e) { /* ignore, use default */ }
                responseObject = auctionManager.getRecentlyFinishedAuctions(count);
            } else if ("listTopWinners".equals(action)) {
                int count = 3;
                try {
                    if (request.getParameter("count") != null) {
                        count = Integer.parseInt(request.getParameter("count"));
                    }
                } catch (NumberFormatException e) { /* ignore, use default */ }
                responseObject = auctionManager.getTopWinners(count);
            }
            else if ("listWon".equals(action)) {
                HttpSession session = request.getSession(false);
                String userId = (session != null) ? (String) session.getAttribute("loggedInUserId") : null;

                if (userId != null) {
                    responseObject = auctionManager.getWonItems(userId);
                } else {
                    responseObject = Map.of("success", false, "message", "User not logged in for won items.");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }
            } else if ("getItemDetails".equals(action)) {
                String itemId = request.getParameter("itemId");
                if (itemId != null && !itemId.trim().isEmpty()) {
                    Item item = auctionManager.getAuctionItem(itemId);
                    if (item != null) {
                        responseObject = item;
                    } else {
                        responseObject = Map.of("success", false, "message", "Item not found.");
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    }
                } else {
                    responseObject = Map.of("success", false, "message", "Item ID required.");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseObject = Map.of("success", false, "message", "Server error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        out.print(gson.toJson(responseObject));
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Object responseObject = null;
        HttpSession session = request.getSession(false);
        String loggedInUserId = (session != null) ? (String) session.getAttribute("loggedInUserId") : null;

        System.out.println("AuctionServlet doPost - Action: " + action + ", User: " + loggedInUserId);

        try {
            if ("create".equals(action)) {
                if (loggedInUserId == null) {
                    responseObject = Map.of("success", false, "message", "Authentication required to create auction.");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                } else {
                    String itemName = request.getParameter("itemName");
                    String itemDescription = request.getParameter("itemDescription");
                    String startingPriceStr = request.getParameter("startingPrice");
                    String durationMinutesStr = request.getParameter("durationMinutes");

                    Part filePart = null;
                    try {
                        filePart = request.getPart("itemPicture");
                    } catch (ServletException e) {
                        System.out.println("No file part named 'itemPicture' found or request not multipart. Continuing without image upload.");
                    }

                    String uploadedFileUrl = "images/placeholder.png";

                    if (filePart != null && filePart.getSize() > 0) {
                        String originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                        String fileExtension = "";
                        int i = originalFileName.lastIndexOf('.');
                        if (i > 0) {
                            fileExtension = originalFileName.substring(i);
                        }
                        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

                        String applicationPath = request.getServletContext().getRealPath("");
                        String uploadFilePath = applicationPath + File.separator + UPLOAD_DIRECTORY;

                        File uploadDir = new File(uploadFilePath);
                        if (!uploadDir.exists()) {
                            if(uploadDir.mkdirs()){
                                System.out.println("Upload directory created: " + uploadFilePath);
                            } else {
                                System.err.println("Failed to create upload directory: " + uploadFilePath);
                                throw new IOException("Failed to create upload directory.");
                            }
                        }

                        File uploadedFile = new File(uploadDir, uniqueFileName);
                        try (InputStream fileContent = filePart.getInputStream();
                             OutputStream outputStream = new FileOutputStream(uploadedFile)) {
                            int read;
                            final byte[] bytes = new byte[1024];
                            while ((read = fileContent.read(bytes)) != -1) {
                                outputStream.write(bytes, 0, read);
                            }
                            uploadedFileUrl = UPLOAD_DIRECTORY.replace(File.separator, "/") + "/" + uniqueFileName;
                            System.out.println("File uploaded successfully: " + uploadedFileUrl);
                        } catch (IOException e) {
                            System.err.println("Error uploading file: " + e.getMessage());
                        }
                    }

                    System.out.println("Create Action - Received itemName: " + itemName);
                    System.out.println("Create Action - Received itemDescription: " + itemDescription);
                    System.out.println("Create Action - Final imageUrl: " + uploadedFileUrl);
                    System.out.println("Create Action - Received startingPriceStr: " + startingPriceStr);
                    System.out.println("Create Action - Received durationMinutesStr: " + durationMinutesStr);

                    if (itemName == null || itemName.trim().isEmpty() ||
                            itemDescription == null || itemDescription.trim().isEmpty() ||
                            startingPriceStr == null || startingPriceStr.trim().isEmpty() ||
                            durationMinutesStr == null || durationMinutesStr.trim().isEmpty()) {
                        responseObject = Map.of("success", false, "message", "Missing or invalid parameters for creating auction.");
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    } else {
                        BigDecimal startingPrice = new BigDecimal(startingPriceStr);
                        int durationMinutesVal = Integer.parseInt(durationMinutesStr);

                        String itemId = auctionManager.createAuction(itemName, itemDescription, uploadedFileUrl, startingPrice, durationMinutesVal, loggedInUserId);
                        if (itemId != null) {
                            responseObject = Map.of("success", true, "itemId", itemId, "message", "Auction created.");
                        } else {
                            responseObject = Map.of("success", false, "message", "Failed to create auction.");
                        }
                    }
                }
            } else if ("placeBid".equals(action)) {
                if (loggedInUserId == null) {
                    responseObject = Map.of("success", false, "message", "Authentication required to place bid.");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                } else {
                    String itemId = request.getParameter("itemId");
                    String amountStr = request.getParameter("amount");

                    if (itemId == null || itemId.trim().isEmpty() ||
                            amountStr == null || amountStr.trim().isEmpty()) {
                        responseObject = Map.of("success", false, "message", "Missing or invalid parameters for placing bid.");
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    } else {
                        BigDecimal amount = new BigDecimal(amountStr);
                        boolean success = bidProcessor.submitBid(itemId, loggedInUserId, amount);
                        if (success) {
                            Item updatedItem = auctionManager.getAuctionItem(itemId);
                            responseObject = Map.of("success", true, "message", "Bid placed successfully.", "updatedItem", updatedItem);
                        } else {
                            Item item = auctionManager.getAuctionItem(itemId);
                            String failMessage = "Bid failed. It might be too low, auction ended, or item not found.";
                            if (item != null && !item.isActive()) failMessage = "Bid failed: Auction has already ended.";
                            else if (item != null && amount.compareTo(item.getCurrentHighestBid()) <= 0) failMessage = "Bid failed: Your bid is not higher than the current bid ($" + item.getCurrentHighestBid() + ").";
                            responseObject = Map.of("success", false, "message", failMessage);
                        }
                    }
                }
            } else {
                responseObject = Map.of("success", false, "message", "Unsupported POST action.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (NumberFormatException e) {
            responseObject = Map.of("success", false, "message", "Invalid number format for price, duration, or bid amount.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            responseObject = Map.of("success", false, "message", "An unexpected server error occurred: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        out.print(gson.toJson(responseObject));
        out.flush();
    }
}