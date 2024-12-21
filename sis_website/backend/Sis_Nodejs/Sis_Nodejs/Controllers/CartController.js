class CartController {
    constructor(cartService) {
        this.cartService = cartService;
    }

    async addToCart(req, res) {

        const userId = req.cookies.id;
        const {offeringId } = req.body;
        if (!userId) {
            return res.status(400).json({ message: 'User ID is required.' });
        }


        if (!userId) {
            return res.status(400).json({ message: 'User not authenticated.' });
        }
        if (!offeringId) {
            return res.status(400).json({ message: 'Offering ID is required.' });
        }

        try {
            await this.cartService.addToCart(userId,offeringId);
            res.status(200).json({ message: 'Course added to cart.' });
        } catch (error) {
            console.error('Error in addToCart:', error.message);
            res.status(500).json({ message: 'Internal server error', error: error.message });
        }
    }

    async removeFromCart(req,res) {
        const { offeringId } = req.body;


        try {
            await this.cartService.removeFromCart(offeringId);
            res.status(200).json({ message: 'Course removed from cart.' });
        } catch (error) {
            console.error('Error in removeFromCart:', error.message);
            res.status(500).json({ message: 'Internal server error', error: error.message });
        }

    }

    async getCartItems(userId) {
        try {
            const cartItems = await this.cartService.getCartItems(userId);
            return cartItems;
        } catch (error) {
            console.error('Error in getCartItems:', error.message);
            throw error; // Rethrow to be caught by the endpoint
        }
    }

    async isCourseInCart(req, res) {
        const userId = req.cookies.id;
        const { courseId } = req.query;

        if (!userId || !courseId) {
            return res.status(400).json({ message: 'User ID and Course ID are required.' });
        }

        try {
            const isInCart = await this.cartService.isCourseInCart(userId, courseId);
            res.status(200).json({ isInCart });
        } catch (err) {
            console.error('Error in isCourseInCart:', err.message);
            res.status(500).json({ message: 'Error checking course in cart.' });
        }
    }

    async checkCoursesInCart(req,res) {
        const { courseIds } = req.query;
        if (!courseIds) {
            throw new Error('Missing courseIds parameter.');
        }

        const idsArray = courseIds.split(',');
        const userId = req.cookies.id;

        try {
            // If there's no userId, you can throw an error here
            if (!userId) {
                throw new Error('Missing user ID.');
            }

            const cartStatuses = await this.cartService.checkCoursesInCart(userId, idsArray);
            return cartStatuses; // Just return the cart statuses, no response
        } catch (err) {
            console.error('Error in checkCoursesInCart:', err.message);
            throw new Error('Error checking courses in cart.');
        }
    }


    async enroll(req, res) {
        const userId = req.cookies.id;
        const { offeringId } = req.body;

        if (!userId || !offeringId) {
            return res.status(400).json({ message: 'User ID and Course ID are required.' });
        }

        try {
            const result = await this.cartService.enroll(userId, offeringId);
            res.status(200).json(result);
        } catch (error) {
            console.error('Error in CartController.enroll:', error.message);
            res.status(500).json({ message: 'Error enrolling in course.', error: error.message });
        }
    }

}



module.exports = CartController;