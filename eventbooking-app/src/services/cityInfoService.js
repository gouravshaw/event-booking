import axios from 'axios';

// External API Service base URL
const API_URL = '/external';

// Function to get city information
export const getCityInfo = async (cityName) => {
  try {
    const response = await axios.get(`${API_URL}/api/city/info`, {
      params: { name: cityName }
    });
    return response.data;
  } catch (error) {
    console.error('Error:', error);
    return { error: 'Could not load city information' };
  }
};