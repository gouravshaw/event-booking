import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getAllEvents, deleteEvent } from '../../services/eventService';
import { getAllBookings } from '../../services/bookingService';
import { isAdmin } from '../../services/authService';

const AdminDashboard = () => {
  const navigate = useNavigate();

  // State for events, bookings and loading
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Stats
  const [stats, setStats] = useState({
    totalEvents: 0,
    totalBookings: 0,
    totalTicketsBooked: 0,
    totalRevenue: 0
  });

  /* eslint-disable react-hooks/exhaustive-deps */
  useEffect(() => {
    checkAdminAccess();
    fetchData();
  }, []);
  /* eslint-enable react-hooks/exhaustive-deps */

  // Check if the user is an admin
  const checkAdminAccess = async () => {
    const adminStatus = await isAdmin();

    // If not admin, redirect to home
    if (!adminStatus) {
      navigate('/');
    }
  };

  // Fetch all data from API
  const fetchData = async () => {
    setLoading(true);
    try {
      // Fetch events
      const eventsData = await getAllEvents();
      setEvents(eventsData);

      // Fetch bookings
      const bookingsData = await getAllBookings();
      // Parse bookings if it's a string
      const parsedBookings = typeof bookingsData === 'string' ? JSON.parse(bookingsData) : bookingsData;
      const parsedArray = Array.isArray(parsedBookings) ? parsedBookings : [];

      // Calculate stats
      calculateStats(eventsData, parsedArray);

      setError(null);
    } catch (err) {
      setError('Failed to load data. Please try again later.');
      setEvents([]);
    } finally {
      setLoading(false);
    }
  };

  // Calculate dashboard statistics
  const calculateStats = (events, bookings) => {
    const totalEvents = events.length;
    const totalBookings = bookings.length;

    // Calculate total tickets booked and revenue
    let ticketsBooked = 0;
    let revenue = 0;

    bookings.forEach(booking => {
      if (booking.status !== 'CANCELLED') {
        ticketsBooked += booking.ticketsBooked || 0;
        revenue += booking.totalPrice || 0;
      }
    });

    setStats({
      totalEvents,
      totalBookings,
      totalTicketsBooked: ticketsBooked,
      totalRevenue: revenue
    });
  };

  // Handle event deletion
  const handleDeleteEvent = async (eventId) => {
    if (window.confirm('Are you sure you want to delete this event?')) {
      try {
        await deleteEvent(eventId);
        // Refresh the data
        fetchData();
        alert('Event deleted successfully');
      } catch (err) {
        alert('Failed to delete event: ' + err.message);
      }
    }
  };

  return (
    <div className="container my-5">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1>Admin Dashboard - Events</h1>
        <Link to="/admin/events/create" className="btn btn-success">
          Create New Event
        </Link>
      </div>

      {/* Stats Cards */}
      <div className="row mb-4">
        <div className="col-md-3">
          <div className="card bg-primary text-white">
            <div className="card-body">
              <h5 className="card-title">Total Events</h5>
              <h2 className="card-text">{stats.totalEvents}</h2>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card bg-success text-white">
            <div className="card-body">
              <h5 className="card-title">Total Bookings</h5>
              <h2 className="card-text">{stats.totalBookings}</h2>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card bg-info text-white">
            <div className="card-body">
              <h5 className="card-title">Tickets Booked</h5>
              <h2 className="card-text">{stats.totalTicketsBooked}</h2>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card bg-warning text-white">
            <div className="card-body">
              <h5 className="card-title">Total Revenue</h5>
              <h2 className="card-text">£{stats.totalRevenue.toFixed(2)}</h2>
            </div>
          </div>
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
        /* Events Table */
        <div className="card">
          <div className="card-body">
            <div className="table-responsive">
              {events.length === 0 ? (
                <div className="alert alert-info">
                  No events found in the system.
                </div>
              ) : (
                <table className="table table-striped table-hover">
                  <thead className="table-dark">
                    <tr>
                      <th>ID</th>
                      <th>Name</th>
                      <th>Type</th>
                      <th>Date</th>
                      <th>Venue</th>
                      <th>Available Tickets</th>
                      <th>Price</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {events.map((event) => (
                      <tr key={event.id}>
                        <td>{event.id}</td>
                        <td>{event.name}</td>
                        <td>{event.type}</td>
                        <td>{event.date}</td>
                        <td>{event.venue}</td>
                        <td>{event.availableTickets}</td>
                        <td>£{event.price.toFixed(2)}</td>
                        <td>
                          <div className="d-flex gap-2">
                            <Link
                              to={`/admin/events/edit/${event.id}`}
                              className="btn btn-sm btn-primary"
                            >
                              Edit
                            </Link>
                            <button
                              onClick={() => handleDeleteEvent(event.id)}
                              className="btn btn-sm btn-danger"
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;