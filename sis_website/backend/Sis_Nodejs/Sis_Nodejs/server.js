var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
const express = require('express');
const cors = require('cors');
const connectToDatabase = require('./back/mongo');
const cookieParser = require('cookie-parser');
const app = express();
const port = process.env.PORT || 1337;
app.use(cors({
    origin: 'http://localhost:3000',
    credentials: true,
}));
app.use(express.json());
app.use(cookieParser());
//user
const UserRepository = require('./Repositories/UserRepository');
const UserService = require('./Services/UserService');
const UserController = require('./Controllers/UserController');
const userRepository = new UserRepository();
const userService = new UserService(userRepository);
const userController = new UserController(userService);
//course
const CourseOfferingController = require('./Controllers/CourseOfferingController');
const CourseOfferingService = require('./Services/CourseOfferingService');
const CourseOfferingRepository = require('./Repositories/CourseOfferingRepository');
const courseOfferingRepository = new CourseOfferingRepository();
const courseOfferingService = new CourseOfferingService(courseOfferingRepository);
const courseOfferingController = new CourseOfferingController(courseOfferingService);
//cart
const CartController = require('./Controllers/CartController');
const CartService = require('./Services/CartService');
const CartRepository = require('./Repositories/CartRepository');
const cartRepository = new CartRepository();
const cartService = new CartService(cartRepository);
const cartController = new CartController(cartService);
//classes
const ClassController = require('./Controllers/ClassController');
const ClassService = require('./Services/ClassService');
const ClassRepository = require('./Repositories/ClassRepository');
const classRepository = new ClassRepository();
const classService = new ClassService(classRepository);
const classController = new ClassController(classService);
// user classes
const UserClassRepository = require('./Repositories/UserClassRepository');
const UserClassService = require('./Services/UserClassService');
const UserClassController = require('./Controllers/UserClassController');
const userClassRepository = new UserClassRepository();
const userClassService = new UserClassService(userClassRepository);
const userClassController = new UserClassController(userClassService);
app.get('/', (req, res) => {
    res.send("server running..");
});
// Login route
app.post('/login', (req, res) => __awaiter(this, void 0, void 0, function* () {
    const { id } = req.body;
    res.cookie("id", id, {
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        sameSite: 'Strict',
    });
    try {
        yield userController.login(req, res);
    }
    catch (error) {
        res.status(500).json({ message: 'Internal server error', error: error.message });
    }
}));
app.get('/test', (req, res) => {
    res.send(req.cookies.id);
});
app.post('/register', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        yield userController.register(req, res);
    }
    catch (err) {
        console.error('Error in /register:', err.message);
        res.status(500).json({ message: 'Error adding new student.' });
    }
}));
app.post('/addTuitionFees', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        yield userController.addTuitionFees(req, res);
    }
    catch (err) {
        console.error('Error in /addTuitionFees:', err.message);
        res.status(500).json({ message: 'Error adding fees.' });
    }
}));
app.get('/getProfileInfos', (req, res) => __awaiter(this, void 0, void 0, function* () {
    var _a;
    try {
        const userId = (_a = req.cookies) === null || _a === void 0 ? void 0 : _a.id;
        if (!userId) {
            console.error('No User ID found in cookies.');
            return res.status(400).json({ error: 'User ID not found in cookies.' });
        }
        const userProfile = yield userController.getProfileInfos(userId);
        if (!userProfile) {
            console.error('User profile not found for ID:', userId);
            return res.status(404).json({ error: 'User not found.' });
        }
        return res.status(200).json(userProfile);
    }
    catch (err) {
        console.error('Error in /getProfileInfos:', err.message);
        return res.status(500).json({ error: 'Internal server error.' });
    }
}));
app.post('/getCourseOfferings', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        const { keyword } = req.body || '';
        console.log('Received keyword:', keyword);
        if (!keyword) {
            return res.status(400).json({ message: 'Keyword is required.' });
        }
        const courses = yield courseOfferingController.fetchCourses(req, res);
        res.json(courses);
    }
    catch (error) {
        console.error('Error in /getCourseOfferings:', error);
        res.status(500).json({ message: 'Internal server error', error: error.message });
    }
}));
app.post('/addToCart', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        yield cartController.addToCart(req, res);
    }
    catch (err) {
        console.error('Error in /addToCart:', err.message);
        res.status(500).json({ message: 'Error adding course to cart.' });
    }
}));
app.delete('/removeFromCart', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        yield cartController.removeFromCart(req, res);
    }
    catch (err) {
        console.error('Error in /removeFromCart:', err.message);
        res.status(500).json({ message: 'Error removing course from cart.' });
    }
}));
app.get('/isCourseInCart', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        const cartStatuses = yield cartController.checkCoursesInCart(req, res);
        res.status(200).json({ cartStatus: cartStatuses });
    }
    catch (err) {
        console.error('Error in /isCourseInCart:', err.message);
        res.status(500).json({ message: 'Error checking courses in cart.' });
    }
}));
app.get('/getCartItems', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        const userId = req.cookies.id;
        if (!userId) {
            return res.json("id not available");
        }
        const cartItems = yield cartController.getCartItems(userId);
        res.status(200).json(cartItems);
    }
    catch (err) {
        console.error('Error in /getCartItems:', err.message);
        res.status(500).json({ message: 'Error fetching cart items.' });
    }
}));
app.post('/enroll', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        yield cartController.enroll(req, res);
    }
    catch (err) {
        console.error('Error in /enroll:', err.message);
        res.status(500).json({ message: 'Error enrolling course to cart.' });
    }
}));
//manage classes for user
app.get('/getClasses', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        yield classController.getEnrolledClasses(req, res);
    }
    catch (err) {
        console.error('Error in /getClasses:', err.message);
        res.status(500).json({ message: 'Error getting courses from enrollment.' });
    }
}));
app.delete('/dropClass', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        yield classController.dropClass(req, res);
    }
    catch (err) {
        console.error('Error in /dropClass:', err.message);
        res.status(500).json({ message: 'Error removing course.' });
    }
}));
// manage course for admin
app.get('/getCourses', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        const courses = yield classController.getCourses(req, res);
        return courses;
    }
    catch (err) {
        console.error('Error in /getCourses:', err.message);
        res.status(500).json({ message: 'Error getting course.' });
    }
}));
app.get('/getInstructors', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        const instructors = yield classController.getInstructors(req, res);
        return instructors;
    }
    catch (err) {
        console.error('Error in /getInstructors:', err.message);
        res.status(500).json({ message: 'Error getting instructors.' });
    }
}));
app.post('/addNewCourse', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        yield classController.addNewCourse(req, res);
    }
    catch (err) {
        console.error('Error in /addingNewCourse:', err.message);
        res.status(500).json({ message: 'Error adding course.' });
    }
}));
app.post('/addNewOffering', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        console.log('Incoming Request Body:', req.body);
        yield classController.addNewOffering(req, res);
    }
    catch (err) {
        console.error('Error in /addNewOffering route:', err.message);
        res.status(500).json({ message: 'Error adding course offering.' });
    }
}));
app.get('/getStudents', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        const courses = yield userController.getStudents(req, res);
        return courses;
    }
    catch (err) {
        console.error('Error in /getStudents:', err.message);
        res.status(500).json({ message: 'Error getting course.' });
    }
}));
app.delete('/dropCourseFromStudent', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        const result = yield userClassController.dropClassAdmin(req, res);
        if (result.success) {
            return res.status(200).json(result);
        }
        else {
            return res.status(500).json({ message: result.error });
        }
    }
    catch (err) {
        console.error('Error in /dropCourseFromStudent:', err.message);
        return res.status(500).json({ message: 'Error removing course.' });
    }
}));
app.get('/getStudentEnrollments/:studentId', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        const enrollments = yield userClassController.getStudentEnrollments(req, res);
        res.status(200).json(enrollments);
    }
    catch (error) {
        console.error('Error fetching enrollments:', error);
        res.status(500).json({ message: 'Server error' });
    }
}));
app.post('/addCourseToStudent', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        const { studentId, offeringId } = req.body;
        console.log('Received request to enroll student:', { studentId, offeringId });
        if (!studentId || !offeringId) {
            console.error('Missing required data:', { studentId, offeringId });
            return res.status(400).json({ error: 'Missing required data (studentId, offeringId).' });
        }
        yield userClassController.enrollStudentWithOffering(studentId, offeringId);
        console.log('Student enrolled successfully:', { studentId, offeringId });
        res.status(200).json({ success: true, message: 'Course enrolled successfully!' });
    }
    catch (err) {
        console.error('Error in /addCourseToStudent:', err.message, err.stack);
        res.status(500).json({ message: 'Error enrolling in course.' });
    }
}));
app.post('/addInstructorToEnrollment', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        yield classController.addInstructorToEnrollment(req, res);
    }
    catch (err) {
        console.error('Error in /addInstructorToEnrollment:', err.message);
        res.status(500).json({ message: 'Internal Server Error' });
    }
}));
app.post('/modifyCourseOffering', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        const { offeringId, status, dayOfWeek, time, maxStudents } = req.body || '';
        console.log('Received elements:', offeringId, status, dayOfWeek, time, maxStudents);
        if (!status || !dayOfWeek || !time || !maxStudents) {
            return res.status(400).json({ message: 'All elements are required.' });
        }
        const result = yield courseOfferingController.modifyCourseOffering(req, res);
        res.json(result);
    }
    catch (error) {
        console.error('Error in /modifyCourseOffering:', error);
        res.status(500).json({ message: 'Internal server error', error: error.message });
    }
}));
app.delete('/removeCourseOfferingFromSystem', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        const { offeringId } = req.body || '';
        console.log('offering id to delete:', offeringId);
        if (!offeringId) {
            return res.status(400).json({ message: 'offeringId is required.' });
        }
        const result = yield courseOfferingController.removeCourseOfferingFromSystem(req, res);
        res.json(result);
    }
    catch (error) {
        console.error('Error in /removeCourseOfferingFromSystem:', error);
        res.status(500).json({ message: 'Internal server error', error: error.message });
    }
}));
app.post('/changePassword', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        const { oldPassword, newPassword } = req.body;
        const userId = req.cookies.id;
        // Validate required fields
        if (!userId) {
            return res.status(401).json({ message: 'User not authenticated.' });
        }
        if (!oldPassword || !newPassword) {
            return res.status(400).json({ message: 'Old password and new password are required.' });
        }
        // Call the controller's changePassword method
        yield userController.changePassword(req, res);
    }
    catch (error) {
        console.error('Error during password change:', error.message);
        return res.status(500).json({ message: 'Internal server error.', error: error.message });
    }
}));
app.get('/getStudentsForCourse/:offeringId', (req, res) => __awaiter(this, void 0, void 0, function* () {
    const { offeringId } = req.params;
    console.log('Received offeringId:', offeringId); // Debug log
    try {
        const students = yield userClassController.getStudentsForCourse(offeringId);
        console.log('Fetched students:', students); // Debug log
        if (students.length === 0) {
            return res.status(404).json({ message: 'No students found for this course offering' });
        }
        res.status(200).json(students);
    }
    catch (error) {
        console.error('Error fetching students for course:', error);
        res.status(500).json({ message: 'Server error' });
    }
}));
app.post('/updateStudentGrades', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        const result = yield userClassController.updateGrades(req, res);
        res.json({ message: result });
    }
    catch (error) {
        console.error('Error updating grades:', error);
        res.json({ error: 'Failed to update grades' });
    }
}));
app.get('/getStudentGrades', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        const grades = yield classController.getGrades(req, res);
        res.status(200).json(grades);
    }
    catch (error) {
        console.error('Error getting grades:', error);
        res.status(500).json({ error: 'Failed to get grades' });
    }
}));
app.get('/getFinancialData', (req, res) => __awaiter(this, void 0, void 0, function* () {
    try {
        const amount = yield userController.getFinancialData(req, res);
        res.status(200).json(amount);
    }
    catch (error) {
        console.error('Error getting amount:', error);
        res.status(500).json({ error: 'Failed to get financial data' });
    }
}));
app.post('/makePayment', (req, res) => __awaiter(this, void 0, void 0, function* () {
    const { amount } = req.body;
    try {
        const userId = req.cookies.id;
        const data = yield userController.makePayment(userId, amount);
        res.status(200).json(data);
    }
    catch (error) {
        console.error('Error processing payment:', error);
        res.status(500).json({ error: 'Failed to make payment' });
    }
}));
app.listen(port, () => {
    console.log(`Server is running on http://localhost:${port}`);
});
//# sourceMappingURL=server.js.map