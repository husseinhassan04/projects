class UserClassController {
    constructor(userClassService) {
        this.userClassService = userClassService;
    }

    async getStudentEnrollments(req,res) {
        try {

            const { studentId } = req.params;
            if (!studentId) {

                throw new Error('Student Id must be provided');
            }
            const result = await this.userClassService.getStudentEnrollments(studentId);
            console.log('Result from Service Layer:', result);
            return result
        } catch (error) {
            console.error('Error in UserClassController.getStudentEnrollments:', error.message);
            res.status(500).json({ message: 'Error getting student classes.', error: error.message });
        }
    }

    async dropClassAdmin(req, res) {
        try {
            const { studentId, courseId, offeringId } = req.body;

            if (!studentId || !courseId || !offeringId) {
                return res.status(400).json({ error: 'Missing required data (studentId, courseId, offeringId).' });
            }

            const message = await this.userClassService.dropClassAdmin(studentId, courseId, offeringId);

            return { success: true, message };
        } catch (error) {
            console.error('Error in dropClassAdmin:', error.message);
            return { error: error.message };
        }
    }







    async enrollStudentWithOffering(studentId, offeringId) {
        try {
            console.log('Attempting to enroll student with offering:', { studentId, offeringId });

            const result = await this.userClassService.addEnrolledCourse(studentId, offeringId);

            console.log('Enrollment result:', result);

            if (result.affectedRows === 0) {
                console.error('Enrollment failed for student:', { studentId, offeringId });
                throw new Error('Enrollment failed.');
            }

            console.log('Enrollment successful for student:', { studentId, offeringId });
            return { success: true, message: 'Course enrolled successfully.' };
        } catch (error) {
            console.error('Error in enrollStudentWithOffering:', error.message, error.stack);
            throw error; // Let the caller handle the error
        }
    }


    async getStudentsForCourse(offeringId) {
        if (!offeringId) {
            console.error('No offeringId provided for fetching students:', { offeringId });
            throw new Error('Offering ID is required to fetch students for the course.');
        }
        try {
            const result = await this.userClassService.getStudentsForCourse(offeringId);

            // Optional: Log the result to make sure the students are returned as expected
            console.log('Fetched students for offeringId:', offeringId, result);

            return result;
        } catch (error) {
            console.error('Error fetching students for course offeringId:', offeringId, error.message, error.stack);
            throw error;
        }
    }

    async updateGrades(req) {
        const { courseId, students } = req.body;
        if (!courseId) {
            console.error('No offeringId provided for updating students:', { courseId });
            throw new Error('Offering ID is required to update students for the course.');
        }
        if (!students || students.length === 0) {
            console.error('No students:', { courseId });
            throw new Error('No students.');
        }
        try {
            const result = await this.userClassService.updateGrades(courseId, students);
            return result;
        } catch (error) {
            console.error('Error Updating grades:', courseId, error.message, error.stack);
            throw error;
        }
    }







}
module.exports = UserClassController;