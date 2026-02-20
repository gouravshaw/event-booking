import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { isAuthenticated, setupAuthListener } from '../../services/authService';

const ProtectedRoute = ({ children, adminOnly = false }) => {
  const [authChecked, setAuthChecked] = useState(false);
  const [isAuthorized, setIsAuthorized] = useState(false);
  
  useEffect(() => {
    const checkAuth = async () => {
      const auth = isAuthenticated();
      setIsAuthorized(auth);
      setAuthChecked(true);
    };
    
    // Check authentication status
    checkAuth();
    
    // Set up Firebase auth state listener
    const unsubscribe = setupAuthListener(user => {
      setIsAuthorized(!!user);
      setAuthChecked(true);
    });
    
    // Clean up listener on unmount
    return () => unsubscribe();
  }, [adminOnly]);
  
  // Show nothing while checking authentication
  if (!authChecked) {
    return null;
  }
  
  // Redirect to login if not authenticated
  if (!isAuthorized) {
    return <Navigate to="/login" />;
  }
  
  // Render children if authenticated
  return children;
};

export default ProtectedRoute;