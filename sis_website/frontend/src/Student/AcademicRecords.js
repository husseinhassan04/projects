import React, { useState, useEffect } from 'react';
import './AcademicRecord.css';

const AcademicRecord = () => {
    const [gradesData, setGradesData] = useState([]);

    useEffect(() => {
        const fetchGradesData = async () => {
            try {
                const response = await fetch('/getStudentGrades', {
                    method: 'GET',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                });

                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }

                const data = await response.json();
                setGradesData(data);
            } catch (error) {
                console.error('Error fetching grades:', error);
            }
        };

        fetchGradesData();
    }, []);

    const getLetterGrade = (grade) => {
        if (grade >= 90) return 'A+';
        if (grade >= 85) return 'A';
        if (grade >= 80) return 'B+';
        if (grade >= 70) return 'B';
        if (grade >= 60) return 'C';
        if (grade >= 50) return 'D';
        return 'F';
    };

    const calculateGPA = () => {
        let total = 0;
        let count = 0;

        for (let i = 0; i < gradesData.length; i++) {
            const grade = parseFloat(gradesData[i].grade);

            if (!isNaN(grade)) {
                total += grade;
                count++;
            }
        }

        return count > 0 ? (total / count).toFixed(2) : 0;
    };

    return (
        <div className="container academic-record py-5" id="academic-record">
            <h1 className="text-center text-primary mb-5">Academic Record</h1>

            {gradesData.length > 0 ? (
                <>
                    <div className="gpa-container mb-5">
                        <h3 className="gpa-title">GPA: {calculateGPA()}</h3>
                    </div>

                    <table className="table table-hover table-striped mt-4">
                        <thead className="table-primary">
                            <tr>
                                <th>Course Code</th>
                                <th>Course Name</th>
                                <th>Grade (Numeric)</th>
                                <th>Grade (Letter)</th>
                            </tr>
                        </thead>
                        <tbody>
                            {gradesData.map((course) => (
                                <tr key={course.course_id}>
                                    <td>{course.course_code}</td>
                                    <td>{course.course_name}</td>
                                    <td>{course.grade}</td>
                                    <td>{getLetterGrade(course.grade)}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </>
            ) : (
                <p className="text-center mt-4">No grades available to display.</p>
            )}
        </div>
    );
};

export default AcademicRecord;
