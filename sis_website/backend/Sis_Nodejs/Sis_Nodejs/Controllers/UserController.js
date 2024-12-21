const bcrypt = require('bcrypt');

class UserController {
    constructor(userService) {
        this.userService = userService;
    }

    async login(req, res) {
        const { id, password } = req.body;
        try {
            const user = await this.userService.login(id, password);
            return res.status(200).json({
                message: 'Login successful',
                user,
                role: user.status,
            });
        } catch (error) {
            if (error.message === 'User not found') {
                return res.status(404).json({ message: 'User ID does not exist' });
            }
            if (error.message === 'Incorrect password') {
                return res.status(401).json({ message: 'Password is incorrect' });
            }
            console.error('Error in UserController.login:', error);
            return res.status(500).json({ message: 'An error occurred', error: error.message });
        }
    }

    async register(req, res) {
        const { lname, fname, password, role } = req.body;
        try {
            await this.userService.register(lname, fname, password, role);
            return res.status(200).json({
                message: 'Register successful'
            });
        } catch (error) {
            if (error.message === 'Password less than 8 characters') {
                return res.status(401).json({ message: 'Password less than 8 characters' });
            }

            else if (error.message === 'Password must start with a capital letter') {
                return res.status(401).json({ message: 'Password must start with a capital letter' });
            }


            else if (error.message === 'Password must contain at least one character or number') {
                return res.status(401).json({ message: 'Password must contain at least one character or number' });
            }
            console.error('Error in UserController.register:', error);
            return res.status(500).json({ message: 'An error occurred', error: error.message });
        }
    }

    async getProfileInfos(userId) {
        try {
            if (!userId) {
                throw new Error('User ID is required.');
            }

            const userProfile = await this.userService.getProfileInfos(userId);

            if (!userProfile) {
                throw new Error('User not found.');
            }

            return userProfile; 
        } catch (error) {
            console.error('Error in UserController.getProfileInfos:', error.message);
            throw error; 
        }
    }


    async getStudents(req, res) {
        try {
            const result = await this.userService.getStudents();
            return res.json(result);
        } catch (error) {
            res.status(500).json({ error: error.message });
        }
    }

    async addTuitionFees(req, res) {
        try { 
            await this.userService.addTuitionFees();
            res.status(200);
        } catch(error) {
            console.error('Error in UserController.getProfileInfos:', error.message);
            throw error;
        }
    }

    async changePassword(req, res) {
        try {
            const { oldPassword, newPassword } = req.body;

            const userId = req.cookies.id;

            if (!userId) {
                throw new Error('User not authenticated.');
            }

            if (!oldPassword || !newPassword) {
                throw new Error('Old password and new password are required.');
            }


            await this.userService.changePassword(userId, oldPassword, newPassword);

            return res.status(200).json({ message: 'Password updated successfully.' });
        } catch (error) {
            console.error('Error in UserController.changePassword:', error.message);

            if (error.message === 'Incorrect old password') {
                return res.status(400).json({ message: 'Old password is incorrect.' });
            }

            return res.status(500).json({ message: 'Internal server error.', error: error.message });
        }
    }



    //async getFinancialData(req) {
    //    try {
    //        const userId = req.cookies.id;
    //        const data = await this.userService.getFinancialData(userId);
    //        return { tuitionFee: data.tuition }; // Return data here, not send response
    //    } catch (error) {
    //        console.error('Error in getFinancialData controller:', error);
    //        throw new Error('Failed to get financial data');
    //    }
    //}


    //async makePayment(req, res) {
    //    const { amount } = req.body;
    //    const userId = req.cookies.id;

    //    try {
    //        const updatedData = await this.userService.makePayment(userId, amount);
    //        res.status(200).json(updatedData); // Directly send the updated data
    //    } catch (error) {
    //        console.error('Error in makePayment controller:', error);
    //        res.status(500).json({ success: false, message: error.message });
    //    }
    //}




}

module.exports = UserController;
