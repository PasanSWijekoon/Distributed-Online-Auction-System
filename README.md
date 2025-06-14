# Distributed Online Auction System

This project implements a distributed online auction system, built using Java EE (Jakarta EE 10) and Maven for a multi-module enterprise application. It provides functionalities for users to register, log in, create auction items, place bids, and view auction statuses in real-time.

## Project Structure

The project is organized into a Maven multi-module structure:

* **`online-auction-system-ear`**: The Enterprise Archive (EAR) module that packages the EJB and Web modules for deployment to an application server.
* **`online-auction-system-ejb`**: The Enterprise JavaBean (EJB) module containing the business logic and data models. This includes:
    * **Beans**: `AuctionManagerBean` (manages auctions, items, and winning bids, handles auction creation and closing, and broadcasts updates via JMS), `BidProcessorBean` (handles bid submissions), `UserManagerBean` (manages user registration and authentication), `UserSessionManagerBean` (manages user-specific sessions and watched auctions), and `AuctionUpdateMDB` (Message Driven Bean for processing auction updates from JMS and firing CDI events to WebSockets).
    * **Models**: `Item`, `Bid`, `UserData`, `WinnerSummary`, `AuctionUpdateEventData`.
    * **Remote Interfaces**: `AuctionManager`, `BidProcessor`, `UserManager`, `UserSessionManager`.
* **`online-auction-system-web`**: The Web module providing the user interface and handling web requests. This includes:
    * **Servlets**: `AuctionServlet` (handles auction-related requests like listing, creating, and placing bids) and `UserServlet` (handles user registration, login, and logout).
    * **Filters**: `AuthCheckFilter` (ensures authentication for protected resources).
    * **WebSockets**: `AuctionWebSocketServer` (for real-time auction updates).
    * **Utility**: `LocalDateTimeAdapter` (for JSON serialization/deserialization of `LocalDateTime` objects).
    * **Frontend**: HTML, CSS, and JavaScript files (`index.html`, `login.html`, `register.html`, `home-style.css`, `auth.js`, `auth-index.js`, `index-app.js`) provide the user interface and handle client-side interactions.

## Technologies Used

* **Backend**: Java EE (Jakarta EE 10), EJBs (Singleton, Stateless, Message-Driven), JMS (for auction updates), CDI (for event broadcasting), WebSockets (for real-time communication).
* **Frontend**: HTML5, CSS3, JavaScript.
* **Libraries**:
    * **Gson**: For JSON serialization and deserialization.
    * **Notyf**: A JavaScript library for toast notifications.
    * **Font Awesome**: For icons.
* **Build Tool**: Maven.
* **Development Environment**: Configured for IntelliJ IDEA, Eclipse, NetBeans, and VS Code.

## Features

* **User Management**: Register new users, authenticate existing users, and manage user sessions.
* **Auction Creation**: Users can create new auction items with a name, description, image, starting price, and duration.
* **Bidding**: Users can place bids on active auction items. The system ensures bids are higher than the current highest bid.
* **Real-time Updates**: Auction status, including current bids and winning bidders, are updated in real-time using WebSockets.
* **Auction Management**: The system automatically checks and closes auctions when their time expires.
* **Auction Listings**: View all active auctions, all finished auctions, recently finished auctions, and a list of top winners.
* **"My Won Items"**: Users can view a list of items they have won.
* **Image Uploads**: Supports uploading images for auction items.

## Setup and Build

1.  **Prerequisites**:
    * JDK 17 or higher.
    * Maven 3.x.
    * A Jakarta EE 10 compatible application server (e.g., WildFly, GlassFish).

2.  **Build the Project**:
    Navigate to the root directory of the project (where `pom.xml` is located) and run:
    ```bash
    mvn clean install
    ```
    This command will compile all modules, run tests, and package the `online-auction-system-ear` module into an `.ear` file.

3.  **Deployment**:
    Deploy the generated `online-auction-system-ear.ear` file (located in `online-auction-system-ear/target/`) to your application server.

## Usage

1.  **Access the Application**: Once deployed, the web application will be accessible at `/auction-app` (e.g., `http://localhost:8080/auction-app/`).
2.  **Registration/Login**: Navigate to `register.html` to create a new account or `login.html` to log in.
3.  **Browse Auctions**: After logging in, you can view active auctions, create new ones, place bids, and see your won items.
4.  **Real-time Interaction**: The system will provide real-time updates on bids and auction statuses without needing to refresh the page.

## Developer

* Pasan Wijekoon
