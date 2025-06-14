document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const notyf = new Notyf({
        duration: 4000,
        position: { x: 'right', y: 'top' },
        types: [
            { type: 'info', background: '#57aeff', icon: false }
        ]
    });
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(loginForm);
            const params = new URLSearchParams();
            for (const pair of formData) { params.append(pair[0], pair[1]); }
            params.append('action', 'login');
            notyf.open({ type: 'info', message: 'Attempting login...' });

            try {
                const response = await fetch('user-servlet', { method: 'POST', body: params });
                const result = await response.json();
                if (result.success && result.userId) {
                    notyf.success('Login successful! Redirecting...');
                    localStorage.setItem('loggedInUserId', result.userId);
                    localStorage.setItem('loggedInUserEmail', result.email || '');
                    window.location.href = 'index.html';
                } else {
                    localStorage.removeItem('loggedInUserId');
                    localStorage.removeItem('loggedInUserEmail');
                    notyf.error(`Login failed: ${result.message || 'Invalid credentials.'}`);
                }
            } catch (error) {
                localStorage.removeItem('loggedInUserId');
                localStorage.removeItem('loggedInUserEmail');
                notyf.error(`Error: ${error.message}`);
            }
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const password = document.getElementById('regPassword').value;
            const confirmPassword = document.getElementById('regConfirmPassword').value;

            if (password !== confirmPassword) {
                notyf.error('Passwords do not match!');
                return;
            }

            const formData = new FormData(registerForm);
            const params = new URLSearchParams();
            for (const pair of formData) { params.append(pair[0], pair[1]); }
            params.append('action', 'register');
            params.delete('confirmPassword');
            notyf.open({ type: 'info', message: 'Registering user...' });

            try {
                const response = await fetch('user-servlet', { method: 'POST', body: params });
                const result = await response.json();
                if (result.success) {
                    notyf.success('Registration successful! Please login.');
                    setTimeout(() => { window.location.href = 'login.html'; }, 1500);
                } else {
                    notyf.error(`Registration failed: ${result.message || 'Could not register user.'}`);
                }
            } catch (error) {
                notyf.error(`Error: ${error.message}`);
            }
        });
    }
});
