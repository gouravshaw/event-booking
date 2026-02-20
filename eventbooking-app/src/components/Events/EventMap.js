import React, { useState, useEffect } from 'react';
import { getGoogleMapsApiKey } from '../../services/mapService';

const EventMap = ({ event }) => {
  const [apiKey, setApiKey] = useState('');
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
      setError('Failed to load map. Please try again later.');
    } finally {
      setLoading(false);
    }
  };
  
  // Create a location string from event data
  const locationString = event ? `${event.venue}, ${event.address}, ${event.city}, ${event.country}, ${event.postcode}` : '';
  
  // Encode the location for use in the URL
  const encodedLocation = encodeURIComponent(locationString);
  
  // Show loading state
  if (loading) {
    return (
      <div className="card mb-4">
        <div className="card-header bg-primary text-white">
          <h5 className="mb-0">Event Location</h5>
        </div>
        <div className="card-body text-center">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading map...</span>
          </div>
          <p className="mt-2">Loading map...</p>
        </div>
      </div>
    );
  }
  
  // Show error state
  if (error) {
    return (
      <div className="card mb-4">
        <div className="card-header bg-primary text-white">
          <h5 className="mb-0">Event Location</h5>
        </div>
        <div className="card-body">
          <div className="alert alert-danger">
            {error}
          </div>
          <p>
            <strong>{event.venue}</strong><br />
            {event.address}<br />
            {event.city}, {event.country}<br />
            {event.postcode}
          </p>
        </div>
      </div>
    );
  }
  
  return (
    <div className="card mb-4">
      <div className="card-header bg-primary text-white">
        <h5 className="mb-0">Event Location</h5>
      </div>
      <div className="card-body">
        <div className="mb-3">
          <p>
            <strong>{event.venue}</strong><br />
            {event.address}<br />
            {event.city}, {event.country}<br />
            {event.postcode}
          </p>
        </div>
        
        <div className="map-container" style={{ height: '300px', width: '100%' }}>
          <iframe
            title="Event Location"
            width="100%"
            height="100%"
            frameBorder="0"
            style={{ border: 0 }}
            src={`https://www.google.com/maps/embed/v1/place?key=${apiKey}&q=${encodedLocation}`}
            allowFullScreen
          ></iframe>
        </div>
      </div>
    </div>
  );
};

export default EventMap;