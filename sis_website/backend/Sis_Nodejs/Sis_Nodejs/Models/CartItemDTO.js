class CartItemDTO {
    constructor(cartId, userId, courseId, offeringId, courseCode, courseName, instructor, maxStudents, status, dayOfWeek, time) {
        this.cartId = cartId;
        this.userId = userId;
        this.courseId = courseId;
        this.offeringId = offeringId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.instructor = instructor;
        this.maxStudents = maxStudents;
        this.status = status;
        this.dayOfWeek = dayOfWeek;
        this.time = time;
    }
}



module.exports = CartItemDTO;