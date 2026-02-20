import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { isAuthenticated, logout, isAdmin, getUserProfile, setupAuthListener } from '../../services/authService';

const Navbar = () => {
  const navigate = useNavigate();
  const [auth, setAuth] = useState(isAuthenticated());
  const [admin, setAdmin] = useState(false);
  const [userProfile, setUserProfile] = useState(null);
  
  // Check if user is authenticated and if they are an admin
  useEffect(() => {
    // Set up authentication listener
    const unsubscribe = setupAuthListener(async (user) => {
      setAuth(!!user);
      
      if (user) {
        const adminStatus = await isAdmin();
        setAdmin(adminStatus);
        
        // Get user profile for welcome message
        try {
          const profile = await getUserProfile();
          setUserProfile(profile);
        } catch (err) {
          console.error("Error fetching user profile:", err);
        }
      } else {
        setAdmin(false);
        setUserProfile(null);
      }
    });
    
    // Check admin status and get profile initially
    const initUserData = async () => {
      if (auth) {
        const adminStatus = await isAdmin();
        setAdmin(adminStatus);
        
        try {
          const profile = await getUserProfile();
          setUserProfile(profile);
        } catch (err) {
          console.error("Error fetching user profile:", err);
        }
      }
    };
    
    initUserData();
    
    // Clean up listener on unmount
    return () => unsubscribe();
  }, [auth]);

  const handleLogout = async () => {
    await logout();
    setAuth(false);
    setAdmin(false);
    setUserProfile(null);
    navigate('/login');
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
      <div className="container">
        <Link className="navbar-brand" to="/">
          Event Booking System
        </Link>
        
        <button 
          className="navbar-toggler" 
          type="button" 
          data-bs-toggle="collapse" 
          data-bs-target="#navbarNav"
        >
          <span className="navbar-toggler-icon"></span>
        </button>
        
        <div className="collapse navbar-collapse" id="navbarNav">
          <ul className="navbar-nav me-auto">
            <li className="nav-item">
              <Link className="nav-link" to="/">
                Home
              </Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link" to="/events">
                Events
              </Link>
            </li>
            
            {auth && (
              <li className="nav-item">
                <Link className="nav-link" to="/bookings">
                  My Bookings
                </Link>
              </li>
            )}
            
            {admin && (
              <li className="nav-item">
                <Link className="nav-link bg-success text-white rounded px-3 mx-2" to="/admin/dashboard">
                  Admin Dashboard
                </Link>
              </li>
            )}
          </ul>
          
          {auth && (
            <ul className="navbar-nav ms-auto">
              {userProfile && (
                <li className="nav-item">
                  <span className="nav-link text-light me-3">
                    Welcome, {userProfile.fullName}
                  </span>
                </li>
              )}
              
              <li className="nav-item">
                <Link className="nav-link" to="/profile">
                  Profile
                </Link>
              </li>
              
              <li className="nav-item">
                <button 
                  className="btn btn-link nav-link" 
                  onClick={handleLogout}
                >
                  Logout
                </button>
              </li>
            </ul>
          )}
          
          {!auth && (
            <ul className="navbar-nav ms-auto">
              <li className="nav-item">
                <Link className="nav-link" to="/login">
                  Login
                </Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to="/register">
                  Register
                </Link>
              </li>
            </ul>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;