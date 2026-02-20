import React, { useState, useEffect } from 'react';
import { getGoogleMapsApiKey } from '../../services/mapService';

const DirectionsMap = ({ event }) => {
  const [apiKey, setApiKey] = useState('');
  const [origin, setOrigin] = useState('');
  const [showDirections, setShowDirections] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Fetch API key on component mount
  useEffect(() => {
    fetchApiKey();
  }, []);
  
  // Function to fetch the API key from backend
  const fetchApiKey = async () => {
    try {
      const key = await getGoogleMapsApiKey();
      setApiKey(key);
      setError(null);
    } catch (err) {
      setError('Failed to load directions. Please try again later.');
    } finally {
      setLoading(false);
    }
  };
  
  // Create destination string from event data
  const destinationString = event ? `${event.venue}, ${event.address}, ${event.city}, ${event.country}, ${event.postcode}` : '';
  
  // Handle input change
  const handleOriginChange = (e) => {
    setOrigin(e.target.value);
  };
  
  // Show directions when button is clicked
  const handleGetDirections = () => {
    if (origin.trim() === '') {
      alert('Please enter your starting location');
      return;
    }
    setShowDirections(true);
  };
  
  // Show loading state
  if (loading) {
    return (
      <div className="card mb-4">
        <div className="card-header bg-primary text-white">
          <h5 className="mb-0">Get Directions</h5>
        </div>
        <div className="card-body text-center">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="mt-2">Loading directions...</p>
        </div>
      </div>
    );
  }
  
  // Show error state
  if (error) {
    return (
      <div className="card mb-4">
        <div className="card-header bg-primary text-white">
          <h5 className="mb-0">Get Directions</h5>
        </div>
        <div className="card-body">
          <div className="alert alert-danger">
            {error}
          </div>
        </div>
      </div>
    );
  }
  
  return (
    <div className="card mb-4">
      <div className="card-header bg-primary text-white">
        <h5 className="mb-0">Get Directions</h5>
      </div>
      <div className="card-body">
        <div className="mb-3">
          <label htmlFor="origin" className="form-label">Starting Location</label>
          <div className="input-group">
            <input
              type="text"
              className="form-control"
              id="origin"
              placeholder="Enter your starting location"
              value={origin}
              onChange={handleOriginChange}
            />
            <button 
              className="btn btn-primary"
              onClick={handleGetDirections}
            >
              Get Directions
            </button>
          </div>
          <div className="form-text">
            Example: "London, UK" or "10 Downing Street, London"
          </div>
        </div>
        
        {showDirections && origin.trim() !== '' && apiKey && (
          <div className="directions-map" style={{ height: '400px', width: '100%' }}>
            <iframe
              title="Directions to Event"
              width="100%"
              height="100%"
              frameBorder="0"
              style={{ border: 0 }}
              src={`https://www.google.com/maps/embed/v1/directions?key=${apiKey}&origin=${encodeURIComponent(origin)}&destination=${encodeURIComponent(destinationString)}&mode=driving`}
              allowFullScreen
            ></iframe>
          </div>
        )}
      </div>
    </div>
  );
};

export default DirectionsMap;