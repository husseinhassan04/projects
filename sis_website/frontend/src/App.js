import './App.css';
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './Login/login.js';
import AdminFeed from './Admin/Admin-Feed.js';
import StudentFeed from './Student/Student-Feed.js';
import ProtectedRoute from './ProtectedRoute';
import AdminProtectedRoute from './AdminProtectedRoute.js';
import StudentProtectedRoute from './StudentProtectedRoute.js';

// Admin Pages
import AddCourses from './Admin/AddCourses.js';
import ModifyCourses from './Admin/ModifyCourses.js';
import ManageStudents from './Admin/ManageStudents.js';
import StudentDetailsPage from './Admin/StudentDetailsPage.js';
import AddStudent from './Admin/AddStudentPage.js';
import ChangePassword from './Admin/ChangePssword.js';
import UpdateGradesPage from './Admin/UpdateGradesPage.js';
// Student Pages
import ClassSearch from './Student/ClassSearch.js';
import ManageCourse from './Student/ManageCourse.js';
import ShoppingCart from './Student/ShoppingCart.js';
import AcademicRecords from './Student/AcademicRecords.js';
// import FinancialAccount from './Student/FinancialAccount.js'; 
import WeeklySchedule from './Student/WeeklySchedule.js';
import Profile from './Student/Profile.js';

const App = () => {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        
        {/* Admin Routes */}
        <Route path="/admin-feed" element={ <AdminProtectedRoute> <AdminFeed /> </AdminProtectedRoute> } />
        <Route path="/add-courses" element={ <AdminProtectedRoute> <AddCourses /> </AdminProtectedRoute> } />
        <Route path="/modify-courses" element={ <AdminProtectedRoute> <ModifyCourses /> </AdminProtectedRoute> } />
        <Route path="/manage-students" element={<AdminProtectedRoute><ManageStudents /></AdminProtectedRoute>} />
        <Route path="/add-student" element={<AdminProtectedRoute><AddStudent /></AdminProtectedRoute>} />
        
        <Route path="/change-password" element={<ProtectedRoute><ChangePassword/></ProtectedRoute>} />
        <Route path="/update-grades" element={<AdminProtectedRoute><UpdateGradesPage/></AdminProtectedRoute>}/>
        
        {/* Student Routes */}
        <Route
          path="/student-feed"
          element={<StudentProtectedRoute><StudentFeed /></StudentProtectedRoute>}
        />
        <Route path="/class-search" element={<StudentProtectedRoute><ClassSearch /></StudentProtectedRoute>} />
        <Route path="/manage-course" element={<StudentProtectedRoute><ManageCourse /></StudentProtectedRoute>} />
        <Route path="/shopping-cart" element={<StudentProtectedRoute><ShoppingCart /></StudentProtectedRoute>} />
        
        {/* Student Details Page Route */}
        <Route path="/student/:studentId" element={<AdminProtectedRoute><StudentDetailsPage /></AdminProtectedRoute>} />


        <Route path="/academic-records" element={<StudentProtectedRoute><AcademicRecords /></StudentProtectedRoute>} />
        {/* <Route path="/financial-account" element={<StudentProtectedRoute><FinancialAccount /></StudentProtectedRoute>} /> */}
        <Route path="/weekly-schedule" element={<ProtectedRoute><WeeklySchedule /></ProtectedRoute>} />
        <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
      </Routes>
    </Router>
  );
};

export default App;
