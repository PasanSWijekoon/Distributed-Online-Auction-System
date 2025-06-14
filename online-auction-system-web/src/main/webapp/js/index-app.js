document.addEventListener('DOMContentLoaded', () => {
    const appContainer = document.querySelector('.app-container');
    const leftSidebar = document.getElementById('leftSidebar');
    const rightSidebar = document.getElementById('rightSidebar');
    const leftSidebarToggleBtn = document.getElementById('leftSidebarToggle');
    const rightSidebarToggleBtn = document.getElementById('rightSidebarToggle');

    const navLinks = document.querySelectorAll('.sidebar-nav a');
    const pageSections = document.querySelectorAll('.page-content');

    const notyf = new Notyf({
        duration: 4000, // Default duration for notifications
        position: { x: 'right', y: 'top' },
        types: [
            { type: 'info', background: '#57aeff', icon: {className: 'fas fa-info-circle', tagName: 'i', color: 'white'} }
        ]
    });

    // Sidebar Toggles
    if (leftSidebarToggleBtn && leftSidebar) {
        leftSidebarToggleBtn.addEventListener('click', () => {
            if (window.innerWidth <= 768) {
                leftSidebar.classList.toggle('open');
                leftSidebarToggleBtn.innerHTML = leftSidebar.classList.contains('open') ? '<i class="fas fa-times"></i>' : '<i class="fas fa-bars"></i>';
            } else {
                appContainer.classList.toggle('left-collapsed');
                leftSidebar.classList.toggle('collapsed', appContainer.classList.contains('left-collapsed'));
                leftSidebarToggleBtn.innerHTML = '<i class="fas fa-bars"></i>';
            }
        });
    }

    if (rightSidebarToggleBtn && rightSidebar) {
        rightSidebarToggleBtn.addEventListener('click', () => {
            if (window.innerWidth <= 768) {
                rightSidebar.classList.toggle('open');
                rightSidebarToggleBtn.innerHTML = rightSidebar.classList.contains('open') ? '<i class="fas fa-times"></i>' : '<i class="fas fa-info-circle"></i>';
            } else {
                const isNowEffectivelyCollapsed = appContainer.classList.toggle('right-collapsed');
                appContainer.classList.toggle('right-sidebar-shown', !isNowEffectivelyCollapsed);
                rightSidebarToggleBtn.innerHTML = isNowEffectivelyCollapsed ? '<i class="fas fa-chevron-left"></i>' : '<i class="fas fa-chevron-right"></i>';
            }
        });
    }

    function initializeToggleButtons() {
        if (leftSidebarToggleBtn) {
            if (window.innerWidth <= 768) {
                leftSidebarToggleBtn.innerHTML = leftSidebar.classList.contains('open') ? '<i class="fas fa-times"></i>' : '<i class="fas fa-bars"></i>';
            } else {
                leftSidebarToggleBtn.innerHTML = '<i class="fas fa-bars"></i>';
            }
        }
        if (rightSidebarToggleBtn) {
            if (window.innerWidth <= 768) {
                rightSidebarToggleBtn.innerHTML = rightSidebar.classList.contains('open') ? '<i class="fas fa-times"></i>' : '<i class="fas fa-info-circle"></i>';
            } else {
                rightSidebarToggleBtn.innerHTML = appContainer.classList.contains('right-collapsed') ? '<i class="fas fa-chevron-left"></i>' : '<i class="fas fa-chevron-right"></i>';
            }
        }
    }
    initializeToggleButtons();
    window.addEventListener('resize', initializeToggleButtons);


    // Page Navigation
    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const pageId = link.getAttribute('data-page');

            pageSections.forEach(section => section.style.display = 'none');
            navLinks.forEach(nav => nav.classList.remove('active-nav-link'));

            const targetPage = document.getElementById(pageId + 'Page');
            if (targetPage) {
                targetPage.style.display = 'block';
                link.classList.add('active-nav-link');
                if (pageId === 'viewAllAuctions') fetchActiveAuctions();
                else if (pageId === 'myWonItems') fetchMyWonItems();
                else if (pageId === 'allFinishedBids') fetchAllFinishedAuctions();
            }

            if (window.innerWidth <= 768 && leftSidebar.classList.contains('open')) {
                leftSidebar.classList.remove('open');
                leftSidebarToggleBtn.innerHTML = '<i class="fas fa-bars"></i>';
            }
        });
    });

    const activeAuctionListContainer = document.getElementById('activeAuctionListContainer');
    const refreshActiveAuctionsBtn = document.getElementById('refreshActiveAuctionsBtn');
    const formAddAuction = document.getElementById('formAddAuction');
    const myWonAuctionListContainer = document.getElementById('myWonAuctionListContainer');
    const allFinishedAuctionListContainer = document.getElementById('allFinishedAuctionListContainer');
    const recentlyFinishedItemsListDiv = document.getElementById('recentlyFinishedItemsList');
    const bidWinnersListDiv = document.getElementById('bidWinnersList');

    const bidModal = document.getElementById('bidModal');
    const closeBidModalBtn = document.getElementById('closeBidModal');
    const formPlaceBid = document.getElementById('formPlaceBid');
    const bidModalItemIdInput = document.getElementById('bidModalItemId');
    const bidModalItemNameSpan = document.getElementById('bidModalItemName');
    const bidModalItemImage = document.getElementById('bidModalItemImage');
    const bidModalItemDescription = document.getElementById('bidModalItemDescription');
    const bidModalCurrentBidSpan = document.getElementById('bidModalCurrentBid');
    const bidModalCurrentBidderSpan = document.getElementById('bidModalCurrentBidder');
    const bidModalEndTimeSpan = document.getElementById('bidModalEndTime');
    const cancelBidButton = document.getElementById('cancelBidButton');


    async function fetchActiveAuctions() {
        if (!activeAuctionListContainer) return;
        activeAuctionListContainer.innerHTML = '<p class="loading-text">Loading active auctions...</p>'; // Keep visual loader
        try {
            const response = await fetch('auction-servlet?action=listActive');
            if (!response.ok) throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            const auctions = await response.json();
            renderAuctions(auctions, activeAuctionListContainer, 'activeAuctionMessage', true);
        } catch (error) {
            console.error("Error fetching active auctions:", error);
            notyf.error(`Failed to load active auctions: ${error.message}`);
            if(activeAuctionListContainer) activeAuctionListContainer.innerHTML = '<p>Could not load auctions.</p>';
        }
    }
    if (refreshActiveAuctionsBtn) refreshActiveAuctionsBtn.addEventListener('click', fetchActiveAuctions);

    if (formAddAuction) {
        formAddAuction.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(formAddAuction);
            formData.append('action', 'create');

            console.log("Submitting 'Create Auction' form. FormData entries:");
            for(var pair of formData.entries()) {
                console.log(pair[0]+ ': '+ (pair[1] instanceof File ? pair[1].name : pair[1]));
            }

            notyf.open({ type: 'info', message: 'Creating auction...' });
            try {
                const response = await fetch('auction-servlet', {
                    method: 'POST',
                    body: formData
                });
                const result = await response.json();
                if (result.success) {
                    notyf.success(`Auction created! ID: ${result.itemId}`);
                    formAddAuction.reset();
                    document.getElementById('imagePreview').style.display = 'none';
                    fetchActiveAuctions();
                } else {
                    notyf.error(`Error: ${result.message || 'Could not create auction.'}`);
                }
            } catch (error) {
                console.error("Error creating auction:", error)
                notyf.error(`Request failed: ${error.message}`);
            }
        });

        const itemPictureInput = document.getElementById('itemPicture');
        const imagePreview = document.getElementById('imagePreview');
        if (itemPictureInput && imagePreview) {
            itemPictureInput.addEventListener('change', function() {
                const file = this.files[0];
                if (file) {
                    const reader = new FileReader();
                    reader.onload = function(e) {
                        imagePreview.src = e.target.result;
                        imagePreview.style.display = 'block';
                    }
                    reader.readAsDataURL(file);
                } else {
                    imagePreview.src = '#';
                    imagePreview.style.display = 'none';
                }
            });
        }
    }

    async function fetchMyWonItems() {
        const userId = localStorage.getItem('loggedInUserId');
        if (!userId) {
            notyf.error('Please log in to see your won items.');
            if(myWonAuctionListContainer) myWonAuctionListContainer.innerHTML = '';
            return;
        }
        if (!myWonAuctionListContainer) return;
        myWonAuctionListContainer.innerHTML = '<p class="loading-text">Loading your won auctions...</p>';
        try {
            const response = await fetch(`auction-servlet?action=listWon`);
            if (!response.ok) throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            const auctions = await response.json();
            renderAuctions(auctions, myWonAuctionListContainer, 'myWonAuctionMessage', false);
        } catch (error) {
            console.error("Error fetching won auctions:", error);
            notyf.error(`Failed to load won auctions: ${error.message}`);
            if(myWonAuctionListContainer) myWonAuctionListContainer.innerHTML = '<p>Could not load auctions.</p>';
        }
    }

    async function fetchAllFinishedAuctions() {
        if (!allFinishedAuctionListContainer) return;
        allFinishedAuctionListContainer.innerHTML = '<p class="loading-text">Loading all finished auctions...</p>';
        try {
            const response = await fetch('auction-servlet?action=listFinished');
            if (!response.ok) throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            const auctions = await response.json();
            renderAuctions(auctions, allFinishedAuctionListContainer, 'allFinishedAuctionMessage', false);
        } catch (error) {
            console.error("Error fetching all finished auctions:", error);
            notyf.error(`Failed to load finished auctions: ${error.message}`);
            if(allFinishedAuctionListContainer) allFinishedAuctionListContainer.innerHTML = '<p>Could not load auctions.</p>';
        }
    }

    async function fetchRecentlyFinishedItems() {
        if (!recentlyFinishedItemsListDiv) return;
        recentlyFinishedItemsListDiv.innerHTML = '<p><em>Loading recently finished...</em></p>';
        try {
            const response = await fetch('auction-servlet?action=listRecentlyFinished&count=3');
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const items = await response.json();
            renderSidebarItemList(items, recentlyFinishedItemsListDiv, "No recently finished items.");
        } catch (error) {
            console.error("Error fetching recently finished items:", error);
            if(recentlyFinishedItemsListDiv) recentlyFinishedItemsListDiv.innerHTML = '<p><em>Could not load items.</em></p>';
        }
    }

    async function fetchTopWinners() {
        if (!bidWinnersListDiv) return;
        bidWinnersListDiv.innerHTML = '<p><em>Loading top winners...</em></p>';
        try {
            const response = await fetch('auction-servlet?action=listTopWinners&count=3');
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const winners = await response.json();
            renderSidebarWinnerList(winners, bidWinnersListDiv, "No winners yet.");
        } catch (error) {
            console.error("Error fetching top winners:", error);
            if(bidWinnersListDiv) bidWinnersListDiv.innerHTML = '<p><em>Could not load winners.</em></p>';
        }
    }

    function renderSidebarItemList(items, container, emptyMessage) {
        if (!container) return;
        container.innerHTML = '';
        if (!items || items.length === 0) {
            container.innerHTML = `<p class="empty-list-message"><em>${emptyMessage}</em></p>`;
            return;
        }
        const ul = document.createElement('ul');
        items.forEach(item => {
            const li = document.createElement('li');
            li.innerHTML = `<strong>${escapeHtml(item.name || 'N/A')}</strong> 
                            <small>(ID: ${escapeHtml(item.itemId)})</small><br>
                            <small>Winner: ${escapeHtml(item.currentHighestBidderUserId || 'N/A')} at $${escapeHtml(item.currentHighestBid || '0.00')}</small>`;
            ul.appendChild(li);
        });
        container.appendChild(ul);
    }

    function renderSidebarWinnerList(winners, container, emptyMessage) {
        if (!container) return;
        container.innerHTML = '';
        if (!winners || winners.length === 0) {
            container.innerHTML = `<p class="empty-list-message"><em>${emptyMessage}</em></p>`;
            return;
        }
        const ol = document.createElement('ol');
        winners.forEach(winner => {
            const li = document.createElement('li');
            li.textContent = `${escapeHtml(winner.userId)} (${winner.auctionsWonCount} win${winner.auctionsWonCount > 1 ? 's' : ''})`;
            ol.appendChild(li);
        });
        container.appendChild(ol);
    }


    async function openBidModal(itemId) {
        if (!bidModal || !bidModalItemIdInput || !bidModalItemNameSpan) return;

        const loggedInUserId = localStorage.getItem('loggedInUserId');
        if (!loggedInUserId) {
            notyf.error('Please log in to place a bid.');
            window.location.href = 'login.html';
            return;
        }


        bidModal.style.display = 'flex';

        try {
            const response = await fetch(`auction-servlet?action=getItemDetails&itemId=${encodeURIComponent(itemId)}`);
            if (!response.ok) throw new Error(`HTTP ${response.status}: Failed to load item details.`);
            const item = await response.json();

            if (item && item.itemId) {
                bidModalItemIdInput.value = item.itemId;
                bidModalItemNameSpan.textContent = item.name || "N/A";
                if(bidModalItemImage) bidModalItemImage.src = item.imageUrl || 'images/placeholder.png';
                if(bidModalItemDescription) bidModalItemDescription.textContent = item.description || "No description available.";
                if(bidModalCurrentBidSpan) bidModalCurrentBidSpan.textContent = item.currentHighestBid || "0.00";
                if(bidModalCurrentBidderSpan) bidModalCurrentBidderSpan.textContent = item.currentHighestBidderUserId || "N/A";
                if(bidModalEndTimeSpan) bidModalEndTimeSpan.textContent = item.auctionEndTime ? new Date(item.auctionEndTime).toLocaleString() : "N/A";

                const bidAmountInput = document.getElementById('bidAmount');
                bidAmountInput.value = '';
                const minBid = (parseFloat(item.currentHighestBid || 0) + 0.01).toFixed(2);
                bidAmountInput.min = minBid;
                bidAmountInput.placeholder = `Minimum bid $${minBid}`;
            } else {
                throw new Error(item.message || "Item details not found or invalid.");
            }
        } catch (error) {
            console.error("Error fetching item details for modal:", error);
            notyf.error(error.message);
        }
    }


    if (closeBidModalBtn) closeBidModalBtn.onclick = () => { if(bidModal) bidModal.style.display = "none"; };
    if (cancelBidButton) cancelBidButton.onclick = () => { if(bidModal) bidModal.style.display = "none"; };
    window.onclick = (event) => {
        if (event.target == bidModal) {
            bidModal.style.display = "none";
        }
    };

    if (formPlaceBid) {
        formPlaceBid.addEventListener('submit', async (e) => {
            e.preventDefault();

            const itemIdValue = document.getElementById('bidModalItemId').value;
            const amountValue = document.getElementById('bidAmount').value;

            const params = new URLSearchParams();
            params.append('itemId', itemIdValue);
            params.append('amount', amountValue);
            params.append('action', 'placeBid');

            notyf.open({ type: 'info', message: 'Placing bid...' });
            try {
                const response = await fetch('auction-servlet', { method: 'POST', body: params });
                const result = await response.json();
                if (result.success) {
                    formPlaceBid.reset();

                    if (result.updatedItem) {
                        if(bidModalCurrentBidSpan) bidModalCurrentBidSpan.textContent = result.updatedItem.currentHighestBid;
                        if(bidModalCurrentBidderSpan) bidModalCurrentBidderSpan.textContent = result.updatedItem.currentHighestBidderUserId || "N/A";
                        const newMinBid = (parseFloat(result.updatedItem.currentHighestBid) + 0.01).toFixed(2);
                        document.getElementById('bidAmount').min = newMinBid;
                        document.getElementById('bidAmount').placeholder = `Minimum bid $${newMinBid}`;

                        notyf.success('Your bid is now the highest!');
                        updateAuctionCardInList(result.updatedItem);
                    }
                } else {
                    notyf.error(`Bid failed: ${result.message || 'Could not place bid.'}`);
                }
            } catch (error) {
                console.error("Error placing bid:", error)
                notyf.error(`Request failed: ${error.message}`);
            }
        });
    }

    function updateAuctionCardInList(updatedItem) {
        if (!updatedItem || !updatedItem.itemId) {
            console.warn("updateAuctionCardInList called with invalid item data:", updatedItem);
            return;
        }

        const card = document.querySelector(`.auction-item-card[data-item-id="${updatedItem.itemId}"]`);
        const isActive = updatedItem.active === true;

        console.log("Updating card (ID: " + updatedItem.itemId + "). Received active status:",
            updatedItem.active, "(Type:", typeof updatedItem.active, "), Interpreted as:", isActive);

        if (!isActive) {
            if (card && activeAuctionListContainer.contains(card)) {
                console.log("Item " + updatedItem.itemId + " is NOT active. Removing card from active list.");
                card.remove();
            }
            fetchRecentlyFinishedItems();
            fetchTopWinners();
            if (document.getElementById('allFinishedBidsPage').style.display !== 'none') {
                fetchAllFinishedAuctions();
            }
            return;
        }

        if (card) {
            console.log("Item " + updatedItem.itemId + " is active. Updating card details.");
            const cardCurrentBidSpan = card.querySelector(`#bid-${updatedItem.itemId}`);
            const cardCurrentBidderSpan = card.querySelector(`#bidder-${updatedItem.itemId}`);
            const detailsDiv = card.querySelector('.auction-item-details');
            let statusElP = null;

            if (detailsDiv) {
                const paragraphs = detailsDiv.querySelectorAll('p');
                paragraphs.forEach(p => {
                    const strongTag = p.querySelector('strong');
                    if (strongTag && strongTag.textContent.trim() === "Status:") {
                        statusElP = p;
                    }
                });
            }

            if(cardCurrentBidSpan) cardCurrentBidSpan.textContent = escapeHtml(updatedItem.currentHighestBid || "0.00");
            if(cardCurrentBidderSpan) cardCurrentBidderSpan.textContent = escapeHtml(updatedItem.currentHighestBidderUserId || "N/A");

            if (statusElP) {
                statusElP.innerHTML = `<strong>Status:</strong> Active`;
            }

            let bidButton = card.querySelector('.bid-on-item-btn');
            if (detailsDiv) {
                const itemNameForButton = updatedItem.name || 'Item';

                if (!bidButton) {
                    console.log("Item " + updatedItem.itemId + " is active and button missing. Re-adding bid button.");
                    bidButton = document.createElement('button');
                    bidButton.className = 'action-button bid-on-item-btn';
                    bidButton.setAttribute('data-item-id', updatedItem.itemId);
                    bidButton.setAttribute('data-item-name', escapeHtml(itemNameForButton));
                    bidButton.innerHTML = '<i class="fas fa-hand-paper"></i> Bid Now';
                    bidButton.addEventListener('click', (e) => {
                        const itemId = e.currentTarget.getAttribute('data-item-id');
                        openBidModal(itemId);
                    });
                    detailsDiv.appendChild(bidButton);
                } else {
                    bidButton.setAttribute('data-item-name', escapeHtml(itemNameForButton));
                }
            }
        } else if (isActive && updatedItem.updateType === 'AUCTION_CREATED') {
            console.log("New auction created (event type: " + updatedItem.updateType + "), refreshing active auctions list.");
            fetchActiveAuctions();
        } else {
            console.log("Card for item " + updatedItem.itemId + " not found for update, or item is not active and not a creation event.");
        }
    }

    function renderAuctions(auctions, container, messageAreaId, showBidButton) {
        if (!container) return;
        container.innerHTML = '';
        if (!auctions || auctions.length === 0) {
            container.innerHTML = '<p>No auctions to display.</p>';
            return;
        }
        auctions.forEach(auction => {
            const itemCard = document.createElement('div');
            itemCard.className = 'auction-item-card';
            itemCard.setAttribute('data-item-id', auction.itemId);

            let bidButtonHtml = '';
            const itemNameForButton = auction.name || auction.itemName || 'Item';
            const isActive = auction.active === true;

            if (showBidButton && isActive) {
                bidButtonHtml = `<button class="action-button bid-on-item-btn" data-item-id="${auction.itemId}" data-item-name="${escapeHtml(itemNameForButton)}"><i class="fas fa-hand-paper"></i> Bid Now</button>`;
            }

            const imageUrl = auction.imageUrl ? auction.imageUrl : 'images/placeholder.png';

            itemCard.innerHTML = `
                <div class="auction-item-image-wrapper">
                    <img src="${escapeHtml(imageUrl)}" alt="${escapeHtml(auction.name || 'Auction Item')}" 
                         onerror="this.onerror=null;this.src='images/placeholder.png';">
                </div>
                <div class="auction-item-details">
                    <h4>${escapeHtml(auction.name || 'Unnamed Item')}</h4>
                    <p class="item-id-display">(ID: ${escapeHtml(auction.itemId || 'N/A')})</p>
                    <p><strong>Seller:</strong> ${escapeHtml(auction.sellerUserId || 'N/A')}</p>
                    <p class="price-info"><strong>Starting:</strong> $${escapeHtml(auction.startingPrice || '0.00')}</p>
                    <p class="price-info">
                        <strong>Current Bid: $</strong><span id="bid-${escapeHtml(auction.itemId)}">${escapeHtml(auction.currentHighestBid || '0.00')}</span>
                        (By: <span id="bidder-${escapeHtml(auction.itemId)}">${escapeHtml(auction.currentHighestBidderUserId || 'N/A')}</span>)
                    </p>
                    <p class="time-left"><strong>Ends:</strong> ${auction.auctionEndTime ? new Date(auction.auctionEndTime).toLocaleString() : 'N/A'}</p>
                    <p><strong>Status:</strong> ${isActive ? 'Active' : 'Ended'}</p>
                    ${bidButtonHtml} 
                </div>
            `;
            container.appendChild(itemCard);
        });

        if(showBidButton) {
            document.querySelectorAll('.bid-on-item-btn').forEach(button => {
                button.addEventListener('click', (e) => {
                    const currentButton = e.currentTarget;
                    const itemId = currentButton.getAttribute('data-item-id');
                    openBidModal(itemId);
                });
            });
        }
    }

    function escapeHtml(unsafe) {
        if (unsafe === null || typeof unsafe === 'undefined') return '';
        if (typeof unsafe !== 'string') unsafe = String(unsafe);
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function initializeRealtimeUpdates() {
        console.log("Initializing real-time updates using WebSockets...");

        const appName = "/auction-app";
        const wsProtocol = window.location.protocol === "https:" ? "wss:" : "ws:";
        const wsUrl = `${wsProtocol}//${window.location.host}${appName}/auction-websocket`;

        let socket;

        function connectWebSocket() {
            socket = new WebSocket(wsUrl);

            socket.onopen = () => {
                console.log("WebSocket connection established to: " + wsUrl);
                notyf.open({ type: 'info', message: 'Real-time updates connected!' });
            };

            socket.onmessage = (event) => {
                try {
                    const auctionUpdate = JSON.parse(event.data);
                    console.log("WebSocket 'auctionUpdate' Received:", auctionUpdate);
                    const loggedInUserId = localStorage.getItem('loggedInUserId');

                    if (auctionUpdate && auctionUpdate.updateType === 'AUCTION_CREATED') {
                        console.log("AUCTION_CREATED event received via WebSocket. Refreshing active auctions list.");
                        fetchActiveAuctions();
                        fetchRecentlyFinishedItems();
                        fetchTopWinners();
                    } else if (auctionUpdate && auctionUpdate.itemId) {
                        const isActiveFromServer = auctionUpdate.active === true;
                        const itemDataForUpdate = {...auctionUpdate, active: isActiveFromServer };

                        updateAuctionCardInList(itemDataForUpdate);

                        if (bidModal && bidModal.style.display === 'flex' &&
                            bidModalItemIdInput && bidModalItemIdInput.value === auctionUpdate.itemId) {

                            if(bidModalCurrentBidSpan) bidModalCurrentBidSpan.textContent = auctionUpdate.currentHighestBid;
                            if(bidModalCurrentBidderSpan) bidModalCurrentBidderSpan.textContent = auctionUpdate.currentHighestBidderUserId || "N/A";

                            const bidAmountInput = document.getElementById('bidAmount');
                            if (bidAmountInput) {
                                const newMinBid = (parseFloat(auctionUpdate.currentHighestBid || 0) + 0.01).toFixed(2);
                                bidAmountInput.min = newMinBid;
                                bidAmountInput.placeholder = `Minimum bid $${newMinBid}`;
                            }
                            if (loggedInUserId !== auctionUpdate.currentHighestBidderUserId) {
                                notyf.open({ type: 'info', message: `Item '${escapeHtml(auctionUpdate.name || auctionUpdate.itemName)}' updated by another user!` });
                            }
                        }
                    } else if (auctionUpdate && auctionUpdate.status === "WebSocket connected successfully to auction updates!") {
                        console.log("Received connection confirmation from WebSocket server.");
                    }

                } catch (e) {
                    console.error("Error processing WebSocket message:", e, "Data:", event.data);
                }
            };

            socket.onclose = (event) => {
                console.log("WebSocket connection closed.", event.reason, "Code:", event.code);
                notyf.error('Real-time updates disconnected. Attempting to reconnect...');
                setTimeout(connectWebSocket, 5000);
            };

            socket.onerror = (error) => {
                console.error("WebSocket error:", error);
                notyf.error('Real-time connection error.');
            };
        }

        connectWebSocket();
    }

    if (window.location.pathname.includes('index.html') || window.location.pathname.endsWith('/auction-app/') || window.location.pathname.endsWith('/auction-app/index.html')) {
        initializeRealtimeUpdates();
        const defaultPageLink = document.querySelector('.sidebar-nav a[data-page="viewAllAuctions"]');
        if (defaultPageLink) {
            defaultPageLink.click();
        } else {
            fetchActiveAuctions();
        }
        fetchRecentlyFinishedItems();
        fetchTopWinners();
    }
});
