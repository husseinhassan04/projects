class ClassController {
    constructor(classService) {
        this.classService = classService;
    }

    async getEnrolledClasses(req, res) {
        try {
            const userId = req.cookies.id;
            if (!userId)
                return res.status(401).json({ error: 'Unauthorized access.' });

            const result = await this.classService.getEnrolledClasses(userId);
           res.json(result);
        } catch (error) {
            res.status(500).json({ error: error.message });
        }
    }

    async dropClass(req, res) {
        try {
            const userId = req.cookies.id;
            const { offeringId } = req.body;

            if (!userId) {
                return res.status(401).json({ error: 'Unauthorized access.' });
            }
            if (!offeringId) {
                return res.status(400).json({ error: 'Offering ID is required.' });
            }

            const message = await this.classService.dropClass(userId, offeringId);
            res.json({ success: true, message });
        } catch (error) {
            console.error('Error in dropClass:', error.message);
            res.status(500).json({ error: error.message });
        }
    }
    async getCourses(req, res) {
        try {
            const result = await this.classService.getCourses();
            return res.json(result);
        } catch (error) {
            res.status(500).json({ error: error.message });
        }
    }

    async getInstructors(req, res) {
        try {
            const result = await this.classService.getInstructors();
            return res.json(result);
        } catch (error) {
            res.status(500).json({ error: error.message });
        }
    }

    async addNewCourse(req, res) {

        const { courseName, courseCode, description, credits, prerequisites } = req.body;

        let checkedPrerequisites = prerequisites;

        if (!prerequisites || prerequisites.length == 0) {
            checkedPrerequisites = 'None';
        }
        else {

            checkedPrerequisites = prerequisites.join(',');
        }

        try {
            const result = await this.classService.addNewCourse(courseName, courseCode, description, credits, checkedPrerequisites);
            res.status(200).json(result);
        } catch (error) {
            console.error('Error in ClassController.addNewCourse:', error.message);
            res.status(500).json({ message: 'Error adding new Course.', error: error.message });
        }
    }

    async addNewOffering(req, res) {
        const { courseId, instructor, maxStudents, status, dayOfWeek, time } = req.body;
        console.log('Received Data:', { courseId, instructor, maxStudents, status, dayOfWeek, time }); // Log the extracted data
        try {
            const result = await this.classService.addNewOffering(courseId, instructor, maxStudents, status, dayOfWeek, time);
            console.log('Result from Service Layer:', result); // Log the service layer result
            res.status(200).json(result);
        } catch (error) {
            console.error('Error in ClassController.addNewOffering:', error.message);
            res.status(500).json({ message: 'Error adding new Course Offering.', error: error.message });
        }
    }

    async addInstructorToEnrollment(req, res) {
        const { courseId, instructorId } = req.body;
        try {
            const result = await this.classService.addInstructorToEnrollment(courseId, instructorId);
            res.status(200).json({ message: 'Instructor added to enrollment table', result });
        } catch (error) {
            console.error('Error in adding instructor to enrollment:', error.message);
            res.status(500).json({ message: 'Internal Server Error' });
        }
    }

    async getAllCourseOfferings(req,res) {
        try {

            const result = await this.classService.getAllCourseOfferings();
            res.json(result);
        } catch (error) {
            res.status(500).json({ error: error.message });
        }
    }


    async getGrades(req, res) {
        const userId = req.cookies.id;
        console.log('Fetched userId:', userId);

        if (!userId) {
            return res.status(401).json({ error: 'Unauthorized access.' });
        }

        try {
            const result = await this.classService.getGrades(userId);
            console.log('Service result:', result);
            return result;
        } catch (error) {
            console.error('Error getting grades:', error);
            res.status(500).json({ error: 'Failed to fetch grades' });
        }
    }







}
module.exports = ClassController