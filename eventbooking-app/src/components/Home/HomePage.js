// src/components/Home/HomePage.js
import React from 'react';
import { Link } from 'react-router-dom';
import './HomePage.css';

const HomePage = () => {
  return (
    <div className="homepage" style={{ backgroundColor: '#000' }}>
      <section
        className="hero-section"
        style={{
          backgroundImage: "url('/images/overlay-image.jpeg')",
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          backgroundRepeat: 'no-repeat',
          minHeight: '60vh',
          color: '#fff',
          textAlign: 'center',
          position: 'relative'
        }}
      >
        <div
          className="hero-overlay"
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.4)'
          }}
        />
        <div
          className="hero-content"
          style={{
            position: 'relative',
            zIndex: 2,
            padding: '80px 20px'
          }}
        >
          <h1 style={{ fontSize: '3rem', fontWeight: 'bold' }}>
            Find something great to do.
          </h1>
          <p style={{ fontSize: '1.2rem', marginBottom: '1.5rem' }}>
            Discover the best in sports, music, culture, business, and more.
          </p>
          <Link to="/events">
            <button className="btn btn-danger btn-lg">Find an event</button>
          </Link>
        </div>
      </section>

      <section className="categories-section py-5" style={{ backgroundColor: '#000' }}>
        <div className="container">
          <h2 className="text-center mb-4" style={{ color: '#fff' }}>Popular Categories</h2>
          <div className="row g-4">
            <div className="col-md-4 col-sm-12">
              <div className="card h-100" style={{ backgroundColor: '#222', borderColor: '#333' }}>
                <img
                  className="card-img-top"
                  src="/images/music-category.jpg"
                  alt="Music"
                  style={{ objectFit: 'cover', height: '200px' }}
                />
                <div className="card-body">
                  <h5 className="card-title" style={{ color: '#fff' }}>Music</h5>
                  <p className="card-text" style={{ color: '#ddd' }}>Discover upcoming concerts and live music events.</p>
                  <Link to="/events?type=Concert">
                    <button className="btn btn-primary">Explore Music Events</button>
                  </Link>
                </div>
              </div>
            </div>

            <div className="col-md-4 col-sm-12">
              <div className="card h-100" style={{ backgroundColor: '#222', borderColor: '#333' }}>
                <img 
                  className="card-img-top" 
                  src="/images/comedy-category.jpg" 
                  alt="Comedy" 
                  style={{ objectFit: 'cover', height: '200px' }} 
                />
                <div className="card-body">
                  <h5 className="card-title" style={{ color: '#fff' }}>Theater</h5>
                  <p className="card-text" style={{ color: '#ddd' }}>Enjoy amazing theatrical performances and shows.</p>
                  <Link to="/events?type=Theater">
                    <button className="btn btn-primary">Explore Theater</button>
                  </Link>
                </div>
              </div>
            </div>
      
            <div className="col-md-4 col-sm-12">
              <div className="card h-100" style={{ backgroundColor: '#222', borderColor: '#333' }}>
                <img 
                  className="card-img-top" 
                  src="/images/food-category.jpeg" 
                  alt="Food" 
                  style={{ objectFit: 'cover', height: '200px' }} 
                />
                <div className="card-body">
                  <h5 className="card-title" style={{ color: '#fff' }}>Festival</h5>
                  <p className="card-text" style={{ color: '#ddd' }}>Discover food festivals, cultural events, and more.</p>
                  <Link to="/events?type=Festival">
                    <button className="btn btn-primary">Explore Festivals</button>
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default HomePage;