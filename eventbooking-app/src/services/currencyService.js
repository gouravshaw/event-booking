import axios from 'axios';

// External API Service base URL
const API_URL = '/external';

// Function to get list of available currencies
export const getAvailableCurrencies = async () => {
  try {
    console.log('Calling currency API endpoint for currency list');
    const response = await axios.get(`${API_URL}/api/currency/list`);
    console.log('Currency API response:', response.data);
    return response.data;
  } catch (error) {
    console.error('Error fetching currencies:', error);
    throw new Error('Failed to fetch currencies');
  }
};

// Function to get exchange rates
export const getExchangeRates = async () => {
  try {
    console.log('Calling currency API endpoint for exchange rates');
    const response = await axios.get(`${API_URL}/api/currency/rates`);
    console.log('Exchange rates API response:', response.data);
    return response.data;
  } catch (error) {
    console.error('Error fetching exchange rates:', error);
    throw new Error('Failed to fetch exchange rates');
  }
};

// Function to convert currency
export const convertCurrency = async (amount, targetCurrency) => {
  try {
    console.log(`Converting ${amount} GBP to ${targetCurrency}`);
    const response = await axios.get(`${API_URL}/api/currency/convert`, {
      params: {
        amount,
        targetCurrency
      }
    });
    console.log('Conversion API response:', response.data);
    return response.data;
  } catch (error) {
    console.error('Error converting currency:', error);
    throw new Error('Failed to convert currency');
  }
};