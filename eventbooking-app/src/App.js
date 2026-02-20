import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';



// Import layouts
import Navbar from './components/Layout/Navbar';
import Footer from './components/Layout/Footer';

// Import authentication components
import Login from './components/Auth/Login';
import Register from './components/Auth/Register';
import Profile from './components/Auth/Profile';

// Import HomePage component
import HomePage from './components/Home/HomePage';

// Import event components
import EventList from './components/Events/EventList';
import EventDetails from './components/Events/EventDetails';

// Import booking components
import BookingList from './components/Bookings/BookingList';

// Import admin components
import AdminDashboard from './components/Admin/AdminDashboard';
import CreateEvent from './components/Admin/CreateEvent';
import EditEvent from './components/Admin/EditEvent';

// Import route protection
import ProtectedRoute from './components/Common/ProtectedRoute';
import AdminRoute from './components/Common/AdminRoute';

function App() {
  return (
    <BrowserRouter>
      <div className="d-flex flex-column min-vh-100">
        <Navbar />
        
        <main className="flex-grow-1">
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<HomePage />} />
            <Route path="/events" element={<EventList />} />
            <Route path="/events/:id" element={<EventDetails />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            
            {/* Protected Routes */}
            <Route 
              path="/profile" 
              element={
                <ProtectedRoute>
                  <Profile />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/bookings" 
              element={
                <ProtectedRoute>
                  <BookingList />
                </ProtectedRoute>
              } 
            />
            
            {/* Admin Routes */}
            <Route 
              path="/admin/dashboard" 
              element={
                <AdminRoute>
                  <AdminDashboard />
                </AdminRoute>
              } 
            />
            <Route 
              path="/admin/events/create" 
              element={
                <AdminRoute>
                  <CreateEvent />
                </AdminRoute>
              } 
            />
            <Route 
              path="/admin/events/edit/:id" 
              element={
                <AdminRoute>
                  <EditEvent />
                </AdminRoute>
              } 
            />
            
            {/* Fallback route */}
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </main>
        
        <Footer />
      </div>
    </BrowserRouter>
  );
}

export default App;