const { debug } = require('console');
const express=require('express');
const mysql=require('mysql');
const port=process.env.PORT||80;
const dbconfig=require('./config/database.js')

// const conf=JSON.parse(fs.reaFileSync('conf.json'));

const connection = mysql.createConnection(dbconfig);
connection.connect();//필요??
// connection.query('SELECT * from Users', (error, rows, fields) => {
//     if (error) throw error;
//     console.log('User info is: ', rows);
//   });
  
const app=express();

app.get('/',(req, res)=>{
    // res.status(200).sendFile(path.join(__dirname, "test.html"))
    res.send("<h1>Hello in index.js<h1>")
    // if (id != undefined && pw != undefined) {
    //     connection.query('select pw, salt from users where id=?', [id], async (error, rows, field) => {
    //         if (error) {
    //             // Query error.
    //             debug("Login failed due to query error.");
    //             debug(error.message);
    //             res.status(400).send(error.message);
    //         }
    //         else if (rows.length == 0) {
    //             debug("There is no such user, or password is incorrect.");
    //             res.status(400).send("There is no such user, or password is incorrect.");
    //         }
    //         else {
    //             const user_info = rows[0];
    //             if (await verify(pw, user_info.salt, user_info.pw)) {
    //                 debug("Login success.");
    //                 res.status(200).send("Login succeeded.");
    //             }
    //             else {
    //                 debug("There is no such user, or password is incorrect.");
    //                 res.status(400).send("There is no such user, or password is incorrect.");
    //             }
    //         }
    //     });
    // }
    // else {
    //     res.status(400).send("Bad request body; you must include id and pw.");
    // }
});


// app.post("/api/login", (req, res)=>{
//     const id=req.body.id;
//     const pw=req.body.pw;

//     debug(`POST /login\t${id}, ${pw}`);

//     if (id != undefined && pw != undefined) {
//         connection.query('select pw, salt from users where id=?', [id], async (error, rows, field) => {
//             if (error) {
//                 // Query error.
//                 debug("Login failed due to query error.");
//                 debug(error.message);
//                 res.status(400).send(error.message);
//             }
//             else if (rows.length == 0) {
//                 debug("There is no such user, or password is incorrect.");
//                 res.status(400).send("There is no such user, or password is incorrect.");
//             }
//             else {
//                 const user_info = rows[0];
//                 if (await verify(pw, user_info.salt, user_info.pw)) {
//                     debug("Login success.");
//                     res.status(200).send("Login succeeded.");
//                 }
//                 else {
//                     debug("There is no such user, or password is incorrect.");
//                     res.status(400).send("There is no such user, or password is incorrect.");
//                 }
//             }
//         });
//     }
//     else {
//         res.status(400).send("Bad request body; you must include id and pw.");
//     }


// });

connection.end();

app.listen(port,()=>{
    console.log("<> Server start. Running on port " +port);
})