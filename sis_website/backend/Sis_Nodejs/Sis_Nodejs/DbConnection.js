const mysql = require('mysql2');
const con = mysql.createConnection({
    host: 'localhost',
    user: 'root',
    password: 'Zouz@5201314',
    database: 'sis'
});

con.connect((err) => {
    if (err) {
        console.error("Error connecting to MySQL:", err);
        process.exit(1);
    } else {
        console.log("Connected to MySQL.");
    }
});


module.exports = { con };