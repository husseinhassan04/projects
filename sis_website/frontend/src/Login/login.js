import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './login.css';

const LoginPage = () => {
    const [id, setId] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');
    const navigate = useNavigate();
    localStorage.clear(); 

    const [loading, setLoading] = useState(false);

    const handleLogin = async (e) => {
        e.preventDefault();
        setMessage('');
        setLoading(true);
        try {
            const response = await fetch('http://localhost:1337/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ id, password }),
                credentials: 'include',
            });

            const data = await response.json();
            const { user, message } = data;
            setMessage(message);
            setLoading(false);

            if (user.role === "admin") {
                localStorage.setItem('isLoggedIn', true);
                localStorage.setItem('role', 'admin');
                sessionStorage.setItem('userId', id);
                navigate('/admin-feed');
            }
            else if (user.role === "user") {
                localStorage.setItem('isLoggedIn', true);
                localStorage.setItem('role', 'student');
                sessionStorage.setItem('userId', id);
                navigate('/student-feed');
            }
        } catch (error) {
            setMessage(
                error.response
                    ? error.response.data.message
                    : 'An unexpected server error occurred'
            );
            setLoading(false);
        }
    };

    return (
        <div className="login-body" id="login-body">
            <div className="login-container" id="login-container">
                <div className="login-card" id="login-card">
                    <h2 className="login-title" id="login-title">HZ University</h2>
                    <form onSubmit={handleLogin} className="login-form" id="login-form">
                        <div className="input-group" id="user-id-group">
                            <label htmlFor="user-id-input">User Id</label>
                            <input
                                id="user-id-input"
                                type="text"
                                value={id}
                                onChange={(e) => setId(e.target.value)}
                                required
                            />
                        </div>
                        <div className="input-group" id="password-group">
                            <label htmlFor="password-input">Password</label>
                            <input
                                id="password-input"
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                            />
                        </div>
                        <button
                            type="submit"
                            className="login-button"
                            id="login-button"
                        >
                            {loading ? 'Logging in...' : 'Log In'}
                        </button>
                        {message && <p className="error-message" id="error-message">{message}</p>}
                    </form>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
