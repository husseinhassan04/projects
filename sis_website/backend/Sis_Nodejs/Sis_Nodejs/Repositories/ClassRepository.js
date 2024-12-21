const mySql = require('../DbConnection');
const util = require('util');
const query = util.promisify(mySql.con.query).bind(mySql.con);

class ClassRepository {


    constructor(classService) {
        this.classService = classService;
    }

    async fetchEnrolledCourses(userId) {
        const coursesQuery = `
            SELECT e.id, co.offering_id, c.course_name, co.day_Of_Week, co.time, c.credits
            FROM enrollments e
            JOIN course_offering co ON e.offeringId = co.offering_id
            JOIN courses c ON co.course_id = c.course_id
            WHERE e.userId = ?`;
        return query(coursesQuery, [userId]);
    }

    async fetchTotalCredits(userId) {
        const totalCreditsQuery = `
            SELECT SUM(c.credits) AS totalCredits
            FROM enrollments e
            JOIN course_offering co ON e.offeringId = co.offering_id
            JOIN courses c ON co.course_id = c.course_id
            WHERE e.userId = ?`;
        const result = await query(totalCreditsQuery, [userId]);
        return result[0]?.totalCredits || 0;
    }


    async dropEnrolledCourse(userId, offeringId) {
        const deleteEnrollmentQuery = `DELETE FROM enrollments WHERE offeringId = ? AND userId = ?`;
        console.log('Executing query:', deleteEnrollmentQuery, 'with values:', [offeringId, userId]);
        return query(deleteEnrollmentQuery, [offeringId, userId]);
    }

    async getCourses() {
        const queryText = `SELECT * FROM courses`;
        return query(queryText,[]);
    }

    async getInstructors() {
        const role = "admin";
        const queryText = `SELECT id,fname,lname FROM users where role=?`;
        return query(queryText, [role]);
    }

    async addNewCourse(courseName, courseCode, description, credits, prerequisites) {
        try {

            const queryText = `INSERT INTO courses (course_name, course_code, course_description, credits, prerequisites) VALUES (?,?,?,?,?)`;
            return await query(queryText, [courseName, courseCode, description, credits, prerequisites]);
        } catch (error) {
            console.error('Error in ClassRepository.addNewCourse:', error.message);
            throw error;
        }

    }

    async addNewOffering(courseId, instructor, maxStudents, status, dayOfWeek, time) {
        console.log('Repository Layer Input:', { courseId, instructor, maxStudents, status, dayOfWeek, time });
        const queryText = `INSERT INTO course_offering (course_id, instructor, max_students, status, day_of_week, time) VALUES (?,?,?,?,?,?)`;
        try {
            const result = await query(queryText, [courseId, instructor, maxStudents, status, dayOfWeek, time]);
            console.log('Query Result:', result);
            return result;
        } catch (error) {
            console.error('Error in ClassRepository.addNewOffering:', error.message);
            throw error;
        }
    }

    async addInstructorToEnrollment(courseId, instructorId) {
        const offeringQueryText = 'SELECT offering_id FROM course_offering WHERE course_id = ? AND instructor = ? ORDER BY offering_id DESC LIMIT 1';

        try {
            const id = await query(offeringQueryText, [courseId, instructorId]);

            if (id.length === 0) {
                throw new Error('No course offering found for the specified courseId and instructor.');
            }

            const offeringId = id[0].offering_id;

            const queryText = 'INSERT INTO enrollments (courseId, offeringId, userId) VALUES (?, ?, ?)';
            const params = [courseId, offeringId, instructorId];

            const result = await query(queryText, params);
            return result;
        } catch (error) {
            throw new Error('Error executing query to add instructor to enrollment: ' + error.message);
        }
    }

    async getAllCourseOfferings() {
        try {
            const queryText = `
            SELECT 
                course_offering.offering_id, 
                courses.course_id, 
                courses.course_name, 
                CONCAT('Dr.', users.fname, ' ', users.lname) AS instructor_name, 
                course_offering.day_of_week,
                course_offering.time
            FROM course_offering
            JOIN courses ON courses.course_id = course_offering.course_id
            JOIN users ON users.id = course_offering.instructor
        `;
            console.log("Fetching all course offerings");
            const result = await query(queryText, []);
            return result;
        } catch (error) {
            throw new Error('Error executing query to get all offerings: ' + error.message);
        }
    }


    async getGrades(userId) {
        try {
            const queryText = `
            SELECT g.grade, co.course_id, c.course_name, c.course_code
            FROM grades g
            JOIN course_offering co ON g.offeringId = co.offering_id
            JOIN courses c ON co.course_id = c.course_id
            WHERE g.studentId = ?;
        `;
            const result = await query(queryText, [userId]);
            if (!result.length) {
                throw new Error('No grades found for the user.');
            }
            return result;
        } catch (error) {
            console.error('Database error:', error);
            throw new Error('Error executing query: ' + error.message);
        }
    }







}

module.exports = ClassRepository