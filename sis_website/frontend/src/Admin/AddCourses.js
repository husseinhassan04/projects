import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import './AddClasses.css';

const AddClasses = () => {
  const navigate = useNavigate();

  const [isAddCourse, setIsAddCourse] = useState(true); // Track which form to show
  const [courses, setCourses] = useState([]);
  const [instructors, setInstructors] = useState([]); // Store instructors with admin role
  const [courseData, setCourseData] = useState({
    courseName: '',
    courseCode: '',
    description: '',
    credits: '',
    prerequisites: [],
  });
  const [offeringData, setOfferingData] = useState({
    courseId: '',
    instructor: '',
    maxStudents: '',
    status: '',
    dayOfWeek: '',
    time: '',
  });

  // Fetch courses
  useEffect(() => {
    const fetchCourses = async () => {
      try {
        const response = await fetch('/getCourses');
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        const data = await response.json();
        setCourses(data);
      } catch (error) {
        console.error('Error fetching courses:', error);
      }
    };
    fetchCourses();
  }, []);

  // Fetch instructors with the admin role
  useEffect(() => {
    const fetchInstructors = async () => {
      try {
        const response = await fetch('/getInstructors');
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        const data = await response.json();
        setInstructors(data);
      } catch (error) {
        console.error('Error fetching instructors:', error);
      }
    };
    fetchInstructors();
  }, []);

  const handleCourseChange = (e) => {
    setCourseData({
      ...courseData,
      [e.target.name]: e.target.value,
    });
  };

  const handleOfferingChange = (e) => {
    setOfferingData({
      ...offeringData,
      [e.target.name]: e.target.value,
    });
  };

  const handlePrerequisiteAdd = (courseCode) => {
    if (!courseData.prerequisites.includes(courseCode)) {
      setCourseData({
        ...courseData,
        prerequisites: [courseCode, ...courseData.prerequisites],
      });
    }
  };

  const handlePrerequisiteRemove = (courseCode) => {
    setCourseData({
      ...courseData,
      prerequisites: courseData.prerequisites.filter((code) => code !== courseCode),
    });
  };

  const handleAddCourse = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch('/addNewCourse', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(courseData),
      });
      if (response.ok) {
        const data = await response.json();
        console.log('Course added successfully:', data);
        setCourseData({
          courseName: '',
          courseCode: '',
          description: '',
          credits: '',
          prerequisites: [],
        });
      } else {
        console.error('Error adding course:', response.statusText);
      }
    } catch (error) {
      console.error('Error adding course:', error);
    }
  };

  const handleAddOffering = async (e) => {
    e.preventDefault();
    try {
      // Send the course offering data to add the offering first
      const offeringResponse = await fetch('/addNewOffering', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(offeringData),
      });
      if (offeringResponse.ok) {
        const offeringDataResponse = await offeringResponse.json();
        console.log('Course offering added successfully:', offeringDataResponse);
  
        // Once the offering is added, add the instructor to the enrollments table
        const enrollmentData = {
          courseId: offeringData.courseId,
          instructorId: offeringData.instructor,
        };
  
        // Send the enrollment data to the server to add the instructor to the enrollments table
        const enrollmentResponse = await fetch('/addInstructorToEnrollment', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(enrollmentData),
        });
  
        if (enrollmentResponse.ok) {
          const enrollmentDataResponse = await enrollmentResponse.json();
          console.log('Instructor added to enrollment table successfully:', enrollmentDataResponse);
  
          // Reset the offering data after success
          setOfferingData({
            courseId: '',
            instructor: '',
            maxStudents: '',
            status: '',
            dayOfWeek: '',
            time: '',
          });
        } else {
          console.error('Error adding instructor to enrollment:', enrollmentResponse.statusText);
        }
      } else {
        console.error('Error adding course offering:', offeringResponse.statusText);
      }
    } catch (error) {
      console.error('Error adding course offering and instructor to enrollment:', error);
    }
  };
  

  return (
    <div className="container mt-5" id="add-classes-body">
      <h2 className="text-center mb-4">Add Classes</h2>

      <div className="text-center mb-4">
        <button
          className={`btn ${isAddCourse ? 'btn-primary' : 'btn-outline-primary'} mx-2`}
          onClick={() => setIsAddCourse(true)}
        >
          Add Course
        </button>
        <button
          className={`btn ${!isAddCourse ? 'btn-primary' : 'btn-outline-primary'} mx-2`}
          onClick={() => setIsAddCourse(false)}
        >
          Add Course Offering
        </button>
      </div>

      {isAddCourse && (
        <form onSubmit={handleAddCourse} className="card p-4">
          <div className="form-group mb-3">
            <label htmlFor="courseName">Course Name</label>
            <input
              type="text"
              className="form-control"
              id="courseName"
              name="courseName"
              value={courseData.courseName}
              onChange={handleCourseChange}
              required
            />
          </div>
          <div className="form-group mb-3">
            <label htmlFor="courseCode">Course Code</label>
            <input
              type="text"
              className="form-control"
              id="courseCode"
              name="courseCode"
              value={courseData.courseCode}
              onChange={handleCourseChange}
              required
            />
          </div>
          <div className="form-group mb-3">
            <label htmlFor="description">Description</label>
            <textarea
              className="form-control"
              id="description"
              name="description"
              value={courseData.description}
              onChange={handleCourseChange}
              required
            ></textarea>
          </div>
          <div className="form-group mb-3">
            <label htmlFor="credits">Credits</label>
            <input
              type="number"
              className="form-control"
              id="credits"
              name="credits"
              value={courseData.credits}
              onChange={handleCourseChange}
              required
            />
          </div>
          <div className="form-group mb-3">
  <label>Prerequisites</label>
  {/* Display selected prerequisites */}
  <div className="selected-prerequisites">
    {courseData.prerequisites.map((prereq) => (
      <div key={prereq} className="prerequisite-badge">
        {prereq}
        <span onClick={() => handlePrerequisiteRemove(prereq)}>✖</span>
      </div>
    ))}
  </div>
  
  {/* Dropdown to add prerequisites */}
  <select
    className="form-control select-prerequisite"
    value=""
    onChange={(e) => handlePrerequisiteAdd(e.target.value)}
  >
    <option value="" disabled>
      Select a prerequisite
    </option>
    {courses
      .filter((course) => !courseData.prerequisites.includes(course.course_code))
      .map((course) => (
        <option key={course.course_code} value={course.course_code}>
          {course.course_name} ({course.course_code})
        </option>
      ))}
  </select>

  
</div>


          <button type="submit" className="btn btn-success mt-3">
            Add Course
          </button>
        </form>
      )}

      {!isAddCourse && (
        <form onSubmit={handleAddOffering} className="card p-4">
          <div className="form-group mb-3">
            <label htmlFor="courseId">Course</label>
            <select
              className="form-control"
              id="courseId"
              name="courseId"
              value={offeringData.courseId}
              onChange={handleOfferingChange}
              required
            >
              <option value="" disabled>
                Select a course
              </option>
              {courses.map((course) => (
                <option key={course.course_id} value={course.course_id}>
                  {course.course_name} ({course.course_code})
                </option>
              ))}
            </select>
          </div>
          <div className="form-group mb-3">
            <label htmlFor="instructor">Instructor</label>
            <select
              className="form-control"
              id="instructor"
              name="instructor"
              value={offeringData.instructor}
              onChange={handleOfferingChange}
              required
            >
              <option value="" disabled>
                Select an instructor
              </option>
              {instructors.map((instructor) => (
                <option
                  key={instructor.id}
                  value={instructor.id}
                >
                  {`${instructor.fname} ${instructor.lname}`}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group mb-3">
            <label htmlFor="maxStudents">Max Students</label>
            <input
              type="number"
              className="form-control"
              id="maxStudents"
              name="maxStudents"
              value={offeringData.maxStudents}
              onChange={handleOfferingChange}
              required
            />
          </div>
          <div className="form-group mb-3">
            <label htmlFor="status">Status</label>
            <select
              className="form-control"
              id="status"
              name="status"
              value={offeringData.status}
              onChange={handleOfferingChange}
              required
            >
              <option value="" disabled>
                Select status
              </option>
              <option value="open">open</option>
              <option value="closed">closed</option>
            </select>
          </div>
          <div className="form-group mb-3">
            <label htmlFor="dayOfWeek">Day of Week</label>
            <input
              type="text"
              className="form-control"
              id="dayOfWeek"
              name="dayOfWeek"
              value={offeringData.dayOfWeek}
              onChange={handleOfferingChange}
              required
            />
          </div>
          <div className="form-group mb-3">
            <label htmlFor="time">Time</label>
            <input
              type="text"
              className="form-control"
              id="time"
              name="time"
              placeholder="hh:mm:ss"
              value={offeringData.time}
              onChange={handleOfferingChange}
              required
            />
          </div>
          <button type="submit" className="btn btn-success mt-3">
            Add Offering
          </button>
        </form>
      )}
    </div>
  );
};

export default AddClasses;
