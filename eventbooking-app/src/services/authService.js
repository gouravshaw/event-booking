import axios from 'axios';
import { auth } from '../firebase';
import {
  signInWithEmailAndPassword,
  signOut,
  onAuthStateChanged
} from 'firebase/auth';

// User Service Base URL
const USER_API_URL = '/user';

// Function to register a new user
// New version (partial)
export const registerUser = async (email, password, fullName, phoneNumber, adminSecret = null) => {
  try {
    // Create request data
    const requestData = {
      email: email,
      password: password,
      fullName: fullName,
      phoneNumber: phoneNumber
    };

    // Only add admin secret if provided
    if (adminSecret && adminSecret.trim() !== '') {
      requestData.adminSecret = adminSecret;
    }

    console.log('Registration data:', JSON.stringify(requestData));

    // Send registration request to backend
    const response = await fetch(`${USER_API_URL}/api/public/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestData)
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('Registration response:', response.status, errorText);
      throw new Error(`Registration failed: ${response.status} ${errorText || response.statusText}`);
    }

    // Important: Do not sign in the user automatically after registration
    // No Firebase sign-in, no token storage

    const data = await response.json();
    console.log('Registration successful:', data);
    return data;
  } catch (error) {
    console.error("Registration error:", error);
    throw new Error(error.message || 'Registration failed');
  }
};

// Function to login user
export const loginUser = async (email, password) => {
  try {
    const userCredential = await signInWithEmailAndPassword(auth, email, password);
    const user = userCredential.user;

    // Get the ID token
    const token = await user.getIdToken();
    localStorage.setItem('userToken', token);
    localStorage.setItem('userEmail', email);

    return { token };
  } catch (error) {
    console.error("Login error:", error);
    throw new Error(error.message || 'Login failed');
  }
};

// Function to check if user is logged in
export const isAuthenticated = () => {
  return auth.currentUser !== null || localStorage.getItem('userToken') !== null;
};

// Function to check if user is admin
export const isAdmin = async () => {
  try {
    const userProfile = await getUserProfile();
    console.log('User profile for admin check:', userProfile);

    // Check if userProfile exists and has role property
    if (!userProfile) {
      console.log('No user profile found');
      return false;
    }

    // Check role (could be either 'ADMIN' or 'admin' depending on your API)
    const isAdminUser = userProfile.role === 'ADMIN' || userProfile.role === 'admin';
    console.log('Is admin based on role:', isAdminUser);

    return isAdminUser;
  } catch (error) {
    console.error("Admin check error:", error);
    return false;
  }
};

// Function to logout user
export const logout = async () => {
  try {
    await signOut(auth);
    localStorage.removeItem('userToken');
    localStorage.removeItem('userEmail');
  } catch (error) {
    console.error("Logout error:", error);
  }
};

// Function to get the auth token
export const getToken = async () => {
  try {
    const currentUser = auth.currentUser;
    if (currentUser) {
      return await currentUser.getIdToken(true);
    }
    return localStorage.getItem('userToken');
  } catch (error) {
    console.error("Get token error:", error);
    return null;
  }
};

// Get current user's profile with proper error handling
export const getUserProfile = async () => {
  try {
    const token = await getToken();
    if (!token) throw new Error('Not authenticated');

    // Try multiple possible URL patterns to find the right one
    const urls = [
      `${USER_API_URL}/api/user/profile`,
      `${USER_API_URL}/api/profile`,
      `${USER_API_URL}/user/profile`
    ];

    let response = null;

    // Try each possible URL
    for (const url of urls) {
      try {
        console.log(`Trying profile URL: ${url}`);
        response = await axios.get(url, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        // If we got here, the request was successful
        console.log(`Profile found at: ${url}`);
        return response.data;
      } catch (err) {
        console.log(`URL ${url} failed with: ${err.message}`);
        // Continue to the next URL
      }
    }

    // If we get here, all URLs failed
    console.error("All profile URL patterns failed. Using fallback profile.");
    return {
      email: localStorage.getItem('userEmail'),
      role: 'USER',
      fullName: 'User'
    };
  } catch (error) {
    console.error("Get profile error:", error);

    // Provide a fallback profile
    return {
      email: localStorage.getItem('userEmail'),
      role: 'USER',
      fullName: 'User'
    };
  }
};

// Setup auth state listener
export const setupAuthListener = (callback) => {
  return onAuthStateChanged(auth, (user) => {
    callback(user);
  });
};