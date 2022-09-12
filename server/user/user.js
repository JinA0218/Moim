import express from 'express';
import { hash_password, verify } from './encrypt.js';
import debug from '../debug.js';
import {connection, app, io, server} from '../config/connection.js';
import {getPartyList, joinParty, leaveParty, getParty, deleteParty} from '../party/partyList.js';
import {getPartyUserList, partyChatList} from '../chatting/partyUserList.js'
import {myParty} from '../party/my/myParty.js';
import {Like, LikedParty} from '../party/my/myLikedParty.js';
import {createTaxiParty} from '../party/taxiParty/createTaxiParty.js';
import {editTaxiParty} from '../party/taxiParty/editTaxiParty.js';
import {createMealParty} from '../party/mealParty/createMealParty.js';
import {editMealParty} from '../party/mealParty/editMealParty.js';
import {createNightMealParty} from '../party/nightMealParty/createNightMealParty.js';
import {editNightMealParty} from '../party/nightMealParty/editNightMealParty.js';
import {createStudyParty} from '../party/studyParty/createStudyParty.js';
import {editStudyParty} from '../party/studyParty/editStudyParty.js';
import {createCustomParty} from '../party/customParty/createCustomParty.js';
import {editCustomParty} from '../party/customParty/editCustomParty.js';



global.global_id=0;
global.chat_id=0;
var id_array=[0,0,0,0,0];//order same as table order : taxi, meal, etc

const port=process.env.PORT||80;

connection.connect();//필요??
  

app.use(express.json());

//socket.io
let a=0;

io.on('connection',(socket)=>{
    debug('a user connected');

    // // //join chatting room
    // socket.on('joinChat', chatItem=>{
    // a=num;
    // socket.join(chatItem.party_id,()=>{
    //     debug(`USRID : ${chatItem.userid}, USERNAME : ${chatItem.username} joined room PARTYID : ${party_id}`);
    //     io.to(chatItem.party_id).emit('joinChatting',chatItem);
    // })
    // })

    // //leave chatting room
    // socket.on('leaveChat', chatItem=>{
    //     socket.leave(chatItem.party_id,()=>{
    //     debug(`USRID : ${chatItem.userid}, USERNAME : ${chatItem.username} left room PARTYID : ${party_id}`);
    //     io.to(chatItem.party_id).emit('leaveChatting', chatItem);
    //     })
    // })
    
    //getting user_info -> 받은 party를 바탕으로 소켓 join
    socket.on('iAm',user_info=>{
        debug(`USER INFO : ${JSON.stringify(user_info)}`);
        //socket이 어떤 party에 있는지 알아야
        const party_id=user_info.party_id;
        const userid=user_info.userid;

        if (party_id!==undefined && party_id!==null&& userid!==undefined &&userid!==null){

            socket.join(String(party_id));
            socket.on('chatMessage',chatItem=>{
                //insert chatItem to chatRoom
                chatItem=JSON.parse(chatItem)
                chatItem.chat_id=chat_id++;
                debug(`CHAT ITEM : ${JSON.stringify(chatItem)}`);
                connection.query('insert into chatRoom(chat_id, chat_type, party_id, userid, username, chat_content, chat_time, chat_date)\
                values (?,?,?,?,?,?,?,?)',[chatItem.chat_id, chatItem.chat_type, chatItem.party_id, chatItem.userid, chatItem.username, chatItem.chat_content, chatItem.chat_time, chatItem.chat_date], async(error, rows, field)=>{
                    if (error){
                        // Query error again..
                        debug("chatMessage_chatRoom failed due to query error 4");
                        debug(error.message);
                    }
                    else{
                        debug(`chatMessage_chatRoom ${JSON.stringify(chatItem)} successfully registered.`);
                        debug(`Rows : ${JSON.stringify(rows)}`);
                    }
                })
                io.to(String(party_id)).emit('chatMessage',chatItem);

            });
        }
        
    })
    
    socket.on('disconnect',()=>{
        debug('user disconnected');
    });
})





//test
app.get('/',(req, res)=>{
    // res.status(200).sendFile(path.join(__dirname, "test.html"))
    res.send("<h1>Hello in user.js<h1>")
});

//update global variable global_id
const party=['taxi_party', 'meal_party','night_meal_party','study_party','custom_party']
for(let i=0;i<5;i++){
    connection.query('select party_id from ?? order by party_id desc limit 1;',[party[i]],async(error, rows, field)=>{
        debug(`Rows in user.js for global id : ${JSON.stringify(rows)}`)
        if(error){
            // Query error.->mysql error: not null but null or not database or no table etc
            debug("GETTING PARTY ID failed due to query error.");
            debug(error.message);
        }
        else if(rows.length==0){//taxi_party has no element
            debug(`There is no party_id in ${party[i]}. Set global_id to 0`);
            id_array[i]=0;
        }
        else{//taxi_party has more than 1 element
            debug(`Set taxi_global_id to the rows length to ${rows.length}.`);
            id_array[i]=rows[0].party_id;
        }
        if (i==4){
            global_id=Math.max(...id_array);
            global_id++;
            debug(`GLOBAL_ID CHECK!!!! : ${global_id}`)
        }
    })
}

//update global variable chat_id
connection.query('select chat_id from chatRoom order by chat_id desc limit 1;',async(error, rows, field)=>{
    debug(`Rows in user.js for chat_id : ${JSON.stringify(rows)}`)
    if(error){
        // Query error.->mysql error: not null but null or not database or no table etc
        debug("GETTING CHAT ID failed due to query error.");
        debug(error.message);
    }
    else if(rows.length==0){//taxi_party has no element
        debug(`There is no chat_id in chatRoom. Set chat_id to 0`);
        chat_id=0;
    }
    else{//taxi_party has more than 1 element
        debug(`Set taxi_global_id to the rows length to ${rows.length}.`);
        chat_id=rows[0].chat_id;
        chat_id++;
        debug(`CHAT_ID CHECK!!!! : ${chat_id}`)
    }
})


//login
app.post("/login", (req, res)=>{
    const userid=req.body.userid;
    const pw=req.body.pw;

    debug(`POST /login\t${userid}, ${pw}`);

    if (userid != undefined && pw != undefined) {
        connection.query('select * from users where userid=?', [userid], async (error, rows, field) => {
            if (error) {
                // Query error.->mysql error: not null but null or not database or no table etc
                debug("Login failed due to query error.");
                debug(error.message);
                res.status(400).send(error.message);
            }
            else if (rows.length == 0) {
                debug("There is no such user, or password is incorrect.");
                res.status(400).send("There is no such user, or password is incorrect.");
            }
            else {
                debug("Rows : "+JSON.stringify(rows));
                const user_info = rows[0];
                debug("Rows[0] : "+JSON.stringify(rows[0]));
                if (await verify(pw, user_info.salt, user_info.pw)) {
                    debug("Login success.");
                    res.status(200).send({msg: "Login succeeded.", username: user_info.username});
                }
                else {
                    debug("There is no such user, or password is incorrect.");
                    res.status(400).send("There is no such user, or password is incorrect.");
                }
            }
        });
    }
    else {
        res.status(400).send("Bad request body; you must include userid and pw.");
    }


});

//register
app.post("/register", (req, res) => {
    const userid = req.body.userid;
    const pw = req.body.pw;
    const username=req.body.username;
    const age=req.body.age;
    const place1=req.body.place1;
    const place2=req.body.place2;
    const place3=req.body.place3;
    

    debug(`POST /register\t${userid}, ${pw}`);

    if (userid !== undefined && pw !== undefined) {
        connection.query('select userid from users where userid=?', [userid], async (error, rows, field) => {
            if (error) {
                // Query error.
                debug("Register failed due to query error 1");
                debug(error.message);
                res.status(400).send(error.message);
            }
            else if (rows.length > 0) {
                debug(`User ID ${userid} already exists.`);
                res.status(400).send("The ID already exists.");
            }
            else {
                debug("OK, you can use this userid..");
                const hashed_pw = await hash_password(pw);
                connection.query('insert into users(username, userid, pw, age, place1, place2, place3, salt) values(?, ?, ?,?,?,?,?,?)', [username, userid, hashed_pw.hashed_pw, age, place1, place2, place3, hashed_pw.salt], (error, rows, field) => {
                    if (error) {
                        // Query error again..
                        debug("Register failed due to query error 2");
                        debug(error.message);
                        res.status(400).send(error.message);
                    }
                    else {
                        debug(`User ${userid}, successfully registered.`);
                        // Redirect to login page.
                        res.status(200).send("Register succeeded.");
                    }
                })
            }
        })
    }
    else {
        res.status(400).send("Bad request body; you must include userid, pw, and username etc.");
    }
});

//register/duplicate_id
app.get("/register/duplicate-id", (req, res) => {
    const userid = req.query.userid;
    debug("QUERY!!!"+JSON.stringify(req.query));

    debug(`GET /register/duplicate-id\t${userid}`);

    if (userid !== undefined) {
        connection.query('select userid from users where userid=?', [userid], async (error, rows, field) => {
            if (error) {
                // Query error.
                debug("Register/duplicate-id failed due to query error 1");
                debug(error.message);
                res.status(400).send(error.message);
            }
            else if (rows.length > 0) {//rows: connection query의 결과
                debug(`User ID ${userid} already exists.`);
                debug(`Rows : ${JSON.stringify(rows)}`);
                res.status(400).send("The ID already exists.");
            }
            else {
                debug("OK, you can use this userid..");
                res.status(200).send("You can use this ID.");
            }
        })
    }
    else {
        res.status(400).send("Bad request; you must include userid.");
    }
});

//when the user clicked a partyList row, send the username to the client
app.get("/username",(req, res)=>{
    const userid=req.query.userid;
    
    debug(`GET /username\tuserid: ${userid}`);

    if (userid != undefined ) {
        connection.query('select username from users where userid=?',[userid], async(error, rows, field)=>{
            if (error) {
                // Query error.->mysql error: not null but null or not database or no table etc
                debug("getting username failed due to query error.");
                debug(error.message);
                res.status(400).send(error.message);
            }
            else if(rows.length==0){
                debug("There is no such userid.");
                res.status(400).send("There is no such userid.");
            }
            else {
                debug("Rows : "+JSON.stringify(rows));//rows: 배열->해당 party들의 배열
                debug(`getting username success. sending : ${JSON.stringify(rows[0])}`);
                res.status(200).send(rows[0]);
            }
        });
    }
    else{
        res.status(400).send("Bad request body; you must include userid.");
    }
});

//show partyList
app.get("/party-list/:type", getPartyList);

//insert new (party_id, userid) into party_user
app.post("/join-party/:type",joinParty);

//delete (party_id, userid) in party_user
app.post("/leave-party",leaveParty);

//create new taxi-party (insert into taxi_party table)
app.post("/create-party/taxi-party", createTaxiParty);

//edit taxi-party
app.post("/edit-party/taxi-party", editTaxiParty);

//get : 성혁오빠가 요청 보냈을 때->full description 정보들 보여주기//party_id(url: party_id), type query string으로 받음.
app.get("/party", getParty);

//  CHATTING
//get party-user-list
app.get("/party-user-list/:party_id", getPartyUserList);//get party-id from client

//delete type (taxi-party, meal-party etc)
app.post('/delete-party/:type', deleteParty);

//showing my party
app.get('/my-party/:userid',myParty);

//like
app.post('/like',Like);

//liked-party
app.get("/liked-party/:userid",LikedParty);

//party-chat-list
app.get('/party-chat-list',partyChatList);

/// MEAL
//create-party
app.post("/create-party/meal-party", createMealParty);

//edit-party
app.post("/edit-party/meal-party", editMealParty);

//NIGHT_MEAL
//create-party
app.post("/create-party/night-meal-party", createNightMealParty);

//edit-party
app.post("/edit-party/night-meal-party", editNightMealParty);

//STUDY
//create-party
app.post("/create-party/study-party", createStudyParty);

//edit-party
app.post("/edit-party/study-party", editStudyParty);

//CUSTOM
//create-party
app.post("/create-party/custom-party", createCustomParty);

//edit-party
app.post("/edit-party/custom-party", editCustomParty);


// connection.end();

server.listen(port,()=>{
    console.log("<> Server start. Running on port " +port);
})//원래 app 이었음

