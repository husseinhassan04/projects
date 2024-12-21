import React from 'react';
import { useLocation } from 'react-router-dom';

const TemplateList = () => {
  const location = useLocation();
  const { data } = location.state || { data: { title: 'Default', text: 'No details available' } };

  return (
    <div className="container mt-5">
      <h3>{data.title}</h3>
      <p>{data.text}</p>
      <ul className="list-group">
        {/* Example of static details; replace with dynamic data if needed */}
        <li className="list-group-item">Detail 1 about {data.title}</li>
        <li className="list-group-item">Detail 2 about {data.title}</li>
        <li className="list-group-item">Detail 3 about {data.title}</li>
      </ul>
    </div>
  );
};

export default TemplateList;
