import React from 'react';
import { useNavigate } from 'react-router-dom';

import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import './Student-Style.css';

const StudentFeed = () => {

  const navigate = useNavigate();


  const items = [
    { title: 'Academic Records', text: 'Check your grades', imgSrc: "/academic_records_logo.png", redirectTo: '/academic-records' },
    // { title: 'Financial Account', text: 'Manage your account', imgSrc: "/financial_account_logo.png", redirectTo: '/financial-account' },
    { title: 'Weekly Schedule', text: 'Check your classes', imgSrc: "/weekly_schedule_logo.png", redirectTo: '/weekly-schedule' },
    { title: 'Class Search', text: 'Search for  classes', imgSrc: "/search_logo.png", redirectTo: '/class-search' },
    { title: 'Manage Course', text: 'Drop/Swap course', imgSrc: "/manage_classes_logo.png", redirectTo: '/manage-course' },
    { title: 'Shopping Cart', text: 'Manage Shopping Cart', imgSrc: "/shopping_cart_logo.png", redirectTo: '/shopping-cart' },
    { title: 'Profile', text: 'Check your info', imgSrc: "profile_logo.png", redirectTo: '/profile' }
  ];

  const handleOptionClick = (selectedItem) => {
    
    navigate(selectedItem.redirectTo);
  };

  const handleLogout = () => {
    localStorage.clear(); 
    navigate('/'); 
  };

  return (
    <>
      <nav className="navbar navbar-expand-lg navbar-light bg-light mb-4">
        <div className="container-fluid">
          <a className="navbar-brand" href="#">
            <img src="/university_logo.jpg" alt="Logo" width="40" height="40" className="d-inline-block align-text-top" />
          </a>

          <div className="mx-auto">
            <span className="navbar-text fw-bold" style={{ fontSize: '1.5rem' }} >Student Dashboard</span>
          </div>

          <div className="d-flex">
            <button className="btn btn-outline-danger" type="button" onClick={handleLogout}>Logout</button>
          </div>
        </div>
      </nav>

      <div className="container mt-5" id="student-feed">
        <div className="row row-cols-2 row-cols-md-4 row-cols-lg-4 g-4">
          {items.map((item, index) => (
            <div className="col" key={index}>
              <div className="card bg-secondary text-center custom-card" onClick={() => handleOptionClick(item)}>
                <div className="card-body">
                  <h5 className="card-title">{item.title}</h5>
                  <img src={item.imgSrc} alt={item.title} className="card-img-top custom-card-img" />
                  <p className="card-text">{item.text}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </>
  );
};

export default StudentFeed;
