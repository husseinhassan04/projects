class UserClassService {
    constructor(userClassRepository) {
        this.userClassRepository = userClassRepository;
    }

    async getStudentEnrollments(studentId) {

        const result = await this.userClassRepository.getStudentEnrollments(studentId);
        return result;

    }


    async dropClassAdmin(studentId, courseId, offeringId) {
        try {
            console.log(`Attempting to drop class: studentId=${studentId}, offeringId=${offeringId}`);

            const result = await this.userClassRepository.dropClassAdmin(studentId,courseId, offeringId);

            console.log("Query result:", result);

            if (result.affectedRows === 0) {
                throw new Error('Enrollment not found for the given offeringId and studentId.');
            }

            return 'Course dropped successfully.';
        } catch (error) {
            console.error('Error in dropClassAdmin:', error.message);
            throw error;
        }
    }




    async addEnrolledCourse(studentId, offeringId) {
        console.log('Starting addEnrolledCourse process:', { studentId, offeringId });

        const courseId = await this.userClassRepository.getCourseIdByOffering(offeringId);
        console.log('Course ID fetched from offering ID:', { offeringId, courseId });

        if (!courseId) {
            console.error('Invalid offering ID. No course found:', { offeringId });
            throw new Error('Invalid offering ID. No course found.');
        }

        const result = await this.userClassRepository.addEnrolledCourse(studentId, courseId, offeringId);
        console.log('Enrollment query result:', result);

        if (result.affectedRows === 0) {
            console.error('Enrollment failed in database:', { studentId, courseId, offeringId });
            throw new Error('Enrollment failed');
        }

        console.log('Course enrolled successfully in database:', { studentId, courseId, offeringId });
        return result;
    }


    async getStudentsForCourse(offeringId) {
        try {
            const result = await this.userClassRepository.getStudentsForCourse(offeringId);
            return result;
        } catch (error) {
            console.error('Error in getStudentsForCourse:', error.message);
            throw error;
        }
    }

    async updateGrades(courseId, students) {
        try {
            for (const student of students) {
                const { id: studentId, grade } = student;

                const existingGrade = await this.userClassRepository.getGrade(studentId, courseId);

                if (existingGrade != null) {
                    await this.userClassRepository.updateGrade(studentId, courseId, grade);
                } else {
                    await this.userClassRepository.addGrade(studentId, courseId, grade);
                }
            }
            return 'Grades updated successfully!';
        } catch (error) {
            throw new Error('Error updating grades in service: ' + error.message);
        }
    }



}

module.exports = UserClassService;