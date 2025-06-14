document.addEventListener('DOMContentLoaded', () => {
    const userInfoDisplay = document.getElementById('userInfoDisplay');
    const sidebarUserName = document.getElementById('sidebarUserName');
    const sidebarUserEmail = document.getElementById('sidebarUserEmail');
    const logoutButton = document.getElementById('logoutBtn');
    const userAvatar = document.getElementById('userAvatar');

    const userId = localStorage.getItem('loggedInUserId');
    const userEmail = localStorage.getItem('loggedInUserEmail');

    if (userId) {
        if (userInfoDisplay) userInfoDisplay.textContent = `${userId}`;
        if (sidebarUserName) sidebarUserName.textContent = userId;

        if (sidebarUserEmail) {
            if (userEmail && userEmail.trim() !== "") {
                sidebarUserEmail.textContent = userEmail;
                sidebarUserEmail.style.fontStyle = 'normal';
            } else {
                sidebarUserEmail.textContent = 'No email provided';
                sidebarUserEmail.style.fontStyle = 'italic';
            }
        }

        if (userAvatar) {
            userAvatar.src = `https://placehold.co/45x45/3498db/ffffff?text=${userId.substring(0,1).toUpperCase()}`;
            userAvatar.alt = userId;
        }
        if (logoutButton) logoutButton.style.display = 'inline-flex';
    } else {
        if (window.location.pathname.includes('index.html') || window.location.pathname.endsWith('/auction-app/') || window.location.pathname.endsWith('/auction-app/index.html')) {
            if (!window.location.pathname.endsWith('login.html')) {
                console.log("User not logged in (checked by auth-index.js). Redirecting to login.html.");
                window.location.href = 'login.html';
            }
        }
        if (userInfoDisplay) userInfoDisplay.textContent = 'Welcome, Guest!';
        if (logoutButton) logoutButton.style.display = 'none';
    }

    if (logoutButton) {
        logoutButton.addEventListener('click', async () => {
            try {
                const response = await fetch('user-servlet?action=logout', { method: 'POST' });
                const result = await response.json();

                localStorage.removeItem('loggedInUserId');
                localStorage.removeItem('loggedInUserEmail');

                if (result.success) {
                    console.log("Logout successful on server.");
                } else {
                    console.warn('Server logout failed or action not processed:', result.message);
                }
                window.location.href = 'login.html';
            } catch (error) {
                console.error('Logout error:', error);
                localStorage.removeItem('loggedInUserId');
                localStorage.removeItem('loggedInUserEmail');
                window.location.href = 'login.html';
            }
        });
    }
});