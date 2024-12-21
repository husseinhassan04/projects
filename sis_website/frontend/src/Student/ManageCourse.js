import React, { useEffect, useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import './manageClasses.css';

const ManageCourse = () => {
    const [courses, setCourses] = useState([]);
    const [totalCredits, setTotalCredits] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [removeMessage, setRemoveMessage] = useState('');

    const fetchClasses = async () => {
        try {
            setLoading(true);
            const response = await fetch('http://localhost:1337/getClasses', {
                method: 'GET',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
            });

            if (!response.ok) {
                throw new Error('Failed to fetch classes');
            }

            const data = await response.json();
            setCourses(data.courses);
            setTotalCredits(data.totalCredits);
        } catch (err) {
            console.error('Error fetching classes:', err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };
    const handleRemoveClass = async (offeringId, courseName) => {
        const isConfirmed = window.confirm(`Are you sure you want to remove the class: ${courseName}?`);
    
        if (!isConfirmed) {
            return;
        }
    
        try {
            const response = await fetch('http://localhost:1337/dropClass', {
                method: 'DELETE',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ offeringId }),
            });
    
            if (!response.ok) {
                throw new Error('Failed to remove class.');
            }
    
            const result = await response.json();
            if (result.success) {
                const removedClass = courses.find((course) => course.offering_id === offeringId);
                setRemoveMessage(`Removed class: ${removedClass.course_name}`);
                setCourses((prevCourses) =>
                    prevCourses.filter((course) => course.offering_id !== offeringId)
                );
                setTotalCredits((prevCredits) => prevCredits - removedClass.credits);
            } else {
                throw new Error(result.message || 'Error removing class.');
            }
        } catch (error) {
            console.error('Error removing class:', error);
            setError(error.message);
        }
    };
    
    useEffect(() => {
        fetchClasses();
    }, []);

    return (
        <div className="ManageClassesBody">
            <div className="container py-5">
                <h2 className="text-center text-light mb-4">Manage Classes</h2>
                {loading ? (
                    <div className="text-center my-5">
                        <div className="spinner-border text-light" role="status">
                            <span className="visually-hidden">Loading...</span>
                        </div>
                    </div>
                ) : error ? (
                    <div className="alert alert-danger text-center">{error}</div>
                ) : courses.length > 0 ? (
                    <>
                        {removeMessage && (
                            <p className="alert alert-warning text-center">
                                {removeMessage}
                            </p>
                        )}
                        <div className="alert alert-info text-center">
                            Total Credits: {totalCredits}
                        </div>
                        <div className="row">
                            {courses.map((course) => (
                                <div key={course.offering_id} className="col-md-12 my-3">
                                    <div className="card class-card shadow-sm">
                                        <div className="card-body">
                                            <h5 className="card-title text-dark">
                                                {course.course_name}
                                            </h5>
                                            <p className="card-text text-dark">
                                                <strong>Day:</strong> {course.day_Of_Week}<br />
                                                <strong>Time:</strong> {course.time}<br />
                                                <strong>Credits:</strong> {course.credits}
                                            </p>
                                            <div className="d-flex justify-content-end">
                                                <button
                                                    className="btn btn-danger btn-sm"
                                                    onClick={() => handleRemoveClass(course.offering_id)}
                                                >
                                                    Remove
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </>
                ) : (
                    <div className="alert alert-info text-center text-light">No classes available.</div>
                )}
            </div>
        </div>
    );
};

export default ManageCourse;
