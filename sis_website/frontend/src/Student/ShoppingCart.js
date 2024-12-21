import React, { useEffect, useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import './cart.css';

const ShoppingCart = () => {
    const [cartItems, setCartItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [isEnrolling, setIsEnrolling] = useState(false);
    const [removeMessage, setRemoveMessage] = useState('');

    const fetchCartItems = async () => {
        try {
            setLoading(true);
            const response = await fetch('http://localhost:1337/getCartItems', {
                method: 'GET',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
            });

            if (!response.ok) {
                throw new Error('Failed to fetch cart items');
            }

            const data = await response.json();
            setCartItems(data);
        } catch (err) {
            console.error('Error fetching cart items:', err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleEnroll = async (offeringId) => {
        try {
            setIsEnrolling(true);
            const response = await fetch('http://localhost:1337/enroll', {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ offeringId }),
            });

            if (!response.ok) {
                throw new Error('Failed to enroll in course.');
            }

            const result = await response.json();
            if (result.success) {
                setCartItems((prevItems) =>
                    prevItems.filter((item) => item.offeringId !== offeringId)
                );
                alert('course successfully enrolled');
            } else {
                throw new Error(result.message || 'Error in enrollment.');
            }
        } catch (error) {
            console.error('Error enrolling in course:', error);
            setError(error.message);
        } finally {
            setIsEnrolling(false);
        }
    };

    const handleRemoveFromCart = async (offeringId) => {
        try {
            const response = await fetch('http://localhost:1337/removeFromCart', {
                method: 'DELETE',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ offeringId }),
            });

            if (!response.ok) {
                throw new Error('Failed to remove item from cart.');
            }

            const result = await response.json();
            if (result.success) {
                const removedCourse = cartItems.find(
                    (item) => item.offeringId === offeringId
                );
                setRemoveMessage(`Removed course: ${removedCourse.courseCode} - ${removedCourse.courseName}`);
                setCartItems((prevItems) =>
                    prevItems.filter((item) => item.offeringId !== offeringId)
                );
            } else {
                throw new Error(result.message || 'Error removing from cart.');
            }
        } catch (error) {
            console.error('Error removing item from cart:', error);
            setError(error.message);
        }
    };

    const handleEnrollAll = async () => {
        try {
            setIsEnrolling(true);
            for (const item of cartItems) {
                await handleEnroll(item.offeringId);
            }
        } catch (error) {
            console.error('Error enrolling in all courses:', error);
        } finally {
            setIsEnrolling(false);
        }
    };

    useEffect(() => {
        fetchCartItems();
    }, []);

    return (
        <div className="CartBody">
            <div className="container py-5">
                <h2 className="text-center text-light mb-4">Your Cart</h2>
                {loading ? (
                    <div className="text-center my-5">
                        <div className="spinner-border text-light" role="status">
                            <span className="visually-hidden">Loading...</span>
                        </div>
                    </div>
                ) : error ? (
                    <div className="alert alert-danger text-center">{error}</div>
                ) : cartItems.length > 0 ? (
                    <>
                        {removeMessage && (
                            <p className="alert alert-warning text-center">
                                {removeMessage}
                            </p>
                        )}
                        <div className="row">
                            {cartItems.map((item) => (
                                <div key={item.offeringId} className="col-md-12 my-3">
                                    <div className="card course-card shadow-sm">
                                        <div className="card-body">
                                            <h5 className="card-title text-dark">
                                                {item.courseCode}: {item.courseName}
                                            </h5>
                                            <p className="card-text text-dark">
                                                <strong>Instructor:</strong> {item.instructor}<br />
                                                <strong>Capacity:</strong> {item.maxStudents} students<br />
                                                <strong>Day:</strong> {item.dayOfWeek}<br />
                                                <strong>Time:</strong> {item.time}
                                            </p>
                                            <div className="d-flex justify-content-between">
                                                <button
                                                    className="btn btn-success btn-sm"
                                                    onClick={() => handleEnroll(item.offeringId)}
                                                    disabled={isEnrolling}
                                                >
                                                    {isEnrolling ? 'Enrolling...' : 'Enroll'}
                                                </button>
                                                <button
                                                    className="btn btn-danger btn-sm"
                                                    onClick={() => handleRemoveFromCart(item.offeringId)}
                                                    disabled={isEnrolling}
                                                >
                                                    Remove
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                        <div className="text-center mt-4">
                            <button
                                className="btn btn-success btn-lg"
                                onClick={handleEnrollAll}
                                disabled={isEnrolling}
                            >
                                {isEnrolling ? 'Enrolling All...' : 'Enroll All'}
                            </button>
                        </div>
                    </>
                ) : (
                    <div className="alert alert-info text-center text-light">Your cart is empty.</div>
                )}
            </div>
        </div>
    );
};

export default ShoppingCart;
