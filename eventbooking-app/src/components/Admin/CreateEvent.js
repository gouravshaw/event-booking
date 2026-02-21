import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { createEvent } from '../../services/eventService';
import { isAdmin } from '../../services/authService';

// Reduced common event types for the dropdown
const EVENT_TYPES = [
  "Conference",
  "Concert",
  "Theater",
  "Sports",
  "Festival"
];

const CreateEvent = () => {
  const navigate = useNavigate();

  // State for form data
  const [formData, setFormData] = useState({
    name: '',
    type: '',
    availableTickets: 100,
    price: 0,
    venue: '',
    address: '',
    city: '',
    country: '',
    postcode: '',
    date: '',
    time: '',
    duration: 2,
    imageData: '' // New field for image data
  });

  // State for image preview
  const [imagePreview, setImagePreview] = useState(null);

  // State for loading and errors
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    checkAdminAccess();
  }, []);

  // Check if the user is an admin
  const checkAdminAccess = async () => {
    const adminStatus = await isAdmin();

    // If not admin, redirect to home
    if (!adminStatus) {
      navigate('/');
    }
  };

  // Handle input changes
  const handleChange = (e) => {
    const { name, value, type } = e.target;

    // Convert number fields to numbers
    if (type === 'number') {
      setFormData({
        ...formData,
        [name]: parseFloat(value)
      });
    } else {
      setFormData({
        ...formData,
        [name]: value
      });
    }
  };

  // Handle image upload
  const handleImageUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        // Get base64 data
        const base64String = reader.result;
        // Set the image data in form state
        setFormData({
          ...formData,
          imageData: base64String
        });
        // Set image preview
        setImagePreview(base64String);
      };
      reader.readAsDataURL(file);
    }
  };

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      // Call create event API
      await createEvent(formData);

      // Show success message and redirect
      alert('Event created successfully!');
      navigate('/admin/dashboard');
    } catch (err) {
      setError('Failed to create event: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container my-5">
      <div className="row justify-content-center">
        <div className="col-lg-8">
          <div className="card">
            <div className="card-header bg-primary text-white">
              <h2 className="mb-0">Create New Event</h2>
            </div>
            <div className="card-body">
              {/* Error Message */}
              {error && (
                <div className="alert alert-danger" role="alert">
                  {error}
                </div>
              )}

              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label htmlFor="name" className="form-label">Event Name</label>
                  <input
                    type="text"
                    className="form-control"
                    id="name"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="type" className="form-label">Event Type</label>
                  <select
                    className="form-select"
                    id="type"
                    name="type"
                    value={formData.type}
                    onChange={handleChange}
                    required
                  >
                    <option value="">Select an event type</option>
                    {EVENT_TYPES.map((type) => (
                      <option key={type} value={type}>
                        {type}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="row mb-3">
                  <div className="col-md-6">
                    <label htmlFor="availableTickets" className="form-label">Available Tickets</label>
                    <input
                      type="number"
                      className="form-control"
                      id="availableTickets"
                      name="availableTickets"
                      value={formData.availableTickets}
                      onChange={handleChange}
                      min="1"
                      required
                    />
                  </div>
                  <div className="col-md-6">
                    <label htmlFor="price" className="form-label">Ticket Price (£)</label>
                    <input
                      type="number"
                      className="form-control"
                      id="price"
                      name="price"
                      value={formData.price}
                      onChange={handleChange}
                      min="0"
                      step="0.01"
                      required
                    />
                  </div>
                </div>

                {/* New Image Upload Field */}
                <div className="mb-3">
                  <label htmlFor="image" className="form-label">Event Image</label>
                  <input
                    type="file"
                    className="form-control"
                    id="image"
                    accept="image/*"
                    onChange={handleImageUpload}
                  />
                  <div className="form-text">Upload an image for the event (recommended size: 800x400px)</div>

                  {/* Image Preview - Updated to show full image */}
                  {imagePreview && (
                    <div className="mt-2">
                      <p>Image Preview:</p>
                      <div className="text-center">
                        <img
                          src={imagePreview}
                          alt="Event preview"
                          className="img-thumbnail"
                          style={{ maxHeight: '200px', maxWidth: '100%', width: 'auto' }}
                        />
                      </div>
                    </div>
                  )}
                </div>

                <div className="mb-3">
                  <label htmlFor="venue" className="form-label">Venue Name</label>
                  <input
                    type="text"
                    className="form-control"
                    id="venue"
                    name="venue"
                    value={formData.venue}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="address" className="form-label">Address</label>
                  <input
                    type="text"
                    className="form-control"
                    id="address"
                    name="address"
                    value={formData.address}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="row mb-3">
                  <div className="col-md-4">
                    <label htmlFor="city" className="form-label">City</label>
                    <input
                      type="text"
                      className="form-control"
                      id="city"
                      name="city"
                      value={formData.city}
                      onChange={handleChange}
                      required
                    />
                  </div>
                  <div className="col-md-4">
                    <label htmlFor="country" className="form-label">Country</label>
                    <input
                      type="text"
                      className="form-control"
                      id="country"
                      name="country"
                      value={formData.country}
                      onChange={handleChange}
                      required
                    />
                  </div>
                  <div className="col-md-4">
                    <label htmlFor="postcode" className="form-label">Postcode</label>
                    <input
                      type="text"
                      className="form-control"
                      id="postcode"
                      name="postcode"
                      value={formData.postcode}
                      onChange={handleChange}
                      required
                    />
                  </div>
                </div>

                <div className="row mb-3">
                  <div className="col-md-4">
                    <label htmlFor="date" className="form-label">Date</label>
                    <input
                      type="date"
                      className="form-control"
                      id="date"
                      name="date"
                      value={formData.date}
                      onChange={handleChange}
                      required
                    />
                  </div>
                  <div className="col-md-4">
                    <label htmlFor="time" className="form-label">Time</label>
                    <input
                      type="time"
                      className="form-control"
                      id="time"
                      name="time"
                      value={formData.time}
                      onChange={handleChange}
                      required
                    />
                  </div>
                  <div className="col-md-4">
                    <label htmlFor="duration" className="form-label">Duration (hours)</label>
                    <input
                      type="number"
                      className="form-control"
                      id="duration"
                      name="duration"
                      value={formData.duration}
                      onChange={handleChange}
                      min="1"
                      step="0.5"
                      required
                    />
                  </div>
                </div>

                <div className="d-flex justify-content-between mt-4">
                  <Link to="/admin/dashboard" className="btn btn-secondary">
                    Cancel
                  </Link>
                  <button
                    type="submit"
                    className="btn btn-primary"
                    disabled={loading}
                  >
                    {loading ? 'Creating...' : 'Create Event'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CreateEvent;