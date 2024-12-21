import React from 'react';
import { Navigate } from 'react-router-dom';

const AdminProtectedRoute = ({ children }) => {
  const isLoggedIn = localStorage.getItem('isLoggedIn');
  const role = localStorage.getItem('role');

  return isLoggedIn && role === 'admin' ? children : <Navigate to="/" replace />;
};


export default AdminProtectedRoute;