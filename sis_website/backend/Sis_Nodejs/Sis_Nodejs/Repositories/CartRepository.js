const mySql = require('../DbConnection');
const util = require('util');
const query = util.promisify(mySql.con.query).bind(mySql.con);

class CartRepository {
    constructor(cartService) {
        this.cartService = cartService;
    }

    async addToCart(userId, offeringId) {
        try {
            const getCourseQuery = "SELECT * FROM course_offering WHERE offering_id = ?";
            const courseResult = await query(getCourseQuery, [offeringId]);

            if (courseResult.length === 0) {
                throw new Error('Offering not found.');
            }

            const courseId = courseResult[0].course_id; 

            const existsQuery = "SELECT * FROM cart WHERE userId = ? AND courseId = ? AND offeringId = ?";
            const existing = await query(existsQuery, [userId, courseId, offeringId]);

            if (existing.length > 0) {
                throw new Error('Course already in cart');
            }

            const queryText = "INSERT INTO cart (userId, offeringId, courseId) VALUES (?, ?, ?)";
            return await query(queryText, [userId, offeringId, courseId]); 
        } catch (error) {
            console.error('Error in CartRepository.addToCart:', error.message);
            throw error;
        }
    }


    async getCartItems(userId) {
        try {
            // Step 1: Fetch offeringId from cart
            const getCartOfferingsQuery = `
            SELECT id AS cartId, offeringId 
            FROM cart 
            WHERE userId = ?;
        `;

            const cartOfferings = await query(getCartOfferingsQuery, [userId]);

            if (cartOfferings.length === 0) return [];


            // Step 2: Fetch offering details
            const offeringIds = cartOfferings.map(item => item.offeringId);
            const getCourseOfferingsQuery = `
            SELECT 
                offering_id AS offeringId,
                course_id AS courseId, 
                instructor, 
                max_students AS maxStudents, 
                status, 
                day_of_week AS dayOfWeek, 
                time 
            FROM course_offering 
            WHERE offering_id IN (?);
        `;
            const courseOfferings = await query(getCourseOfferingsQuery, [offeringIds]);

            // Step 3: Fetch course details
            const courseIds = courseOfferings.map(item => item.courseId);
            const getCourseDetailsQuery = `
            SELECT 
                course_id AS courseId, 
                course_code AS courseCode, 
                course_name AS courseName 
            FROM courses 
            WHERE course_id IN (?);
        `;
            const courseDetails = await query(getCourseDetailsQuery, [courseIds]);

            // Step 4: Merge results
            const cartItems = cartOfferings.map(cartItem => {
                const offering = courseOfferings.find(item => item.offeringId === cartItem.offeringId);
                const course = courseDetails.find(item => item.courseId === offering.courseId);

                return {
                    cartId: cartItem.cartId,
                    userId: userId,
                    offeringId: offering.offeringId,
                    courseId: offering.courseId,
                    courseCode: course.courseCode,
                    courseName: course.courseName,
                    instructor: offering.instructor,
                    maxStudents: offering.maxStudents,
                    status: offering.status,
                    dayOfWeek: offering.dayOfWeek,
                    time: offering.time
                };
            });

            return cartItems;
        } catch (error) {
            console.error('Error in CartRepository.getCartItems:', error);
            throw error;
        }
    }



    async isCourseInCart(userId, offeringId) {
        try { 
        const queryText = "SELECT * FROM cart WHERE userId = ? AND offeringId = ?";
        const result = await query(queryText, [userId, offeringId]);

            return result.length > 0;

        } catch (error) {
            console.error('Error in CartRepository.isCourseInCart:', error);
            throw error;
        }
    }

    async removeFromCart(offeringId) {
        try {
            const queryText = 'DELETE FROM cart WHERE offeringId = ?';
            await query(queryText,[offeringId]);

        } catch (error) {

            console.error('Error in CartRepository.removeFromCart:', error.message);
            throw error;
        }
    }



    async enroll(userId, offeringId) {
        try {
            // Step 1: Check if the user is already enrolled in the course offering
            const checkQuery = "SELECT * FROM enrollments WHERE userId = ? AND offeringId = ?";
            const existing = await query(checkQuery, [userId, offeringId]);

            if (existing.length > 0) {
                console.log('User is already enrolled in this course.');
                throw new Error('User is already enrolled in this course.');
            }

            // Step 2: Get the courseId from the cart based on offeringId
            const getCourseIdFromCartQuery = "SELECT courseId FROM cart WHERE userId = ? AND offeringId = ?";
            const cartResult = await query(getCourseIdFromCartQuery, [userId, offeringId]);

            if (cartResult.length === 0) {
                console.log('Course not found in cart.');
                throw new Error('Course not found in cart.');
            }

            const courseId = cartResult[0].courseId;
            console.log('Course ID from cart:', courseId);

            // Step 3: Get course and its prerequisites
            const courseQuery = `
        SELECT co.offering_id, co.day_Of_Week, co.time, c.credits, c.prerequisites, co.max_students
        FROM course_offering co
        JOIN courses c ON co.course_id = c.course_id
        WHERE co.offering_id = ?`;
            const course = await query(courseQuery, [offeringId]);

            if (!course || course.length === 0) {
                console.log('Course offering not found.');
                throw new Error('Course offering not found.');
            }

            const { day_Of_Week, time, credits, prerequisites,max_students } = course[0];
            console.log('Course details:', { day_Of_Week, time, credits, prerequisites, max_students });

            if (!day_Of_Week || !credits || !prerequisites || max_students === undefined) {
                console.log('Missing important course details (day_Of_Week, credits, prerequisites, or capacity).');
                throw new Error('Course details are incomplete.');
            }

            // Step 4: Check if prerequisites are met
            if (prerequisites !== "None") {
                const prereqs = prerequisites.split(',');

                // Loop through each prerequisite and check if the user is enrolled
                for (const prereq of prereqs) {
                    const prereqQuery = `
                SELECT e.courseId
                FROM enrollments e
                JOIN courses c ON e.courseId = c.course_id
                WHERE e.userId = ? AND c.course_code = ?`;
                    const enrolledPrereq = await query(prereqQuery, [userId, prereq.trim()]);

                    if (enrolledPrereq.length === 0) {
                        console.log(`User has not completed the prerequisite: ${prereq.trim()}`);
                        throw new Error(`User has not completed the prerequisite: ${prereq.trim()}`);
                    }
                    console.log(`User has completed prerequisite: ${prereq.trim()}`);
                }
            }

            // Step 5: Check for time conflicts with already enrolled courses
            const enrolledCoursesQuery = `
        SELECT o.course_id, o.day_Of_Week, o.time
        FROM enrollments e
        JOIN course_offering o ON e.offeringId = o.offering_id
        WHERE e.userId = ?`;
            const enrolledCourses = await query(enrolledCoursesQuery, [userId]);

            // Check for time conflicts and same course
            for (const enrolledCourse of enrolledCourses) {
                if (enrolledCourse.course_id == courseId) {
                    console.log('Already enrolled in a similar course.');
                    throw new Error('Already enrolled in a similar course.');
                }

                if (enrolledCourse.day_Of_Week == day_Of_Week) {
                    const enrolledStartTime = this.parseTimeToMinutes(enrolledCourse.time);
                    const offeringStartTime = this.parseTimeToMinutes(time);

                    if (this.isTimeConflict(enrolledStartTime, enrolledCourse.credits, offeringStartTime, credits)) {
                        console.log('Time conflict with another enrolled course.');
                        throw new Error('Time conflict with another enrolled course.');
                    }
                }
            }

            // Step 6: Check total credits (do not exceed 21 credits)
            const totalCreditsQuery = `
        SELECT SUM(c.credits) AS totalCredits
        FROM courses c
        JOIN course_offering o ON c.course_id = o.course_id
        JOIN enrollments e ON o.offering_id = e.courseId
        WHERE e.userId = ?`;
            const totalCreditsResult = await query(totalCreditsQuery, [userId]);
            const totalCredits = totalCreditsResult[0].totalCredits || 0;
            console.log('Total credits:', totalCredits);

            if (totalCredits > 21) {
                console.log('Total credits exceed 21 credits.');
                throw new Error('Total credits exceed 21 credits.');
            }

            // Step 7: Check the number of students already enrolled in the course
            const enrollmentCountQuery = "SELECT COUNT(*) AS enrolledCount FROM enrollments WHERE offeringId = ?";
            const enrollmentCountResult = await query(enrollmentCountQuery, [offeringId]);
            const enrolledCount = enrollmentCountResult[0].enrolledCount;

            if (enrolledCount >= max_students +1) {
                console.log('Course is full. Cannot enroll.');
                throw new Error('Course is full. Cannot enroll.');
            }

            // Step 8: Enroll the user in the course
            const enrollQuery = "INSERT INTO enrollments (userId, courseId, offeringId) VALUES (?, ?, ?)";
            await query(enrollQuery, [userId, courseId, offeringId]);

            // Step 9: Remove from cart
            const removeFromCartQuery = "DELETE FROM cart WHERE userId = ? AND offeringId = ?";
            await query(removeFromCartQuery, [userId, offeringId]);

            return { success: true, message: 'Enrollment successful.' };
        } catch (error) {
            console.error('Error in CartRepository.enroll:', error.message);
            throw error;
        }
    }




    parseTimeToMinutes(time) {
        const [hours, minutes, seconds] = time.split(':').map(Number);
        return (hours * 60) + minutes + (seconds / 60);
    }


    isTimeConflict(start1, duration1, start2, duration2) {
        const end1 = start1 + duration1*60;
        const end2 = start2 + duration2*60;

        return !(end1 <= start2 || end2 <= start1);
    }





}

module.exports = CartRepository;