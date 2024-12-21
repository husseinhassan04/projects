class CartService {
    constructor(cartRepository) {
        this.cartRepository = cartRepository;
    }

    async addToCart(userId,offeringId) {
        try {

            const isInCart = await this.cartRepository.isCourseInCart(userId, offeringId);
            if (isInCart) {
                throw new Error('Course is already in the cart.');
            }
            return await this.cartRepository.addToCart(userId,offeringId);
        } catch (err) {
            console.error('Error in CartService.addToCart:', err.message);
            throw new Error('Error adding course to cart }');
        }
    }

    async removeFromCart(offeringId) {
        try {
            return await this.cartRepository.removeFromCart(offeringId);
        } catch (err) {
            console.error('Error in CartService.removeFromCart:', err.message);
            throw new Error('Error removing course from cart }');
        }
        }

    async getCartItems(userId) {
        try {
            return await this.cartRepository.getCartItems(userId);
        } catch (err) {
            console.error('Error in CartService.getCartItems:', err.message);
            throw new Error('Error fetching cart items');
        }
    }

    async isCourseInCart(userId, courseId) {
        try {
            return await this.cartRepository.isCourseInCart(userId, courseId);
        } catch (err) {
            console.error('Error in CartService.isCourseInCart:', err.message);
            throw new Error('Error checking if course is in cart');
        }
    }

    async checkCoursesInCart  (userId,courseIds)  {
        const cartStatuses = {};
        for (const courseId of courseIds) {
            const isInCart = await this.cartRepository.isCourseInCart(userId,courseId);
            cartStatuses[courseId] = isInCart;
        }
        return cartStatuses;
    };

    async enroll(userId, offeringId) {
        try {
            return await this.cartRepository.enroll(userId, offeringId);
        } catch (err) {
            console.error('Error in CartService.enroll:', err.message);
            throw err;
        }
    }

}

module.exports = CartService;