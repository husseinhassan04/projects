import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom'; // Import useNavigate
import './ManageClasses.css';

const ManageCourses = () => {
    const [courses, setCourses] = useState([]);
    const [error, setError] = useState(null);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [editCourseData, setEditCourseData] = useState(null);
    const [successMessage, setSuccessMessage] = useState(null);
    const [students, setStudents] = useState([]);
    const [selectedCourse, setSelectedCourse] = useState(null);

    const navigate = useNavigate(); // Initialize the navigate function

    const fetchCourses = async (keyword = '') => {
        try {
            const response = await fetch('/getCourseOfferings', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ keyword: keyword.toLowerCase() }),
            });

            if (!response.ok) {
                throw new Error('Failed to fetch courses');
            }

            const data = await response.json();
            setCourses(data);
        } catch (error) {
            setError(error.message);
        }
    };

    const handleSearch = (e) => {
        e.preventDefault();
        fetchCourses(searchKeyword);
    };

    const openEditModal = (course) => {
        setEditCourseData(course);
    };

    const closeEditModal = () => {
        setEditCourseData(null);
    };

    const openUpdateGradesModal = async (course) => {
        const studentsResponse = await fetch(`/getStudentsForCourse/${course.offeringId}`);
        const studentsData = await studentsResponse.json();
        setStudents(studentsData);
   
        navigate('/update-grades', { state: { course, students: studentsData } });
    };
   


    const closeUpdateGradesModal = () => {
        setSelectedCourse(null);
        setStudents([]);
    };



    const handleEditChange = (field, value) => {
        setEditCourseData((prev) => ({ ...prev, [field]: value }));
    };

    const updateCourse = async () => {
        if (editCourseData) {
            try {
                const response = await fetch('/modifyCourseOffering', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        offeringId: editCourseData.offeringId,
                        status: editCourseData.status,
                        maxStudents: editCourseData.maxStudents,
                        time: editCourseData.time,
                        dayOfWeek: editCourseData.dayOfWeek,
                    }),
                });

                if (!response.ok) {
                    throw new Error('Failed to update course');
                }

                const data = await response.json();
                window.location.reload();
                alert('Course updated successfully.');
                closeEditModal();
                fetchCourses(); // Reload course list after update
            } catch (error) {
                console.error('Error updating course:', error);
                setError(error.message);
            }
        }
    };

    const removeCourse = async (offeringId) => {
        if (window.confirm('Are you sure you want to remove this course offering?')) {
            try {
                const response = await fetch('/removeCourseOfferingFromSystem', {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ offeringId }),
                });

                if (!response.ok) {
                    throw new Error('Failed to remove course');
                }

                const data = await response.json();
                alert(data.message);
                fetchCourses();
                window.location.reload();
            } catch (error) {
                console.error('Error removing course:', error);
            }
        }
    };

    const addTuitionFees = async () => {
        try {
            const response = await fetch('/addTuitionFees', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({}),
            });

            if (!response.ok) {
                throw new Error('Failed to add tuition fees');
            }

            const data = await response.json();
            alert('Tuition fees added successfully!');
            setSuccessMessage('Tuition fees have been successfully added to the database.');
        } catch (error) {
            console.error('Error adding tuition fees:', error);
            setError(error.message);
        }
    };

    const updateGrades = async () => {
        try {
            const response = await fetch('/updateStudentGrades', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    courseId: selectedCourse.offeringId,
                    students: students.map((student) => ({
                        id: student.id,
                        grade: student.grade,
                    })),
                }),
            });

            if (!response.ok) {
                throw new Error('Failed to update grades');
            }

            const data = await response.json();
            alert('Grades updated successfully!');
            closeUpdateGradesModal();
            fetchCourses(); // Reload course list after updating grades
        } catch (error) {
            console.error('Error updating grades:', error);
            setError(error.message);
        }
    };

    return (
        <div className="container py-5" id="modify_courses">
            <h1 className="text-center mb-4 text-primary">Manage Course Offerings</h1>
            {error && <div className="alert alert-danger">{error}</div>}
            {successMessage && <div className="alert alert-success">{successMessage}</div>}

            {/* Add Tuition Fees Button */}
            <div className="mb-4 d-flex justify-content-end">
                <button className="btn btn-success" onClick={addTuitionFees}>Add Tuition Fees</button>
            </div>

            {/* Search Bar */}
            <form className="d-flex mb-4" onSubmit={handleSearch}>
                <input
                    type="text"
                    className="form-control me-2"
                    placeholder="Search by keyword"
                    value={searchKeyword}
                    onChange={(e) => setSearchKeyword(e.target.value)}
                />
                <button type="submit" className="btn btn-primary">Search</button>
            </form>

            {/* Course List */}
            <div className="course-list">
                {courses.length === 0 ? (
                    <p className="text-center">No courses available.</p>
                ) : (
                    courses.map((course) => (
                        <div key={course.offeringId} className="course-item mb-4 shadow-lg rounded-lg hover-shadow">
                            <div className="course-info p-4 bg-white rounded-lg border">
                                <h5 className="course-title">{course.courseName} - {course.courseCode}</h5>
                                <p><strong>Instructor:</strong> {course.instructor}</p>
                                <p><strong>Day:</strong> {course.dayOfWeek}</p>
                                <p><strong>Time:</strong> {course.time}</p>
                                <p><strong>Status:</strong> {course.status}</p>
                                <p><strong>Max Students:</strong> {course.maxStudents}</p>
                                <div className="d-flex justify-content-end gap-3">
                                    <button className="btn btn-outline-warning" onClick={() => openEditModal(course)}>
                                        Edit
                                    </button>
                                    <button className="btn btn-outline-danger" onClick={() => removeCourse(course.offeringId)}>
                                        Remove
                                    </button>
                                    <button className="btn btn-outline-info" onClick={() => openUpdateGradesModal(course)}>
                                        Update Grades
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))
                )}
            </div>

            {/* Edit Modal */}
            {editCourseData && (
                <div className="modal show d-block">
                    <div className="modal-dialog">
                        <div className="modal-content">
                            <div className="modal-header">
                                <h5 className="modal-title">Edit Course Offering</h5>
                                <button type="button" className="btn-close" onClick={closeEditModal}></button>
                            </div>
                            <div className="modal-body">
                                <div className="mb-3">
                                    <label className="form-label">Day</label>
                                    <input
                                        type="text"
                                        className="form-control"
                                        value={editCourseData.dayOfWeek}
                                        onChange={(e) => handleEditChange('dayOfWeek', e.target.value)}
                                    />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">Time</label>
                                    <input
                                        type="text"
                                        className="form-control"
                                        value={editCourseData.time}
                                        onChange={(e) => handleEditChange('time', e.target.value)}
                                    />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">Status</label>
                                    <select
                                        className="form-select"
                                        value={editCourseData.status}
                                        onChange={(e) => handleEditChange('status', e.target.value)}
                                    >
                                        <option value="Open">Open</option>
                                        <option value="Closed">Closed</option>
                                    </select>
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">Max Students</label>
                                    <input
                                        type="number"
                                        className="form-control"
                                        value={editCourseData.maxStudents || 30}
                                        onChange={(e) => handleEditChange('maxStudents', e.target.value)}
                                    />
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button type="button" className="btn btn-secondary" onClick={closeEditModal}>Cancel</button>
                                <button type="button" className="btn btn-primary" onClick={updateCourse}>Save Changes</button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ManageCourses;
