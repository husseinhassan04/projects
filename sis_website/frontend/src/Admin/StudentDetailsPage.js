import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom'; // For dynamic routing and navigation
import './StudentsDetailsAdmin.css'; // Custom CSS

const StudentDetailsPage = () => {
  const [student, setStudent] = useState(null);
  const [newCourseId, setNewCourseId] = useState('');
  const { studentId } = useParams(); // Get studentId from the URL
  const navigate = useNavigate(); // Hook to navigate back to Manage Students page

  const fetchStudentEnrollments = async () => {
    try {
      const response = await fetch(`/getStudentEnrollments/${studentId}`);
      if (response.ok) {
        const data = await response.json();
        setStudent((prev) => ({ ...prev, enrolledCourses: data }));
      } else {
        alert('Error fetching enrollments.');
      }
    } catch (error) {
      console.error('Error fetching enrollments:', error);
    }
  };

  useEffect(() => {
    fetchStudentEnrollments();
  }, [studentId]);

  const handleDropCourse = async (courseId, offeringId) => {
    if (student) {
      try {
        const response = await fetch(`/dropCourseFromStudent`, {
          method: 'DELETE',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ studentId, courseId, offeringId }), // Ensure the structure matches
        });
  
        if (response.ok) {
          const updatedStudent = await response.json();
          
          // Filter out the dropped course from the enrolledCourses array
          const updatedCourses = student.enrolledCourses.filter(
            (course) => course.course_id !== courseId || course.offering_id !== offeringId
          );
  
          // Update the student state with the new enrolled courses list
          setStudent((prevStudent) => ({
            ...prevStudent,
            enrolledCourses: updatedCourses,
          }));
  
          alert('Course dropped successfully!');
        } else {
          alert('Error dropping course');
        }
      } catch (error) {
        console.error('Error dropping course:', error);
      }
    }
  };

  const handleEnrollCourse = async () => {
    if (newCourseId && student) {
      try {
        const response = await fetch(`/addCourseToStudent`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            studentId,
            offeringId: newCourseId,
          }),
        });

        if (response.ok) {
          alert('Course enrolled successfully!');
          window.location.reload();
          setNewCourseId(''); // Clear the input field
        } else {
          const errorData = await response.json();
          alert(errorData.message || 'Error enrolling course');
        }
      } catch (error) {
        console.error('Error enrolling course:', error);
      }
    } else {
      alert('Please enter a valid offering ID.');
    }
  };

  return (
    <div id='manage-students'>
    <div className="main-container" >
      {student ? (
        <div className="student-details-card">
          <div className="student-header">
            <h2>
              Manage Courses for {student.fName} {student.lName}
            </h2>
          </div>

          {/* Current Enrollments */}
          <div className="enrollment-section">
            <h5>Current Enrollments</h5>
            {student.enrolledCourses.length === 0 ? (
              <div className="alert alert-info">
                No courses enrolled yet.
              </div>
            ) : (
              <div className="course-list">
                {student.enrolledCourses.map((course) => (
                  <div
                    key={`${course.course_id}-${course.offering_id}`}
                    className="course-card"
                  >
                    <div className="course-info">
                      <strong>{course.course_code}: {course.course_name}</strong>
                      <div className="course-details">
                        <span>Instructor: {course.instructor}</span>
                        <br />
                        <span>
                          Schedule: {course.day_of_week}, {course.time}
                        </span>
                      </div>
                    </div>
                    <button
                      className="btn btn-danger"
                      onClick={() =>
                        handleDropCourse(course.course_id, course.offering_id)
                      }
                    >
                      Drop
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Enroll New Course */}
          <div className="enroll-section">
            <h5>Course Enroll:</h5>
            <input
              type="text"
              className="input-course-id"
              value={newCourseId}
              onChange={(e) => setNewCourseId(e.target.value)}
              placeholder="Enter Course ID"
            />
            <button className="btn btn-success" onClick={handleEnrollCourse}>
              Enroll
            </button>
          </div>
        </div>
      ) : (
        <div className="loading-message">Loading student details...</div>
      )}
    </div>
    </div>
  );
};

export default StudentDetailsPage;
