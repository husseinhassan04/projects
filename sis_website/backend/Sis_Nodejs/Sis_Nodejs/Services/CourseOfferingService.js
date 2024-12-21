class CourseOfferingService {
    constructor(courseOfferingRepository) {
        this.courseOfferingRepository = courseOfferingRepository;
    }

    async fetchCourses(keyword) {
        const formattedKeyword = keyword ? `%${keyword.trim().toLowerCase()}%` : '%';

        return await this.courseOfferingRepository.fetchCourses(formattedKeyword);
    }
    async modifyCourseOffering(offeringId, status, dayOfWeek, time, maxStudents) {

        return await this.courseOfferingRepository.modifyCourseOffering(offeringId,status, dayOfWeek, time, maxStudents);
    }

    async removeCourseOfferingFromSystem(offeringId) {
        return await this.courseOfferingRepository.removeCourseOfferingFromSystem(offeringId);
    }
}

module.exports = CourseOfferingService;
