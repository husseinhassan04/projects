import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const ChangePassword = ({ userId }) => {
    const [oldPassword, setOldPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (newPassword.length < 8) {
            setError('Password must be at least 8 characters long.');
            return;
        }

        try {
            const response = await fetch('http://localhost:1337/changePassword', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ oldPassword, newPassword }),
            });

            const result = await response.json();
            if (!response.ok) {
                throw new Error(result.message || 'Failed to change password');
            }

            setSuccess('Password changed successfully!');
            setTimeout(() => navigate('/profile'), 1000);
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div className="container mt-5">
            <h2 className="text-center">Change Password</h2>
            {error && <div className="alert alert-danger">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}
            <form onSubmit={handleSubmit}>
                <div className="mb-3">
                    <label htmlFor="oldPassword" className="form-label">Old Password</label>
                    <input
                        type="password"
                        className="form-control"
                        id="oldPassword"
                        value={oldPassword}
                        onChange={(e) => setOldPassword(e.target.value)}
                        required
                    />
                </div>
                <div className="mb-3">
                    <label htmlFor="newPassword" className="form-label">New Password</label>
                    <input
                        type="password"
                        className="form-control"
                        id="newPassword"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        required
                    />
                </div>
                <button type="submit" className="btn btn-primary">Change Password</button>
            </form>
        </div>
    );
};

export default ChangePassword;