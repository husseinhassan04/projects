import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import './StudentProfile.css';

const Profile = () => {
    const [profileData, setProfileData] = useState(null);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const response = await fetch('/getProfileInfos', {
                    method: 'GET',
                    credentials: 'include',
                });

                if (!response.ok) {
                    if (response.status === 401) {
                        navigate('/');
                    }
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                const data = await response.json();
                setProfileData(data);
            } catch (err) {
                console.error('Error fetching profile data:', err);
                setError(`Failed to load profile information: ${err.message}`);
            }
        };

        fetchProfile();
    }, [navigate]);

    const handleChangePassword = () => {
        navigate('/change-password'); 
    };

    if (error) {
        return (
            <div className="alert alert-danger mt-5 text-center">
                {error}
            </div>
        );
    }

    if (!profileData) {
        return (
            <div className="spinner-border text-primary mt-5" role="status">
                <span className="visually-hidden">Loading...</span>
            </div>
        );
    }

    return (
        <div className="container" id="profile">
            <div className="card shadow-lg p-4 rounded-3 profile-card">
                <div className="card-header text-center">
                    <h2 className="text-primary">Profile</h2>
                </div>
                <div className="card-body">
                    <div className="profile-info mb-4">
                        <p><strong>Name:</strong> {profileData.firstName} {profileData.lastName}</p>
                        <p><strong>Email:</strong> {profileData.email}</p>
                        <p><strong>Role:</strong> {profileData.role}</p>
                    </div>
                    <div className="d-flex justify-content-center">
                        <button className="btn btn-primary btn-lg" onClick={handleChangePassword}>
                            Change Password
                        </button>
                    </div>
                </div>
            </div>
            <footer className="footer mt-5 text-center">
                <p>&copy; 2024 Student Profile. All Rights Reserved.</p>
            </footer>
        </div>
    );
};

export default Profile;