import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './ManageStudents.css'; // Custom CSS

const ManageStudents = () => {
  const [students, setStudents] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate(); // Hook to navigate to the student's page

  useEffect(() => {
    const fetchStudents = async () => {
      try {
        const response = await fetch('/getStudents');
        const data = await response.json();
        setStudents(data);
      } catch (error) {
        console.error('Error fetching students:', error);
      }
    };

    fetchStudents();
  }, []);

  const filteredStudents = students.filter((student) =>
    `${student.fName} ${student.lName} ${student.email}`
      .toLowerCase()
      .includes(searchTerm.toLowerCase())
  );

  const handleStudentSelect = (studentId) => {
    navigate(`/student/${studentId}`);
  };

  return (
    <div className="container mt-5" id="manage-students">
      <h2 className="text-center text-white mb-4">Manage Students</h2>

      {/* Search Box */}
      <div className="mb-4">
        <input
          type="text"
          className="form-control search-box"
          placeholder="Search students by name or email"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      {/* Students Table */}
      <div className="table-responsive">
        <table className="table table-bordered table-striped table-hover text-white">
          <thead className="bg-primary">
            <tr >
              <th>ID</th>
              <th>Name</th>
              <th>Email</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredStudents.length === 0 ? (
              <tr>
                <td colSpan="4" className="text-center">No students found</td>
              </tr>
            ) : (
              filteredStudents.map((student) => (
                <tr key={student.id}>
                  <td>{student.id}</td>
                  <td>{student.fName} {student.lName}</td>
                  <td>{student.email}</td>
                  <td>
                    <button
                      className="btn btn-success"
                      onClick={() => handleStudentSelect(student.id)}
                    >
                      View Details
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ManageStudents;
