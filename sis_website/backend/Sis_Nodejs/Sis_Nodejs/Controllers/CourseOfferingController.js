
class CourseOfferingController {
    constructor(courseOfferingService) {
        this.courseOfferingService = courseOfferingService;
    }

    async fetchCourses(req, res) {
        try {
            const { keyword } = req.body;
            if (!keyword) {
                return res.status(400).json({ message: 'Keyword is required.' });
            }
            const courses = await this.courseOfferingService.fetchCourses(keyword);
            res.json(courses);
        } catch (error) {
            console.error('Error in CourseOfferingController.fetchCourses:', error);
            res.status(500).json({ message: 'Internal server error', error: error.message });
        }
    }

    async modifyCourseOffering(req, res) {
        try {
            const { status, maxStudents,  time,dayOfWeek,offeringId } = req.body || '';
            if (!status || !dayOfWeek || !time || !maxStudents) {
                return res.status(400).json({ message: 'All elements are required.' });
            }
            const courses = await this.courseOfferingService.modifyCourseOffering(offeringId,status, dayOfWeek, time, maxStudents);
            res.json(courses);
        } catch (error) {
            console.error('Error in CourseOfferingController.fetchCourses:', error);
            res.status(500).json({ message: 'Internal server error', error: error.message });
        }
    }

    async removeCourseOfferingFromSystem(req, res) {
        try {
            const { offeringId } = req.body || '';
            if (!offeringId) {
                return res.status(400).json({ message: 'offeringId is required.' });
            }
            const courses = await this.courseOfferingService.removeCourseOfferingFromSystem(offeringId);
            res.json(courses);
        } catch (error) {
            console.error('Error in CourseOfferingController.fetchCourses:', error);
            res.status(500).json({ message: 'Internal server error', error: error.message });
        }
    }

}

module.exports = CourseOfferingController;
