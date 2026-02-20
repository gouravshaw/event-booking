import axios from 'axios';
import { getToken } from './authService';

// Fixed API URL for Docker environment
const API_URL = '/booking';

// Function to create a new booking
export const createBooking = async (eventId, tickets) => {
  try {
    const token = await getToken();
    if (!token) throw new Error('Not authenticated');
    
    console.log('Creating booking for event:', eventId, 'tickets:', tickets);
    
    // Clear request format for better debugging
    const requestData = {
      eventId: Number(eventId),
      tickets: Number(tickets)
    };
    
    console.log('Request data:', requestData);
    
    const response = await axios.post(
      `${API_URL}/api/user/bookings`,
      requestData,
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    console.log('Booking response:', response.data);
    return response.data;
  } catch (error) {
    console.error("Create booking error:", error);
    
    // Better error handling
    let errorMessage = 'Failed to create booking';
    if (error.response) {
      // The request was made and the server responded with a status code
      // that falls out of the range of 2xx
      console.error("Error response data:", error.response.data);
      console.error("Error response status:", error.response.status);
      
      if (error.response.data && error.response.data.error) {
        errorMessage = error.response.data.error;
      } else if (error.response.status === 401) {
        errorMessage = 'Authentication failed. Please log in again.';
      } else if (error.response.status === 400) {
        errorMessage = 'Invalid booking data. Please check your inputs.';
      }
    } else if (error.request) {
      // The request was made but no response was received
      console.error("No response received:", error.request);
      errorMessage = 'No response from server. Please check your connection.';
    } else {
      // Something happened in setting up the request that triggered an Error
      console.error("Request error:", error.message);
    }
    
    throw new Error(errorMessage);
  }
};

// Function to cancel a booking
export const cancelBooking = async (bookingId) => {
  try {
    const token = await getToken();
    if (!token) throw new Error('Not authenticated');
    
    console.log('Cancelling booking:', bookingId);
    
    const response = await axios.put(
      `${API_URL}/api/user/bookings/${bookingId}/cancel`,
      {},
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    console.log('Cancel booking response:', response.data);
    return response.data;
  } catch (error) {
    console.error("Cancel booking error:", error);
    throw new Error(error.response?.data?.error || error.message || 'Failed to cancel booking');
  }
};

// Function to get all bookings for the current user
export const getUserBookings = async () => {
  try {
    const token = await getToken();
    if (!token) throw new Error('Not authenticated');
    
    console.log('Fetching user bookings');
    
    const response = await axios.get(`${API_URL}/api/user/bookings`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    console.log(`Retrieved ${response.data.length} bookings`);
    return response.data;
  } catch (error) {
    console.error("Get user bookings error:", error);
    throw new Error(error.response?.data?.error || error.message || 'Failed to fetch bookings');
  }
};

// Function to get booking details by ID
export const getBookingById = async (bookingId) => {
  try {
    const token = await getToken();
    if (!token) throw new Error('Not authenticated');
    
    console.log('Fetching booking details:', bookingId);
    
    const response = await axios.get(`${API_URL}/api/user/bookings/${bookingId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    return response.data;
  } catch (error) {
    console.error("Get booking error:", error);
    throw new Error(error.response?.data?.error || error.message || 'Failed to fetch booking details');
  }
};

// Admin function to get all bookings in the system
export const getAllBookings = async () => {
  try {
    const token = await getToken();
    if (!token) throw new Error('Not authenticated');
    
    console.log('Fetching all bookings (admin)');
    
    const response = await axios.get(`${API_URL}/api/admin/bookings`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    return response.data;
  } catch (error) {
    console.error("Get all bookings error:", error);
    throw new Error(error.response?.data?.error || error.message || 'Failed to fetch all bookings');
  }
};

// Admin function to delete a booking
export const deleteBooking = async (bookingId) => {
  try {
    const token = await getToken();
    if (!token) throw new Error('Not authenticated');
    
    console.log('Deleting booking (admin):', bookingId);
    
    const response = await axios.delete(`${API_URL}/api/admin/bookings/${bookingId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    return response.data;
  } catch (error) {
    console.error("Delete booking error:", error);
    throw new Error(error.response?.data?.error || error.message || 'Failed to delete booking');
  }
};