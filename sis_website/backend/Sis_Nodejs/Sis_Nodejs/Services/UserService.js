const bcrypt = require('bcrypt');


class UserService {
    constructor(userRepository) {
        this.userRepository = userRepository;
    }

    async login(id, password) {
        if (!id || !password) {
            throw new Error('Id and password must be provided');
        }

        const authResult = await this.userRepository.authenticate(id, password);

        if (!authResult.userExists) {
            throw new Error('User not found');
        }

        if (!authResult.isPasswordCorrect) {
            throw new Error('Incorrect password');
        }

        const user = authResult.user;
        return user;
    }
    async register(lname, fname, password, role) {
        if (password.length < 8) {
            throw new Error('Password must be at least 8 characters long');
        }

        if (!/^[A-Z]/.test(password)) {
            throw new Error('Password must start with a capital letter');
        }

        if (!/[a-zA-Z0-9]/.test(password)) {
            throw new Error('Password must contain at least one character or number');
        }

        const result = await this.userRepository.register(lname, fname, password, role);
        return result;
    }

    async getProfileInfos(userId) {
        try {
            const userProfile = await this.userRepository.getProfileInfos(userId);

            if (!userProfile) {
                throw new Error('User not found.');
            }

            return userProfile;
        } catch (error) {
            console.error('Error in UserService.getProfileInfos:', error.message);
            throw error;
        }
    }


    async getStudents() {
        const courses = await this.userRepository.getStudents();
        return courses;
    }

    async addTuitionFees() {
        try {
            await this.userRepository.addTuitionFees();
        } catch(error) {
            console.error('Error in UserService.addTuitionFees():', error.message);
        throw error;
    }
    }

    async changePassword(userId, oldPassword, newPassword) {
        try {
            if (!userId || !oldPassword || !newPassword) {
                throw new Error('User ID, old password, and new password are required.');
            }

            const user = await this.userRepository.getUserById(userId);

            if (!user) {
                throw new Error('User not found.');
            }

            const isOldPasswordCorrect = await bcrypt.compare(oldPassword, user.password);

            console.log('Is old password correct:', isOldPasswordCorrect);

            if (!isOldPasswordCorrect) {
                throw new Error('Incorrect old password');
            }


            await this.userRepository.updatePassword(userId, newPassword);

            console.log(`Password changed successfully for user ID: ${userId}`);
        } catch (error) {
            console.error('Error in UserService.changePassword:', error.message);
            throw error;
        }
    }


    //async getFinancialData(userId) {
    //    try {
    //        const userData = await this.userRepository.getUserFinancialData(userId);
    //        return { tuitionFee: userData.tuition }; // Standardize response
    //    } catch (error) {
    //        throw new Error('Error in getting financial data: ' + error.message);
    //    }
    //}


    //async makePayment(userId, amount) {
    //    try {
    //        const userData = await this.userRepository.getUserFinancialData(userId);
    //        const tuitionFee = userData.tuition;

    //        if (amount <= 0 || amount > tuitionFee) {
    //            throw new Error('Invalid payment amount.');
    //        }

    //        const remainingBalance = tuitionFee - amount;
    //        await this.userRepository.updateFinancialData(userId, remainingBalance);

    //        return { tuitionFee: remainingBalance }; // Consistent key in response
    //    } catch (error) {
    //        throw new Error('Error in processing payment: ' + error.message);
    //    }
    //}




}



module.exports = UserService;
