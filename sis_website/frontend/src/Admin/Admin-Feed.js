import React from 'react';

import { useNavigate } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import './Admin-Style.css';

const AdminFeed = () => {
  const navigate = useNavigate();
  const items = [
    
    { title: 'Profile', text: 'Check your info', imgSrc: "/profile_logo.png", link: "/profile" },
    { title: 'Weekly Schedule', text: 'Check your classes', imgSrc: "/weekly_schedule_logo.png", link: "/weekly-schedule" },
    { title: 'Add Courses', text: 'Add a new course to the list', imgSrc: "/add_course_logo.png", link: "/add-courses" },
    { title: 'Manage Students', text: 'Help students in enrollment', imgSrc: "/manage_students_logo.png", link: "/manage-students" },
    { title: 'Add Student', text: 'Create new student account', imgSrc: "/add_student_logo.png", link: "/add-student" },
    { title: 'Modify Courses', text: 'Modify courses infos', imgSrc: "/edit_course_logo.png", link: "/modify-courses" }
  ];

  const handleLogout = () => {
    localStorage.clear(); // Clear authentication data
    navigate('/'); // Redirect to login
  };

  return (
    <>
      <nav className="navbar navbar-expand-lg navbar-light bg-light mb-4" id="admin-feed">
        <div className="container-fluid">
          <a className="navbar-brand" href="#">
            <img src="/university_logo.jpg" alt="Logo" width="40" height="40" className="d-inline-block align-text-top" />
          </a>

          <div className="mx-auto">
            <span className="navbar-text fw-bold" style={{ fontSize: '1.5rem' }} >Admin Dashboard</span>
          </div>

          <div className="d-flex">
            
            <button className="btn btn-outline-danger" type="button" onClick={handleLogout}>Logout</button>
          </div>
        </div>
      </nav>

      <div className="container mt-5" id="admin-feed">
        <div className="row row-cols-2 row-cols-md-4 row-cols-lg-4 g-4">
          {items.map((item, index) => (
            <div className="col" key={index}>
              <div className="card bg-secondary text-center custom-card" onClick={() => navigate(item.link)}>
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



export default AdminFeed;
