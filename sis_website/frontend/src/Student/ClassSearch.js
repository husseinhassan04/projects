import React, { useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';

const ClassSearch = () => {
    const [filteredCourses, setFilteredCourses] = useState([]);
    const [keyword, setSearchQuery] = useState('');
    const [error, setError] = useState(null);
    const [responseMessage, setResponseMessage] = useState('');
    const [cartStatus, setCartStatus] = useState({});

    const addToCart = async (offeringId) => {
        try {
            const response = await fetch('http://localhost:1337/addToCart', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify({ offeringId }),
            });

            if (!response.ok) {
                const data = await response.json();
                throw new Error(data.message || 'Error adding course to cart');
            }

            const data = await response.json();
            setResponseMessage(data.message);
            setCartStatus((prevStatus) => ({ ...prevStatus, [offeringId]: true }));
        } catch (err) {
            setError(err.message);
            setResponseMessage('Error adding course to cart: ' + err.message);
        }
    };

    const fetchCourses = async () => {
        try {
            const response = await fetch('http://localhost:1337/getCourseOfferings', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ keyword }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Error fetching courses.');
            }

            const data = await response.json();
            setFilteredCourses(data);

            if (data.length > 0) {
                const courseIds = data.map((course) => course.offeringId).join(',');
                const cartStatusResponse = await fetch(
                    `http://localhost:1337/isCourseInCart?courseIds=${courseIds}`,
                    { method: 'GET', credentials: 'include' }
                );

                if (!cartStatusResponse.ok) {
                    const cartErrorData = await cartStatusResponse.json();
                    throw new Error(cartErrorData.message || 'Error checking cart statuses.');
                }

                const cartStatusData = await cartStatusResponse.json();
                setCartStatus(cartStatusData.cartStatus);
            }

            setResponseMessage(`Success: Found ${data.length} courses.`);
        } catch (err) {
            setFilteredCourses([]);
            setResponseMessage(`Error: ${err.message}`);
            setError(err.message);
        }
    };

    const handleSearchInputChange = (e) => {
        setSearchQuery(e.target.value);
    };

    const handleSearchButtonClick = () => {
        if (!keyword.trim()) {
            setResponseMessage('Please enter a search keyword.');
            return;
        }
        setError(null);
        fetchCourses();
    };

    return (
        <div className="container mt-4">
            <h2 className="text-center mb-4">Course Search</h2>

            <div className="mb-3">
                <input
                    type="text"
                    className="form-control"
                    placeholder="Search courses by keyword..."
                    value={keyword}
                    onChange={handleSearchInputChange}
                />
            </div>

            <button className="btn btn-primary mb-4" onClick={handleSearchButtonClick}>
                Search
            </button>

            {responseMessage && (
                <div className={error ? 'alert alert-danger' : 'alert alert-success'}>
                    {responseMessage}
                </div>
            )}

            <div className="table-responsive">
                <table className="table table-striped table-bordered">
                    <thead className="table-dark">
                        <tr>
                            <th>ID</th>
                            <th>Course Code</th>
                            <th>Course Name</th>
                            <th>Instructor</th>
                            <th>Capacity</th>
                            <th>Status</th>
                            <th>Day</th>
                            <th>Time</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredCourses.length > 0 ? (
                            filteredCourses.map((course) => (
                                <tr key={course.offeringId}>
                                    <td>{course.offeringId}</td>
                                    <td>{course.courseCode}</td>
                                    <td>{course.courseName}</td>
                                    <td>{course.instructor}</td>
                                    <td>{course.maxStudents}</td>
                                    <td>{course.status}</td>
                                    <td>{course.dayOfWeek}</td>
                                    <td>{course.time}</td>
                                    <td>
                                        {cartStatus[course.offeringId] ? (
                                            <span className="text-success">Already in Cart</span>
                                        ) : (
                                            <button
                                                className="btn btn-success"
                                                onClick={() => addToCart(course.offeringId)}
                                            >
                                                Add to Cart
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan="9" className="text-center">
                                    {keyword.trim() === '' ? 'Enter a search keyword above.' : 'No courses found.'}
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default ClassSearch;
