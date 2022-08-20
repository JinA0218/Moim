import mysql from 'mysql2';
import fs from 'fs';

const conf=JSON.parse(fs.readFileSync('../config/database.json'));

const connection = mysql.createConnection({
    host: conf.host,
    user: conf.user,
    password: conf.password,
    database: conf.database
    });

export {connection};