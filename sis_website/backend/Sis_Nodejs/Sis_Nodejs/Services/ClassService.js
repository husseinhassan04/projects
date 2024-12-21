class ClassService {
    constructor(classRepository) {
        this.classRepository = classRepository;
    }

    async getEnrolledClasses(userId) {
        const courses = await this.classRepository.fetchEnrolledCourses(userId);
        const totalCredits = await this.classRepository.fetchTotalCredits(userId);
        return { courses, totalCredits };
    }

    async dropClass(userId, offeringId) {
        const totalCredits = await this.classRepository.fetchTotalCredits(userId);

        if (totalCredits <= 5) {
            throw new Error('Cannot drop courses if total credits are less than 5.');
        }

        const result = await this.classRepository.dropEnrolledCourse(userId, offeringId);

        if (result.affectedRows === 0) {
            throw new Error('Enrollment not found for the given offeringId and userId.');
        }

        return 'Course dropped successfully.';
    }

    async getCourses() {
        const courses = await this.classRepository.getCourses();
        return courses;
    }

    async getInstructors() {
        const instructors = await this.classRepository.getInstructors();
        return instructors;
    }

    async addNewCourse(courseName, courseCode, description, credits, prerequisites) {
        try {
            return await this.classRepository.addNewCourse(courseName, courseCode, description, credits, prerequisites);
        } catch (err) {
            console.error('Error in ClassService.addNewCourse:', err.message);
            throw err;
        }
    }

    async addNewOffering(courseId, instructor, maxStudents, status, dayOfWeek, time) {
        console.log('Service Layer Input:', { courseId, instructor, maxStudents, status, dayOfWeek, time }); // Log the service input
        try {
            const result = await this.classRepository.addNewOffering(courseId, instructor, maxStudents, status, dayOfWeek, time);
            console.log('Result from Repository Layer:', result); // Log the repository layer result
            return result;
        } catch (err) {
            console.error('Error in ClassService.addNewOffering:', err.message);
            throw err;
        }
    }

    async addInstructorToEnrollment(courseId, instructorId) {
        try {
            const result = await this.classRepository.addInstructorToEnrollment(courseId, instructorId);
            return result;
        } catch (error) {
            throw new Error('Error in adding instructor to enrollment');
        }
    }

    async getAllCourseOfferings() {
        const courses = await this.classRepository.getAllCourseOfferings();
        return courses;
    }

    async getGrades(userId) {
        try {
            const result = await this.classRepository.getGrades(userId);
            return result;
        } catch (error) {
            console.error('Service error:', error);
            throw new Error('Error fetching grades from repository: ' + error.message);
        }
    }




}

module.exports = ClassService;