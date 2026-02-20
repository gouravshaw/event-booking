import React, { useState, useEffect } from 'react';
import { getUserProfile, isAdmin } from '../../services/authService';

const Profile = () => {
  // State for user profile
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isAdminUser, setIsAdminUser] = useState(false);
  
  // Load profile on component mount
  useEffect(() => {
    fetchUserProfile();
    checkAdminStatus();
  }, []);
  
  // Fetch user profile from API
  const fetchUserProfile = async () => {
    setLoading(true);
    try {
      const data = await getUserProfile();
      
      // Parse the JSON data if it's a string
      const parsedProfile = typeof data === 'string' ? JSON.parse(data) : data;
      
      setProfile(parsedProfile);
      setError(null);
    } catch (err) {
      setError('Failed to load your profile. Please try again later.');
      setProfile(null);
    } finally {
      setLoading(false);
    }
  };
  
  // Check if user is admin
  const checkAdminStatus = async () => {
    try {
      const adminStatus = await isAdmin();
      setIsAdminUser(adminStatus);
    } catch (err) {
      console.error("Error checking admin status:", err);
      setIsAdminUser(false);
    }
  };
  
  // Format date string to be more readable
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString();
    } catch (e) {
      return dateString;
    }
  };
  
  return (
    <div className="container my-5">
      <div className="row justify-content-center">
        <div className="col-md-8">
          <div className="card">
            <div className="card-header bg-primary text-white">
              <h3 className="mb-0">My Profile</h3>
            </div>
            
            <div className="card-body">
              {/* Error Message */}
              {error && (
                <div className="alert alert-danger" role="alert">
                  {error}
                </div>
              )}
              
              {/* Loading Indicator */}
              {loading ? (
                <div className="text-center my-4">
                  <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                  </div>
                  <p className="mt-2">Loading your profile...</p>
                </div>
              ) : (
                /* Profile Content */
                profile && (
                  <div>
                    {isAdminUser && (
                      <div className="alert alert-info mb-4">
                        <strong>Admin Account</strong> - You have administrator privileges
                      </div>
                    )}
                    
                    <div className="row mb-4">
                      <div className="col-md-4 text-md-end">
                        <strong>Full Name:</strong>
                      </div>
                      <div className="col-md-8">
                        {profile.fullName}
                      </div>
                    </div>
                    
                    <div className="row mb-4">
                      <div className="col-md-4 text-md-end">
                        <strong>Email:</strong>
                      </div>
                      <div className="col-md-8">
                        {profile.email}
                      </div>
                    </div>
                    
                    <div className="row mb-4">
                      <div className="col-md-4 text-md-end">
                        <strong>Phone Number:</strong>
                      </div>
                      <div className="col-md-8">
                        {profile.phoneNumber}
                      </div>
                    </div>
                    
                    <div className="row mb-4">
                      <div className="col-md-4 text-md-end">
                        <strong>Role:</strong>
                      </div>
                      <div className="col-md-8">
                        {profile.role === 'ADMIN' ? (
                          <span className="badge bg-danger">Administrator</span>
                        ) : (
                          <span className="badge bg-info">User</span>
                        )}
                      </div>
                    </div>
                    
                    <div className="row mb-4">
                      <div className="col-md-4 text-md-end">
                        <strong>Account Created:</strong>
                      </div>
                      <div className="col-md-8">
                        {formatDate(profile.createdAt)}
                      </div>
                    </div>
                    
                    <div className="row">
                      <div className="col-md-4 text-md-end">
                        <strong>Last Updated:</strong>
                      </div>
                      <div className="col-md-8">
                        {formatDate(profile.updatedAt)}
                      </div>
                    </div>
                  </div>
                )
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;