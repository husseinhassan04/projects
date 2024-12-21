import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';

const UpdateGradesPage = () => {
    const { state } = useLocation();
    const { course, students } = state;

    // Ensure studentsData is initialized as an array (or fallback to empty array if not)
    const [studentsData, setStudentsData] = useState(Array.isArray(students) ? students : []);

    useEffect(() => {
        // Log students data and its type for debugging
        console.log('Students:', studentsData);
        console.log('Type of studentsData:', Array.isArray(studentsData) ? 'Array' : 'Not an array');
    }, [studentsData]); // Log every time studentsData is updated

    const handleGradeChange = (studentId, grade) => {
        setStudentsData((prevStudents) =>
            prevStudents.map((student) =>
                student.id === studentId ? { ...student, grade } : student
            )
        );
    };

    const updateGrades = async () => {
        try {
            const response = await fetch('/updateStudentGrades', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    courseId: course.offeringId,
                    students: studentsData.map((student) => ({
                        id: student.id,
                        grade: student.grade === null ? 0 : student.grade,
                    })),
                }),
            });

            if (!response.ok) {
                throw new Error('Failed to update grades');
            }

            const data = await response.json();
            alert('Grades updated successfully!');
        } catch (error) {
            console.error('Error updating grades:', error);
        }
    };

    return (
        <div className="container py-5">
            <h1 className="text-center mb-4 text-primary">
                Update Grades for {course.courseName}
            </h1>
            <table className="table table-bordered">
                <thead>
                    <tr>
                        <th>Student ID</th>
                        <th>First Name</th>
                        <th>Last Name</th>
                        <th>Grade</th>
                    </tr>
                </thead>
                <tbody>
                    {studentsData.map((student) => (
                        <tr key={student.id}>
                            <td>{student.id}</td>
                            <td>{student.fName}</td>
                            <td>{student.lName}</td>
                            <td>
                                <input
                                    type="text"
                                    className="form-control"
                                    value={student.grade === null ? 0 : student.grade}
                                    onChange={(e) =>
                                        handleGradeChange(student.id, e.target.value)
                                    }
                                />
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
            <div className="d-flex justify-content-end">
                <button className="btn btn-primary" onClick={updateGrades}>
                    Save Grades
                </button>
            </div>
        </div>
    );
};

export default UpdateGradesPage;
