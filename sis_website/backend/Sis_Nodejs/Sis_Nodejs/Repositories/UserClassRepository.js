const mySql = require('../DbConnection');
const util = require('util');

const query = util.promisify(mySql.con.query).bind(mySql.con);

class UserClassRepository {

    async getStudentEnrollments(studentId) {

        try {

            const queryText = `SELECT co.course_id, co.offering_id, co.time, 
    co.day_of_week, co.instructor, co.max_students, c.course_name, c.course_code
    FROM enrollments e INNER JOIN course_offering co ON e.courseId = co.course_id AND e.offeringId = co.offering_id INNER JOIN courses c 
    ON co.course_id = c.course_id
    WHERE e.userId = ?
`;
            return query(queryText, [studentId]);
        } catch (error) {
            console.error('Error in UserClassRepository.getStudentEnrollments:', error);
            throw error;
        }
    }


    async dropClassAdmin(studentId, courseId, offeringId) {
        const deleteEnrollmentQuery = `DELETE FROM enrollments WHERE offeringId = ? AND userId = ? and courseId=?`;

        console.log('Executing query to drop course:', deleteEnrollmentQuery, 'with values:', [offeringId, studentId]);

        try {
            const result = await query(deleteEnrollmentQuery, [offeringId, studentId,courseId]);
            console.log('Query result:', result);

            return result;
        } catch (error) {
            console.error('Error executing drop course query:', error.message);
            throw error;
        }
    }


    async getCourseIdByOffering(offeringId) {
        console.log('Fetching course ID for offering ID:', offeringId);

        const queryText = `SELECT course_Id FROM course_offering WHERE offering_id = ?`;
        try {
            const result = await query(queryText, [offeringId]);
            console.log('Course ID query result:', { offeringId, result });

            if (result.length === 0 || !result[0].course_Id) {
                console.warn('No course found for offering ID:', offeringId);
                return null;
            }

            const courseId = result[0].course_Id;
            console.log('Course ID retrieved:', { courseId });
            return courseId;
        } catch (err) {
            console.error('Error executing query for offering ID:', offeringId, err.message);
            throw err;
        }
    }



    async addEnrolledCourse(studentId, courseId, offeringId) {
        const addEnrollmentQuery = `
        INSERT INTO enrollments (userId, courseId, offeringId)
        VALUES (?, ?, ?)
         `;

        console.log('Preparing to execute enrollment query:', {
            query: addEnrollmentQuery,
            values: [studentId, courseId, offeringId],
        });

        const result = await query(addEnrollmentQuery, [studentId, courseId, offeringId]);

        const changeFeesQuery = `update users 
                                   set tuition = -1 where id=?
                                `;
        query(changeFeesQuery, [studentId]);
        console.log('Enrollment query executed successfully:', { studentId, courseId, offeringId, result });
        return result;
    }


    async getStudentsForCourse(offeringId) {
        try {
            const queryText = `
            SELECT users.id, users.fName, users.lName, grades.grade
            FROM users
            JOIN enrollments ON users.id = enrollments.userId
            JOIN course_offering ON enrollments.offeringId = course_offering.offering_id
            LEFT JOIN grades ON users.id = grades.studentId AND course_offering.offering_id = grades.offeringId
            WHERE course_offering.offering_id = ?
        `;
            const result = await query(queryText, [offeringId]);
            return result;
        } catch (err) {
            console.error('Error executing query for getting students:', offeringId, err.message);
            throw err;
        }
    }

    async getGrade(studentId, offeringId) {
        const queryText = 'SELECT grade FROM grades WHERE studentId = ? AND offeringId = ?';
        const result = await query(queryText, [studentId, offeringId]);
        return result.length > 0 ? result[0].grade : null; 
    }


    async updateGrade(studentId, offeringId, grade) {
        const queryText = 'UPDATE grades SET grade = ? WHERE studentId = ? AND offeringId = ?';
        await query(queryText, [grade, studentId, offeringId]);
    }

    async addGrade(studentId, offeringId, grade) {
        const queryText = 'INSERT INTO grades (studentId, offeringId, grade) VALUES (?, ?, ?)';
        await query(queryText, [studentId, offeringId, grade]);
    }






}

module.exports = UserClassRepository;