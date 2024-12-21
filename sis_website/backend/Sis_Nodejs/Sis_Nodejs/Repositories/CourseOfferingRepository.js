const mySql = require('../DbConnection');
const util = require('util');
const query = util.promisify(mySql.con.query).bind(mySql.con);
const CourseOfferingDTO = require('../Models/CourseOfferingDTO');

class CourseOfferingRepository {
    async fetchCourses(keyword) {
        try {
            const queryText = `SELECT co.offering_id, c.course_code, c.course_name, co.instructor, 
                                co.max_students, co.status, co.day_of_week, co.time, users.fname, users.lname
                                FROM course_offering co, courses c, users
                                WHERE co.course_id = c.course_id 
                                AND users.id = co.instructor 
                                AND (LOWER(users.fname) LIKE ? 
                                OR LOWER(co.status) LIKE ? 
                                OR LOWER(co.day_of_week) LIKE ? 
                                OR LOWER(co.time) LIKE ? 
                                OR LOWER(c.course_code) LIKE ? 
                                OR LOWER(c.course_name) LIKE ?);`;

            const result = await query(queryText, [keyword, keyword, keyword, keyword, keyword, keyword]);

            return result.map(row =>
                new CourseOfferingDTO(
                    row.offering_id,
                    row.course_code,
                    row.course_name,
                    "Dr. " + row.fname + " " + row.lname,
                    row.max_students,
                    row.status,
                    row.day_of_week,
                    row.time
                ));
        } catch (error) {
            console.error('Error in CourseOfferingRepository.fetchCourses:', error);
            throw error;
        }
    }

    async removeCourseOfferingFromSystem(offeringId) {
        try {
            const queryText = `DELETE FROM course_offering WHERE offering_id = ?;`;
            const result = await query(queryText, [offeringId]);

            return { affectedRows: result.affectedRows };
        } catch (error) {
            console.error('Error in CourseOfferingRepository.removeCourseOfferingFromSystem:', error);
            throw error;
        }
    }

    async modifyCourseOffering(offeringId, status, dayOfWeek, time, maxStudents) {
        try {
            const queryText = `UPDATE course_offering
                               SET status = ?, 
                                   day_of_week = ?, 
                                   time = ?, 
                                   max_students = ?
                               WHERE offering_id = ?;`;

            const result = await query(queryText, [status, dayOfWeek, time, maxStudents, offeringId]);

            return { affectedRows: result.affectedRows };
        } catch (error) {
            console.error('Error in CourseOfferingRepository.modifyCourseOffering:', error);
            throw error;
        }
    }
}

module.exports = CourseOfferingRepository;
