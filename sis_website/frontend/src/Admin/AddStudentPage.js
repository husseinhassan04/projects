import React, { useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import './AddStudent.css';

const AddStudent = () => {
    const [lname, setLname] = useState('');
    const [fname, setFname] = useState('');
    const [password, setPassword] = useState('');
    const [role, setRole] = useState('student');
    const [message, setMessage] = useState(''); // For showing success/error messages

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch('http://localhost:1337/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ lname, fname, password, role }),
            });

            const data = await response.json();
            if (response.ok) {
                setMessage('Registration successful!');
                // Clear form fields after successful registration
                setLname('');
                setFname('');
                setPassword('');
                setRole('student');
            } else {
                setMessage(`Registration failed: ${data.message}`);
            }
        } catch (error) {
            console.error('Error during registration:', error);
            setMessage('Error during registration, please try again.');
        }
    };

    return (
        <div className="container mt-5" id="add-student">
            <div className="card">
                <div className="card-body">
                    <h2 className="card-title text-center">Add Student</h2>

                    {/* Display message */}
                    {message && (
                        <div className={`alert ${message.includes('failed') ? 'alert-danger' : 'alert-success'}`} role="alert">
                            {message}
                        </div>
                    )}

                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label htmlFor="lname">Last Name</label>
                            <input
                                type="text"
                                className="form-control"
                                id="lname"
                                value={lname}
                                onChange={(e) => setLname(e.target.value)}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="fname">First Name</label>
                            <input
                                type="text"
                                className="form-control"
                                id="fname"
                                value={fname}
                                onChange={(e) => setFname(e.target.value)}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="password">Password</label>
                            <input
                                type="password"
                                className="form-control"
                                id="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="role">Role</label>
                            <select
                                className="form-control"
                                id="role"
                                value={role}
                                onChange={(e) => setRole(e.target.value)}
                                required
                            >
                                <option value="student">Student</option>
                                <option value="admin">Admin</option>
                            </select>
                        </div>
                        <button type="submit" className="btn btn-primary btn-block">Add Student</button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default AddStudent;
