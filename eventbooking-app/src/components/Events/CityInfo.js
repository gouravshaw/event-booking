import React, { useState, useEffect } from 'react';
import { getCityInfo } from '../../services/cityInfoService';

const CityInfo = ({ city }) => {
  const [cityData, setCityData] = useState(null);
  const [loading, setLoading] = useState(true);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    if (city) {
      fetchCityInfo();
    }
  }, [city]);

  const fetchCityInfo = async () => {
    setLoading(true);
    const data = await getCityInfo(city);
    setCityData(data);
    setLoading(false);
  };

  // Loading state
  if (loading) {
    return (
      <div className="card mb-4">
        <div className="card-header bg-primary text-white">
          <h5 className="mb-0">About {city}</h5>
        </div>
        <div className="card-body text-center">
          <div className="spinner-border text-primary" role="status"></div>
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  // Error or no data
  if (!cityData || cityData.error) {
    return (
      <div className="card mb-4">
        <div className="card-header bg-primary text-white">
          <h5 className="mb-0">About {city}</h5>
        </div>
        <div className="card-body">
          <p>Information about {city} is not available.</p>
        </div>
      </div>
    );
  }

  // Display city information
  return (
    <div className="card mb-4">
      <div className="card-header bg-primary text-white">
        <h5 className="mb-0">About {cityData.name}</h5>
      </div>
      <div className="card-body">
        {cityData.imageUrl && (
          <img
            src={cityData.imageUrl}
            alt={cityData.name}
            className="img-fluid rounded float-end ms-3 mb-2"
            style={{ maxWidth: '200px' }}
          />
        )}
        <p>{cityData.description}</p>
      </div>
    </div>
  );
};

export default CityInfo;