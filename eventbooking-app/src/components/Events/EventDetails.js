import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getEventById } from '../../services/eventService';
import { createBooking } from '../../services/bookingService';
import { isAuthenticated, isAdmin } from '../../services/authService';
import { getAvailableCurrencies, convertCurrency } from '../../services/currencyService';
import EventMap from './EventMap';
import CityInfo from './CityInfo';

const EventDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [isLoggedIn, setIsLoggedIn] = useState(isAuthenticated());
  const [isAdminUser, setIsAdminUser] = useState(false);

  // State for event details
  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // State for booking form
  const [tickets, setTickets] = useState(1);
  const [bookingStatus, setBookingStatus] = useState({
    loading: false,
    error: null,
    success: false
  });

  // Currency conversion states
  const [currencies, setCurrencies] = useState({});
  const [selectedCurrency, setSelectedCurrency] = useState('GBP');
  const [convertedPrice, setConvertedPrice] = useState(0);
  const [isConverting, setIsConverting] = useState(false);

  // Default event image
  const defaultEventImage = "https://via.placeholder.com/800x400?text=No+Image+Available";

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    fetchEventDetails();
    checkAuth();
    loadCurrencies().catch(err => console.error('Currency loading failed:', err));
  }, [id]);

  // Load available currencies
  const loadCurrencies = async () => {
    try {
      const availableCurrencies = await getAvailableCurrencies();
      console.log('Available currencies loaded:', availableCurrencies);
      setCurrencies(availableCurrencies);
      return availableCurrencies;
    } catch (err) {
      console.error('Failed to load currencies:', err);
      setCurrencies({ 'GBP': 'British Pound' }); // Fallback to just GBP
      throw err;
    }
  };

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    if (event && event.price) {
      if (selectedCurrency === 'GBP') {
        setConvertedPrice(event.price);
      } else {
        convertEventPrice();
      }
    }
  }, [selectedCurrency, event]);

  // Convert the event price
  const convertEventPrice = async () => {
    if (selectedCurrency === 'GBP') {
      setConvertedPrice(event.price);
      return;
    }

    setIsConverting(true);
    try {
      console.log(`Converting ${event.price} GBP to ${selectedCurrency}`);
      const result = await convertCurrency(event.price, selectedCurrency);
      console.log('Conversion result:', result);
      setConvertedPrice(result.converted);
    } catch (err) {
      console.error('Error converting price:', err);
      setConvertedPrice(event.price); // Fallback to original price
    } finally {
      setIsConverting(false);
    }
  };

  // Handle currency selection changes
  const handleCurrencyChange = (e) => {
    console.log('Currency changed to:', e.target.value);
    setSelectedCurrency(e.target.value);
  };

  // Format currency with appropriate symbol
  const formatCurrency = (amount, currencyCode) => {
    const symbols = {
      'GBP': '£',
      'USD': '$',
      'EUR': '€',
      'JPY': '¥',
      'CAD': 'C$',
      'AUD': 'A$',
      'CHF': 'CHF',
      'CNY': '¥',
      'INR': '₹',
      'BRL': 'R$',
      'MXN': 'Mex$'
    };

    const symbol = symbols[currencyCode] || '';

    // Format number based on currency
    if (currencyCode === 'JPY' || currencyCode === 'CNY') {
      return `${symbol}${Math.round(amount).toLocaleString()}`; // No decimal for yen and yuan
    } else {
      return `${symbol}${amount.toFixed(2).toLocaleString()}`;
    }
  };

  // Check if user is logged in and if they're an admin
  const checkAuth = async () => {
    const loggedIn = isAuthenticated();
    setIsLoggedIn(loggedIn);

    if (loggedIn) {
      const adminStatus = await isAdmin();
      setIsAdminUser(adminStatus);
    }
  };

  // Fetch event details from API
  const fetchEventDetails = async () => {
    setLoading(true);
    try {
      const data = await getEventById(id);
      setEvent(data);
      setConvertedPrice(data.price); // Set initial price in GBP
      setError(null);
    } catch (err) {
      setError('Failed to load event details. Please try again later.');
      setEvent(null);
    } finally {
      setLoading(false);
    }
  };

  // Handle ticket quantity change
  const handleTicketChange = (e) => {
    const value = parseInt(e.target.value);
    if (value > 0 && value <= (event?.availableTickets || 1)) {
      setTickets(value);
    }
  };

  // Handle booking submission
  const handleBookTickets = async (e) => {
    e.preventDefault();

    if (!isLoggedIn) {
      navigate('/login');
      return;
    }

    setBookingStatus({
      loading: true,
      error: null,
      success: false
    });

    try {
      await createBooking(event.id, tickets);

      setBookingStatus({
        loading: false,
        error: null,
        success: true
      });

      // Refresh event details to update available tickets
      fetchEventDetails();
    } catch (err) {
      setBookingStatus({
        loading: false,
        error: err.error || 'Failed to book tickets. Please try again.',
        success: false
      });
    }
  };

  // Render loading state
  if (loading) {
    return (
      <div className="container my-5 text-center">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
        <p className="mt-2">Loading event details...</p>
      </div>
    );
  }

  // Render error state
  if (error || !event) {
    return (
      <div className="container my-5">
        <div className="alert alert-danger">
          {error || 'Event not found'}
        </div>
        <Link to="/events" className="btn btn-primary">
          Back to Events
        </Link>
      </div>
    );
  }

  // Custom styles for better alignment
  const customStyles = {
    cardContainer: {
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
    },
    cardContent: {
      flex: '1 0 auto',
    },
    stickyBookingBox: {
      position: 'sticky',
      top: '20px',
    }
  };

  return (
    <div className="container my-5">
      <div className="row g-4">
        {/* Event Title - Full width for both columns */}
        <div className="col-12 mb-3">
          <h1 className="display-5 fw-bold">{event.name}</h1>
          <p className="text-muted">{event.type}</p>
        </div>

        {/* Main content - left column */}
        <div className="col-lg-8">
          {/* Event Image */}
          <div className="card mb-4 shadow-sm">
            <img
              src={event.imageData || defaultEventImage}
              alt={event.name}
              className="card-img-top"
              style={{ maxHeight: '400px', objectFit: 'cover' }}
            />
            <div className="card-body">
              <div className="row">
                <div className="col-md-6">
                  <p className="mb-1"><strong>Date:</strong> {event.date}</p>
                  <p className="mb-1"><strong>Time:</strong> {event.time}</p>
                  <p className="mb-1"><strong>Duration:</strong> {event.duration} hours</p>
                </div>
                <div className="col-md-6 text-md-end">
                  <p className="mb-1"><strong>Available Tickets:</strong> {event.availableTickets}</p>
                  <p className="mb-1">
                    <strong>Price:</strong> {isConverting ?
                      <span><small>Converting...</small></span> :
                      formatCurrency(convertedPrice, selectedCurrency)}
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Event Location Card */}
          <div className="card mb-4 shadow-sm">
            <div className="card-header bg-primary text-white">
              <h5 className="mb-0">Event Location</h5>
            </div>
            <div className="card-body">
              <p className="mb-1">{event.venue}</p>
              <p className="mb-1">{event.address}</p>
              <p className="mb-1">{event.city}, {event.country}</p>
              <p className="mb-3">{event.postcode}</p>

              <EventMap event={event} />
            </div>
          </div>

          {/* Get Directions Button */}
          <a
            href={`https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(
              `${event.venue}, ${event.address}, ${event.city}, ${event.country}, ${event.postcode}`
            )}`}
            className="btn btn-primary btn-lg w-100 mb-4"
            target="_blank"
            rel="noopener noreferrer"
          >
            Get Directions
          </a>
        </div>

        {/* Right Column - Booking and Navigation */}
        <div className="col-lg-4">
          <div style={customStyles.stickyBookingBox}>
            {/* Book Tickets Card */}
            <div className="card mb-4 shadow-sm">
              <div className="card-header bg-primary text-white">
                <h4 className="mb-0">Book Tickets</h4>
              </div>
              <div className="card-body">
                {bookingStatus.success ? (
                  <div className="alert alert-success">
                    Your booking was successful! Check your bookings page for details.
                  </div>
                ) : (
                  <>
                    {bookingStatus.error && (
                      <div className="alert alert-danger">
                        {bookingStatus.error}
                      </div>
                    )}

                    <form onSubmit={handleBookTickets}>
                      <div className="mb-3">
                        <label htmlFor="tickets" className="form-label">
                          Number of Tickets
                        </label>
                        <input
                          type="number"
                          className="form-control"
                          id="tickets"
                          value={tickets}
                          onChange={handleTicketChange}
                          min="1"
                          max={event.availableTickets}
                          required
                        />
                        <div className="form-text">
                          {event.availableTickets} tickets available
                        </div>
                      </div>

                      {/* Currency selector */}
                      <div className="mb-3">
                        <label htmlFor="currency" className="form-label">
                          Select Currency
                        </label>
                        <select
                          className="form-select"
                          id="currency"
                          value={selectedCurrency}
                          onChange={handleCurrencyChange}
                        >
                          {Object.keys(currencies).length > 0 ? (
                            Object.keys(currencies).map((code) => (
                              <option key={code} value={code}>
                                {code} - {currencies[code]}
                              </option>
                            ))
                          ) : (
                            <option value="GBP">GBP - British Pound (Loading more...)</option>
                          )}
                        </select>
                      </div>

                      <div className="mb-3">
                        <p className="fw-bold">
                          Total Price: {isConverting ?
                            <span><small>Converting...</small></span> :
                            formatCurrency(convertedPrice * tickets, selectedCurrency)}
                        </p>
                      </div>

                      <button
                        type="submit"
                        className="btn btn-primary w-100"
                        disabled={bookingStatus.loading || event.availableTickets < 1 || !isLoggedIn}
                      >
                        {bookingStatus.loading ? 'Processing...' : 'Book Now'}
                      </button>
                    </form>
                  </>
                )}
              </div>
            </div>

            {/* Back to Events Button */}
            <Link to="/events" className="btn btn-outline-secondary w-100 mb-4">
              Back to Events
            </Link>

            {/* City Information - Moved here as requested */}
            <CityInfo city={event.city} />

            {/* Admin Edit Button */}
            {isAdminUser && (
              <Link to={`/admin/events/edit/${event.id}`} className="btn btn-outline-primary w-100 mt-2">
                Edit Event
              </Link>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default EventDetails;