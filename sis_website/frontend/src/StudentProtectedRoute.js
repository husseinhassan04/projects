import React from 'react';
import { Navigate } from 'react-router-dom';

const StudentProtectedRoute = ({ children }) => {
    const isLoggedIn = localStorage.getItem('isLoggedIn');
    const role = localStorage.getItem('role');
  
    return isLoggedIn && role === 'student' ? children : <Navigate to="/" replace />;
  };

  export default StudentProtectedRoute;