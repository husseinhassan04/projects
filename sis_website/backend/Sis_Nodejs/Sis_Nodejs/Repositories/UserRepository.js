const mySql = require('../DbConnection');
const util = require('util');

const bcrypt = require('bcrypt');

const query = util.promisify(mySql.con.query).bind(mySql.con);

class UserRepository {
    async authenticate(id, password) {
    try {
        const queryText = 'SELECT * FROM users WHERE id = ?';
        const result = await query(queryText, [id]);

        if (result.length === 0) {
            return { userExists: false };
        }

        const user = result[0];
        const isPasswordCorrect = await bcrypt.compare(password, user.password);

        if (isPasswordCorrect) {
            return { userExists: true, isPasswordCorrect: true, user };
        } else {
            return { userExists: true, isPasswordCorrect: false };
        }

    } catch (error) {
        console.error('Error in UserRepository.authenticate:', error);
        throw error;
    }
    }


    async register(lname, fname, password, role) {
        try {
            const insertQuery = 'INSERT INTO users (lname, fname, password, role) VALUES (?, ?, ?, ?)';
            const selectQuery = 'SELECT id FROM users WHERE lname = ? AND fname = ? AND password = ? AND role = ?';
            const updateQuery = 'UPDATE users SET email = CONCAT(id, "@hz.edu.lb") WHERE id = ?';

            if (role === "student") {
                role = "user";
            }

            const salt = await bcrypt.genSalt(10);
            const hashedPassword = await bcrypt.hash(password, salt);

            await query(insertQuery, [lname, fname, hashedPassword, role]);

            const rows = await query(selectQuery, [lname, fname, hashedPassword, role]);

            if (rows.length === 0) {
                return { userCreated: false };
            }

            const userId = rows[0].id;

            await query(updateQuery, [userId]);

            return { userCreated: true };
        } catch (error) {
            console.error('Error in UserRepository.register:', error);
            throw error;
        }
    }


    async getProfileInfos(userId) {
        try {
            const getInfosQuery = "SELECT * FROM users WHERE id = ?";
            const getInfosResult = await query(getInfosQuery, [userId]);

            if (getInfosResult.length === 0) { 
                throw new Error('User not found.');
            }

            const userProfile = {
                id: getInfosResult[0].id,
                firstName: getInfosResult[0].fName,
                lastName: getInfosResult[0].lName,
                email: getInfosResult[0].email,
                role: getInfosResult[0].role,
            };

            return userProfile;
        } catch (error) {
            console.error('Error fetching user profile:', error.message);
            throw error;
        }
    }



    async getStudents() {
        const studentRole = 'user';
        const queryText = `select * from users where role=?`;
        return query(queryText, [studentRole]);
    }

    async addTuitionFees() {
        try {
            const queryText = `UPDATE users SET tuition = (SELECT COALESCE(SUM(c.credits * 100), -1)
                                FROM enrollments e
                                JOIN courses c ON e.courseId = c.course_id
                                WHERE e.userId = users.id)
                                WHERE tuition = -1 and role='user' `;

            query(queryText, []);
        } catch (error) {
            console.error('Error in UserRepository.addTuitionFees():', error);
            throw error;
        }
    }

    async getUserById(userId) {
        try {
            if (!userId) {
                throw new Error('User ID is required.');
            }

            const queryText = 'SELECT id, fName, lName, email, password, role FROM users WHERE id = ?';
            const result = await query(queryText, [userId]);

            if (result.length === 0) {
                throw new Error('User not found.');
            }

            return result[0];
        } catch (error) {
            console.error('Error in UserRepository.getUserById:', error.message);
            throw error;
        }
    }

    async updatePassword(id, newPassword) {
        try {
            if (!id || !newPassword) {
                throw new Error('User ID and new password are required.');
            }

            const salt = await bcrypt.genSalt(10);
            const hashedPassword = await bcrypt.hash(newPassword, salt);;

            const updateQuery = 'UPDATE users SET password = ? WHERE id = ?';
            const result = await query(updateQuery, [hashedPassword, id]);
            console.log(hashedPassword);

            if (result.affectedRows === 0) {
                throw new Error('User not found or failed to update password.');
            }

            console.log(`Password updated successfully for user ID: ${ id }`);
        } catch (error) {
            console.error(`Error in UserRepository.updatePassword: ${ error.message }`);
            throw error;
        }
    }



    //async getUserFinancialData(userId) {
    //    try {
    //        if (!userId) {
    //            throw new Error('User ID is required.');
    //        }

    //        const queryText = 'SELECT tuition FROM users WHERE id = ?';
    //        const [results] = await query(queryText, [userId]);

    //        if (results.length === 0) {
    //            throw new Error('User not found');
    //        }

    //        return results[0];
    //    } catch (error) {
    //        console.error(`Error in UserRepository.getUserFinancialData: ${error.message}`);
    //        throw error;
    //    }
    //}

    //async updateFinancialData(userId, remainingBalance) {
    //    try {
    //        const queryText = 'UPDATE users SET tuition = ? WHERE id = ?';
    //        const [results] = await query(queryText, [remainingBalance, userId]);

    //        if (results.affectedRows === 0) {
    //            throw new Error('User not found or failed to update tuition fee.');
    //        }

    //        console.log(`Tuition fee updated for user ID: ${userId}`);
    //    } catch (error) {
    //        console.error(`Error in UserRepository.updateFinancialData: ${error.message}`);
    //        throw error;
    //    }
    //}



}

module.exports = UserRepository;
