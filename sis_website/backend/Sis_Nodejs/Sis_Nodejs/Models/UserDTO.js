class UserDTO {
    constructor(id, firstName, lastName,email, password,status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.status = status
    }
}

module.exports = UserDTO;