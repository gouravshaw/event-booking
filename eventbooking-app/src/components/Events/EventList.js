import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { getAllEvents, searchEvents } from '../../services/eventService';
import { isAuthenticated, isAdmin } from '../../services/authService';

// Reduced common event types for the dropdown
const EVENT_TYPES = [
  "Conference",
  "Concert",
  "Theater",
  "Sports",
  "Festival"
];

const EventList = () => {
  const location = useLocation();
  // State for events and loading
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isAdminUser, setIsAdminUser] = useState(false);

  // Parse query parameters to get type from URL
  const queryParams = new URLSearchParams(location.search);
  const typeFromUrl = queryParams.get('type');

  // State for search filters
  const [filters, setFilters] = useState({
    type: typeFromUrl || '',
    city: '',
    date: ''
  });

  /* eslint-disable react-hooks/exhaustive-deps */
  useEffect(() => {
    checkAuth();
    if (typeFromUrl) {
      handleSearch({ preventDefault: () => { } });
    } else {
      fetchEvents();
    }
  }, [typeFromUrl]);
  /* eslint-enable react-hooks/exhaustive-deps */

  // Check if user is logged in and if they're an admin
  const checkAuth = async () => {
    const loggedIn = isAuthenticated();
    if (loggedIn) {
      const adminStatus = await isAdmin();
      setIsAdminUser(adminStatus);
    }
  };

  // Fetch all events from API
  const fetchEvents = async () => {
    setLoading(true);
    try {
      const data = await getAllEvents();
      setEvents(data);
      setError(null);
    } catch (err) {
      setError('Failed to load events. Please try again later.');
      setEvents([]);
    } finally {
      setLoading(false);
    }
  };

  // Handle filter changes
  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters({
      ...filters,
      [name]: value
    });
  };

  // Handle search submit
  const handleSearch = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const data = await searchEvents(
        filters.type || null,
        filters.city || null,
        filters.date || null
      );
      setEvents(data);
      setError(null);
    } catch (err) {
      setError('Search failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // Reset filters
  const handleReset = () => {
    setFilters({
      type: '',
      city: '',
      date: ''
    });
    fetchEvents();
  };

  // Default event image
  const defaultEventImage = "https://via.placeholder.com/400x200?text=No+Image+Available";

  return (
    <div className="container my-5">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1>Upcoming Events</h1>

        {isAdminUser && (
          <Link to="/admin/events/create" className="btn btn-success">
            <i className="bi bi-plus-circle me-2"></i>
            Add New Event
          </Link>
        )}
      </div>

      {/* Search/Filter Form */}
      <div className="card mb-4">
        <div className="card-header bg-light">
          <h5 className="mb-0">Search Events</h5>
        </div>
        <div className="card-body">
          <form onSubmit={handleSearch}>
            <div className="row g-3">
              <div className="col-md-4">
                <label htmlFor="type" className="form-label">Event Type</label>
                <select
                  className="form-select"
                  id="type"
                  name="type"
                  value={filters.type}
                  onChange={handleFilterChange}
                >
                  <option value="">All Event Types</option>
                  {EVENT_TYPES.map((type) => (
                    <option key={type} value={type}>
                      {type}
                    </option>
                  ))}
                </select>
              </div>

              <div className="col-md-4">
                <label htmlFor="city" className="form-label">City</label>
                <input
                  type="text"
                  className="form-control"
                  id="city"
                  name="city"
                  value={filters.city}
                  onChange={handleFilterChange}
                  placeholder="Enter city"
                />
              </div>

              <div className="col-md-4">
                <label htmlFor="date" className="form-label">Date</label>
                <input
                  type="date"
                  className="form-control"
                  id="date"
                  name="date"
                  value={filters.date}
                  onChange={handleFilterChange}
                />
              </div>

              <div className="col-12">
                <button type="submit" className="btn btn-primary me-2">
                  Search
                </button>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={handleReset}
                >
                  Reset
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {/* Loading Indicator */}
      {loading ? (
        <div className="text-center my-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="mt-2">Loading events...</p>
        </div>
      ) : (
        /* Events List */
        <div className="row g-4">
          {events.length === 0 ? (
            <div className="col-12">
              <div className="alert alert-info">
                No events found. Try adjusting your search filters.
              </div>
            </div>
          ) : (
            events.map((event) => (
              <div className="col-md-6 col-lg-4" key={event.id}>
                <div className="card h-100">
                  {/* Event Image - Updated to show full image without cropping */}
                  <div className="card-img-top text-center" style={{ padding: '10px' }}>
                    <img
                      src={event.imageData || defaultEventImage}
                      alt={event.name}
                      className="img-fluid"
                      style={{ maxHeight: '200px', width: 'auto' }}
                    />
                  </div>
                  <div className="card-body">
                    <h5 className="card-title">{event.name}</h5>
                    <h6 className="card-subtitle mb-2 text-muted">
                      {event.type}
                    </h6>
                    <p className="card-text">
                      <strong>Date:</strong> {event.date}<br />
                      <strong>Time:</strong> {event.time}<br />
                      <strong>Venue:</strong> {event.venue}, {event.city}<br />
                      <strong>Available Tickets:</strong> {event.availableTickets}<br />
                      <strong>Price:</strong> £{event.price.toFixed(2)}
                    </p>
                  </div>
                  <div className="card-footer bg-white text-center">
                    <Link
                      to={`/events/${event.id}`}
                      className="btn btn-primary w-100"
                    >
                      View Details
                    </Link>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
};

export default EventList;