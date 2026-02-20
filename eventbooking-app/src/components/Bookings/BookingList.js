import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getUserBookings, getAllBookings, cancelBooking, deleteBooking } from '../../services/bookingService';
import { getEventById } from '../../services/eventService';
import { isAdmin } from '../../services/authService';

const BookingList = () => {
  // State for bookings and loading
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [eventDetails, setEventDetails] = useState({});
  const [isAdminUser, setIsAdminUser] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  
  // Load bookings on component mount
  useEffect(() => {
    checkAdminStatus();
    fetchBookings();
  }, []);
  
  // Check if user is admin
  const checkAdminStatus = async () => {
    try {
      const adminStatus = await isAdmin();
      setIsAdminUser(adminStatus);
    } catch (err) {
      console.error("Error checking admin status:", err);
      setIsAdminUser(false);
    }
  };
  
  // Fetch bookings from API based on user role
  const fetchBookings = async () => {
    setLoading(true);
    try {
      // Check if user is admin again to ensure we have the latest status
      const adminStatus = await isAdmin();
      
      // Get bookings - all bookings for admin, user bookings for regular users
      const data = adminStatus ? await getAllBookings() : await getUserBookings();
      
      // Parse the JSON data (if it's a string)
      const parsedBookings = typeof data === 'string' ? JSON.parse(data) : data;
      
      setBookings(Array.isArray(parsedBookings) ? parsedBookings : []);
      setError(null);
      
      // Fetch event details for each booking
      const eventDetailsMap = {};
      for (const booking of Array.isArray(parsedBookings) ? parsedBookings : []) {
        try {
          const eventData = await getEventById(booking.eventId);
          eventDetailsMap[booking.eventId] = eventData;
        } catch (err) {
          console.error(`Failed to fetch event ${booking.eventId}:`, err);
        }
      }
      
      setEventDetails(eventDetailsMap);
    } catch (err) {
      setError('Failed to load bookings. Please try again later.');
      setBookings([]);
    } finally {
      setLoading(false);
    }
  };

  // Handle booking cancellation
  const handleCancelBooking = async (bookingId) => {
    if (window.confirm('Are you sure you want to cancel this booking? This action cannot be undone.')) {
      setCancelling(true);
      try {
        await cancelBooking(bookingId);
        alert('Booking cancelled successfully');
        // Refresh bookings list
        fetchBookings();
      } catch (err) {
        alert('Failed to cancel booking: ' + err.message);
      } finally {
        setCancelling(false);
      }
    }
  };

  // Handle booking deletion (admin only)
  const handleDeleteBooking = async (bookingId) => {
    if (window.confirm('Are you sure you want to delete this booking? This action cannot be undone.')) {
      setCancelling(true);
      try {
        await deleteBooking(bookingId);
        alert('Booking deleted successfully');
        // Refresh bookings list
        fetchBookings();
      } catch (err) {
        alert('Failed to delete booking: ' + err.message);
      } finally {
        setCancelling(false);
      }
    }
  };
  
  // Format the booking status with appropriate color
  const getStatusBadge = (status) => {
    let badgeClass = '';
    
    switch (status) {
      case 'CONFIRMED':
        badgeClass = 'bg-success';
        break;
      case 'CANCELLED':
        badgeClass = 'bg-danger';
        break;
      case 'PENDING':
        badgeClass = 'bg-warning';
        break;
      default:
        badgeClass = 'bg-secondary';
    }
    
    return (
      <span className={`badge ${badgeClass}`}>
        {status}
      </span>
    );
  };
  
  // Format date string to be more readable
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    
    try {
      const date = new Date(dateString);
      return date.toLocaleString();
    } catch (e) {
      return dateString;
    }
  };
  
  return (
    <div className="container my-5">
      <h1 className="mb-4">
        {isAdminUser ? 'All Bookings' : 'My Bookings'}
      </h1>
      
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
          <p className="mt-2">Loading bookings...</p>
        </div>
      ) : (
        /* Bookings List */
        <>
          {bookings.length === 0 ? (
            <div className="alert alert-info">
              {isAdminUser ? "No bookings found in the system." : "You don't have any bookings yet."}
              <Link to="/" className="alert-link ms-2">
                Browse events
              </Link>
            </div>
          ) : (
            <div className="row">
              {bookings.map((booking) => {
                const event = eventDetails[booking.eventId] || {};
                
                return (
                  <div className="col-md-6 mb-4" key={booking.id}>
                    <div className="card">
                      <div className="card-header d-flex justify-content-between align-items-center">
                        <h5 className="mb-0">Booking #{booking.id}</h5>
                        {getStatusBadge(booking.status)}
                      </div>
                      
                      <div className="card-body">
                        <h5 className="card-title">{event.name || 'Event'}</h5>
                        <p className="text-muted">{event.type || 'Event'}</p>
                        
                        {isAdminUser && (
                          <p><strong>User ID:</strong> {booking.userFirebaseUid}</p>
                        )}
                        
                        <div className="mb-3">
                          <p><strong>Event Date:</strong> {event.date || 'N/A'}</p>
                          <p><strong>Event Time:</strong> {event.time || 'N/A'}</p>
                          <p><strong>Venue:</strong> {event.venue || 'N/A'}</p>
                        </div>
                        
                        <div className="row mb-3">
                          <div className="col-6">
                            <strong>Tickets:</strong> {booking.ticketsBooked}
                          </div>
                          <div className="col-6">
                            <strong>Total:</strong> £{booking.totalPrice?.toFixed(2) || '0.00'} {booking.currency !== 'GBP' ? booking.currency : ''}
                          </div>
                        </div>
                        
                        <p className="mb-0">
                          <strong>Booking Date:</strong> {formatDate(booking.bookingTime)}
                        </p>
                      </div>
                      
                      <div className="card-footer bg-white">
                        <div className="d-flex justify-content-between">
                          <Link 
                            to={`/events/${booking.eventId}`} 
                            className="btn btn-outline-primary"
                          >
                            View Event
                          </Link>
                          
                          {booking.status !== 'CANCELLED' && (
                            <button 
                              className="btn btn-outline-danger"
                              onClick={() => handleCancelBooking(booking.id)}
                              disabled={cancelling}
                            >
                              {cancelling ? 'Processing...' : 'Cancel Booking'}
                            </button>
                          )}
                          
                          {isAdminUser && (
                            <button 
                              className="btn btn-outline-secondary"
                              onClick={() => handleDeleteBooking(booking.id)}
                              disabled={cancelling}
                            >
                              Delete
                            </button>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default BookingList;