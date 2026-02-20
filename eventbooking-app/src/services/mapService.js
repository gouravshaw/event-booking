import axios from 'axios';

// External API Service base URL
const API_URL = '/external';

// Function to get the Google Maps API key from the backend
export const getGoogleMapsApiKey = async () => {
  try {
    const response = await axios.get(`${API_URL}/api/maps/key`);
    return response.data.key;
  } catch (error) {
    console.error('Error fetching Google Maps API key:', error);
    throw new Error('Failed to fetch Google Maps API key');
  }
};

// Function to get directions from the backend
export const getDirections = async (origin, destination) => {
  try {
    const response = await axios.get(`${API_URL}/api/maps/directions`, {
      params: {
        origin,
        destination
      }
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching directions:', error);
    throw new Error('Failed to fetch directions');
  }
};