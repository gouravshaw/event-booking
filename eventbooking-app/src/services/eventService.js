import axios from 'axios';
import { getToken } from './authService';

// Fix: Use correct base URL
const API_URL = '/event';

// Function to get all events
export const getAllEvents = async () => {
  try {
    const response = await axios.get(`${API_URL}/api/public/events`);
    return response.data;
  } catch (error) {
    console.error("Get events error:", error);
    throw new Error(error.message || 'Failed to fetch events');
  }
};

// Function to get event by ID
export const getEventById = async (eventId) => {
  try {
    const response = await axios.get(`${API_URL}/api/public/events/${eventId}`);
    return response.data;
  } catch (error) {
    console.error("Get event error:", error);
    throw new Error(error.message || 'Failed to fetch event details');
  }
};

// Function to search events
export const searchEvents = async (type = null, city = null, date = null) => {
  try {
    let url = `${API_URL}/api/public/events/search?`;
    
    if (type) url += `type=${encodeURIComponent(type)}&`;
    if (city) url += `city=${encodeURIComponent(city)}&`;
    if (date) url += `date=${encodeURIComponent(date)}`;
    
    const response = await axios.get(url);
    return response.data;
  } catch (error) {
    console.error("Search events error:", error);
    throw new Error(error.message || 'Failed to search events');
  }
};

// Admin function to create a new event
export const createEvent = async (eventData) => {
  try {
    const token = await getToken();
    if (!token) throw new Error('Not authenticated');
    
    console.log('Creating event with data:', eventData);
    console.log('Using token:', token);
    
    const response = await axios.post(`${API_URL}/api/admin/events`, eventData, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    console.log('Create event response:', response);
    return response.data;
  } catch (error) {
    console.error("Create event error:", error);
    throw new Error(error.message || 'Failed to create event');
  }
};

// Admin function to update an event
export const updateEvent = async (eventId, eventData) => {
  try {
    const token = await getToken();
    if (!token) throw new Error('Not authenticated');
    
    console.log('Updating event with ID:', eventId);
    console.log('Update data:', eventData);
    console.log('Using token:', token);
    
    const response = await axios.put(`${API_URL}/api/admin/events/${eventId}`, eventData, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    console.log('Update event response:', response);
    return response.data;
  } catch (error) {
    console.error("Update event error:", error);
    throw new Error(error.message || 'Failed to update event');
  }
};

// Admin function to delete an event
export const deleteEvent = async (eventId) => {
  try {
    const token = await getToken();
    if (!token) throw new Error('Not authenticated');
    
    console.log('Deleting event with ID:', eventId);
    console.log('Using token:', token);
    
    try {
      const response = await axios.delete(`${API_URL}/api/admin/events/${eventId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      console.log('Delete event response:', response);
      return response.data;
    } catch (error) {
      // Check if this is the active bookings error (HTTP 409 Conflict)
      if (error.response && error.response.status === 409) {
        throw new Error(error.response.data.error || "Cannot delete event with active bookings, Cancel the bookings and inform the users first");
      }
      
      throw error;
    }
  } catch (error) {
    console.error("Delete event error:", error);
    throw new Error(error.message || 'Caution!!! ');
  }
};