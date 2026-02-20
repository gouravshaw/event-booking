import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { isAdmin, isAuthenticated } from '../../services/authService';

const AdminRoute = ({ children }) => {
  const [authChecked, setAuthChecked] = useState(false);
  const [isAuthorized, setIsAuthorized] = useState(false);
  const [loading, setLoading] = useState(true); // Add loading state for debugging
  
  useEffect(() => {
    const checkAdminAuth = async () => {
      // First check if user is authenticated
      const auth = isAuthenticated();
      console.log('Is authenticated:', auth);
      
      if (auth) {
        // Then check if user is admin
        try {
          console.log('Checking admin status...');
          const adminStatus = await isAdmin();
          console.log('Is admin:', adminStatus);
          setIsAuthorized(adminStatus);
        } catch (err) {
          console.error('Error checking admin status:', err);
          setIsAuthorized(false);
        }
      } else {
        console.log('Not authenticated, cannot be admin');
        setIsAuthorized(false);
      }
      
      setAuthChecked(true);
      setLoading(false);
    };
    
    // Check admin authentication status
    checkAdminAuth();
  }, []);
  
  // Show loading indicator
  if (loading) {
    return (
      <div className="container text-center mt-5">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
        <p className="mt-2">Checking admin access...</p>
      </div>
    );
  }
  
  // Show nothing while checking authentication
  if (!authChecked) {
    return null;
  }
  
  // Redirect to home if not admin
  if (!isAuthorized) {
    console.log('Not authorized as admin, redirecting to home');
    return <Navigate to="/" />;
  }
  
  // Render children if admin
  console.log('User is admin, rendering admin page');
  return children;
};

export default AdminRoute;