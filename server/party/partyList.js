// import Connection from 'mysql2/typings/mysql/lib/Connection';
import debug from '../debug.js';
import {connection} from '../config/connection.js';
// import { global_id } from '../global.js';

//show party list : only when current_count < maximum_count ->current_count가 넘어갈 가능성이 생기나?
const getPartyList=function (req, res){
    const input_type=req.params.type;
    const type=input_type.replace('-','_');
    
    debug(`GET /party-list\t${type}`);

    if (type != undefined ) {
        var query="";
        var query_list=[];

        switch(type){
            case "taxi_party":
                query='select * from ?? where current_count < maximum_count order by ??, ??, ??';
                query_list=[type,'party_date','party_time', 'count_difference'];
                break;
            case "meal_party":
                query='select * from ?? where current_count < maximum_count order by ??, ??, ??';
                query_list=[type,'party_date','party_time', 'count_difference'];
                break;
            case "night_meal_party":
                query='select * from ?? where current_count < maximum_count order by ??, ??, ??';
                query_list=[type,'party_date','party_time', 'count_difference'];
                break;
            case "study_party":
                query='select * from ?? where current_count < maximum_count order by ??';
                query_list=[type,'count_difference'];
                break;
            case "custom_party":
                query='select * from ?? where current_count < maximum_count order by ??';
                query_list=[type,'count_difference'];
                break;
        }
        connection.query(query,query_list, async(error, rows, field)=>{
            var rows_list=[];
            if (error) {
                // Query error.->mysql error: not null but null or not database or no table etc
                debug("partyList failed due to query error.");
                debug(error.message);
                res.status(400).send(error.message);
            }
            else {
                debug("Rows : "+JSON.stringify(rows));//rows: 배열->해당 party들의 배열
                debug("partyList success.");
                for(let i=0;i<rows.length;i++){
                    rows_list.push({
                        common: {
                        party_id : rows[i].party_id,
                        party_name : rows[i].party_name,
                        party_head : rows[i].party_head,
                        place : {
                        has_place : rows[i].has_place,
                        place1 : rows[i].place1,
                        place2 : rows[i].place2,
                        place3 : rows[i].place3
                    },
                        current_count : rows[i].current_count,
                        maximum_count : rows[i].maximum_count,
                        detailed_description : rows[i].detailed_description,
                        count_difference : rows[i].count_difference
                    },
                        extra : {
                        detailed_start_place : rows[i].detailed_start_place,
                        destination: rows[i].destination,
                        party_date: rows[i].party_date,
                        party_time: rows[i].party_time
                        }
                    }
                    )
                }
                res.status(200).send(rows_list);
            }
        });
    }
    else{
        res.status(400).send("Bad request body; you must include type.");
    }
}

//create new party (insert to partylist)
const createTaxiParty=function (req, res){
    const party_head=req.body.party_head;//userid of the head of the party
    const party_id=global_id;
    const party_name=req.body.party_name;
    const has_place=req.body.has_place;
    const place1=req.body.place1;
    const place2=req.body.place2;
    const place3=req.body.place3;
    const detailed_start_place=req.body.detailed_start_place;
    const destination=req.body.destination;
    const party_date=req.body.party_date;
    const party_time=req.body.party_time;
    const current_count=req.body.current_count;//1
    const maximum_count=req.body.maximum_count;
    const detailed_description=req.body.detailed_description;
    const count_difference=maximum_count-current_count;
    
    debug(`POST /create-party/\ttaxi-party`);
    
    if(party_head!==undefined){
        //check whether the party_head is valid (userid exists)
        connection.query('select userid, username from users where userid=?', [party_head], async(error, rows, field)=>{
            if(error){
                // Query error.
                debug("createTaxiParty_checking userid failed due to query error 0");
                debug(error.message);
                res.status(400).send(error.message);
            }
            else if(rows.length==0){
                debug(`PARTY HEAD is not valid. USERID ${party_head} doesn't exist.`);
                res.status(400).send("PARTY HEAD is not valid. The USERID doesn't exist.");
            }
            else{//party_head is valid (userid exists)
                if (party_id !== undefined &&party_head!==undefined&& party_name !== undefined && has_place!==undefined && place1!==undefined && place2!==undefined &&
                    place3!==undefined && detailed_start_place!==undefined && destination!==undefined && party_date!==undefined &&
                    party_time!==undefined && current_count!==undefined && maximum_count!==undefined && detailed_description!==undefined && count_difference!==undefined
                    ) {
                        const username=rows[0].username;
                        connection.query('select party_id from taxi_party where party_id=?', [party_id], async (error, rows, field) =>{
                    if (error) {
                        // Query error.
                        debug("createTaxiParty failed due to query error 1");
                        debug(error.message);
                        res.status(400).send(error.message);
                    }
                    else if (rows.length > 0) {
                        debug(`PARTY ID ${party_id} already exists.`);
                        res.status(400).send("The PARTY ID already exists.");
                    }
                    else {
                        debug("OK, you can use this PARTY ID..");
                        if (maximum_count<current_count){
                            debug(`MAXIMUM_COUNT : ${maximum_count} < CURRENT_cOUNT : ${current_count} doesn't make sense.`);
                            res.status(400).send(`MAXIMUM_COUNT : ${maximum_count} < CURRENT_cOUNT : ${current_count} doesn't make sense.`);
                    
                        }
                        else{
                                // 1. insert new row in type-party
                            connection.query('insert into taxi_party(party_id, party_head, party_name, has_place, place1, place2, place3, detailed_start_place, destination, party_date, party_time, current_count, maximum_count, count_difference, detailed_description)\
                            values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)',
                            [party_id,party_head, party_name,has_place,place1,place2,place3,detailed_start_place,destination,party_date,party_time,current_count,maximum_count,count_difference, detailed_description],
                            async(error, rows, field)=> {
                                        if (error) {
                                            // Query error again..
                                            debug("createTaxiParty failed due to query error 2");
                                            debug(error.message);
                                            res.status(400).send(error.message);
                                        }
                                        else {
                                            
                                            debug(`createTaxiParty PARTY ID : ${party_id}, successfully registered.`);
                                            global_id+=1;
                                                    // 2. insert new row(party_head) in party_user
                                            connection.query('insert into party_user(party_id, userid, username) values (?,?,?)', [party_id, party_head, username], async(error, rows, field)=> {
                                                if (error) {
                                                    // Query error again..
                                                    debug("createPartyUser_addPartyHead failed due to query error 2");
                                                    debug(error.message);
                                                    res.status(400).send(error.message);
                                                }
                                                else {
                                                    debug(`createPartyUser_addPartyHead PARTY ID : ${party_id}, PARTY_HEAD : ${party_head}, USERNAME : ${username} successfully registered.`);
                                                    res.status(200).send({party_id: party_id});//TODO : response 추후 수정하기!!!  //"createTaxiParty and createPartyUser_addPartyHead succeeded."
                                                }
                                            })
                                        }
                                    })
                        }
                        
                    }
                
                });
                }
                else {
                    debug(`party_head : ${party_head}`)
                    debug(`party_id : ${party_id}`)
                    debug(`party_name : ${party_name}`)
                    debug(`has_place : ${has_place}`)
                    debug(`place1 : ${place1}`)
                    debug(`place2 : ${place2}`)
                    debug(`place3 : ${place3}`)
                    debug(`detailed_start_place : ${detailed_start_place}`)
                    debug(`destination : ${destination}`)
                    debug(`party_date : ${party_date}`)
                    debug(`party_time : ${party_time}`)
                    debug(`current_count : ${current_count}`)
                    debug(`maximum_count : ${maximum_count}`)
                    debug(`count_difference : ${count_difference}`)
                    debug(`detailed_description : ${detailed_description}`)

                    res.status(400).send("Bad request body; you must include party_id, party_head, party_name etc.");
                }
            }
        })
    }
    else {
        res.status(400).send("Bad request body; you must include party_head.");
    }
}

//join-party
const joinParty=function (req, res){
    const input_type=req.params.type;
    const type=input_type.replace('-','_');
    const party_id=req.body.party_id;
    const userid=req.body.userid;
    const username=req.body.username;
    
    debug(`POST /join-party/\tPARTY ID : ${party_id}, USREID : ${userid}`);
    
    if(userid!==undefined){
        //check whether the userid exists
        connection.query('select userid from users where userid=?', [userid], async(error, rows, field)=>{
            if(error){
                // Query error.
                debug("joinParty userid failed due to query error 0");
                debug(error.message);
                res.status(400).send(error.message);
            }
            else if(rows.length==0){
                debug(`USERID ${userid} doesn't exist.`);
                res.status(400).send("USERID doesn't exist.");
            }
            else{//userid exists
                if (party_id !== undefined) {
                        connection.query('select party_id, current_count, maximum_count from ?? where party_id=?', [type, party_id], async (error, rows, field) =>{
                        
                    if (error) {
                        // Query error.
                        debug("joinParty failed due to query error 1");
                        debug(error.message);
                        res.status(400).send(error.message);
                    }
                    else if (rows.length == 0) {
                        debug(`PARTY ID : ${party_id} doesn't exist in ${type} table.`);
                        res.status(400).send(`PARTY ID : ${party_id} doesn't exist in ${type} table.`);
                    }
                    else {
                        debug("OK, the PARTY ID exists..");
                        debug(`ROWS : ${JSON.stringify(rows)}`)

                        var current_count=rows[0].current_count;
                        const maximum_count=rows[0].maximum_count;

                        //check whether (userid, username) exists in users table
                        connection.query('select * from users where userid=? and username=?',[userid, username],async(error, rows, field)=>{
                            if (error) {
                                // Query error again..
                                debug("joinParty failed due to query error 5");
                                debug(error.message);
                                res.status(400).send(error.message);
                            }
                            else if(rows.length==0){
                                debug(`(USER ID : ${userid}, USERNAME : ${username}) doesn't exist in users table.`);
                                res.status(400).send(`(USER ID : ${userid}, USERNAME : ${username}) doesn't exist in users table.`);
                            }
                            else{// (userid, username) exists in users table
                                    //check whether (party_id, userid) exist
                            connection.query('select * from party_user where party_id=? and userid=? and username=?',[party_id,userid, username],async(error, rows, field)=>{
                                if(error){
                                    // Query error again..
                                    debug("joinParty failed due to query error 2");
                                    debug(error.message);
                                    res.status(400).send(error.message);
                                }

                                else if(rows.length>0){//(party_id, userid, username) already exists
                                    debug(`(PARTY ID : ${party_id}, USERID : ${userid}, UESERNAME : ${username}) already exist in party_user table.`);
                                    res.status(400).send("The PARTY ID and USREID and USERNAME already exist in party_user table.");
                                }
                                else if(current_count==maximum_count){
                                    //if current_count==maximum_cout, (party_id, userid) cannot be inserted
                                    debug(`cannot insert] CURRENT_COUNT : ${current_count} == MAXIMUM_COUNT : ${maximum_count}`);
                                    res.status(400).send(`cannot insert] CURRENT_COUNT : ${current_count} == MAXIMUM_COUNT : ${maximum_count}`);
                                }
                                else{//(party_id, userid, username) doesn't exist-> need to insert
                                    //insert new row(party_id, userid, username) in party_user
                                    debug(`ROWS : ${rows}`)
                                    connection.query('insert into party_user(party_id, userid, username) values (?,?,?)', [party_id,userid, username], async(error, rows, field)=> {
                                        if (error) {
                                            // Query error again..
                                            debug("joinParty failed due to query error 3");
                                            debug(error.message);
                                            res.status(400).send(error.message);
                                        }
                                        else {//insert
                                            debug(`joinParty PARTY ID : ${party_id}, USERID: ${userid}, USERNAME : ${username} successfully registered.`);
                                            debug(`Rows : ${JSON.stringify(rows)}`)//not that important data included
                                            //update current_count in type table
                                            connection.query('update ?? set current_count=?, count_difference=? where party_id=?',[type, current_count+1, maximum_count-current_count-1,party_id])
                                            res.status(200).send({current_count: current_count+1});//"joinParty succeeded."
                                        }
                                    })
                                }
                            })
                            }
                        })

                        
                        
                    }
                
                });
                }
                else {
                    res.status(400).send("Bad request body; you must include party_id.");
                }
            }
        })
    }
    else {
        res.status(400).send("Bad request body; you must include party_id or userid.");
    }
}


//leave-party
const leaveParty=function (req, res){
    const input_type=req.params.type;
    const type=input_type.replace('-','_');
    const party_id=req.body.party_id;
    const userid=req.body.userid;
    const username=req.body.username;

    debug(`POST /leave-party/\tPARTY ID : ${party_id}, USREID : ${userid}`);
    
    if(userid!==undefined){
        //check whether the userid exists
        connection.query('select userid from users where userid=?', [userid], async(error, rows, field)=>{
            if(error){
                // Query error.
                debug("leaveParty userid failed due to query error 0");
                debug(error.message);
                res.status(400).send(error.message);
            }
            else if(rows.length==0){
                debug(`USERID ${userid} doesn't exist.`);
                res.status(400).send("USERID doesn't exist.");
            }
            else{//userid exists
                if (party_id !== undefined) {
                        connection.query('select party_id, current_count, maximum_count from ?? where party_id=?', [type, party_id], async (error, rows, field) =>{
                    if (error) {
                        // Query error.
                        debug("leaveParty failed due to query error 1");
                        debug(error.message);
                        res.status(400).send(error.message);
                    }
                    else if (rows.length == 0) {
                        debug(`PARTY ID : ${party_id} doesn't exist in ${type} table.`);
                        res.status(400).send(`PARTY ID : ${party_id} doesn't exist in ${type} table.`);
                    }
                    else {
                        debug("OK, the PARTY ID, CURRENT_COUNT exists..");
                        debug(`ROWS : ${JSON.stringify(rows)}`)

                        var current_count=rows[0].current_count;
                        const maximum_count=rows[0].maximum_count;

                        //check whether (userid, username) exists in users table
                        connection.query('select * from users where userid=? and username=?', [userid, username],async(error, rows, field)=>{
                            if(error){
                                // Query error again..
                                debug("leaveParty failed due to query error 5");
                                debug(error.message);
                                res.status(400).send(error.message);
                            }
                            else if(rows.length==0){
                                debug(`(USER ID : ${userid}, USERNAME : ${username}) doesn't exist in users table.`);
                                res.status(400).send(`(USER ID : ${userid}, USERNAME : ${username}) doesn't exist in users table.`);
                            }
                            else{
                                // (userid, username) exists in users table
                                    //check whether (party_id, userid, username) exist in party_user
                            connection.query('select * from party_user where party_id=? and userid=? and username=?',[party_id,userid, username],async(error, rows, field)=>{
                                if(error){
                                    // Query error again..
                                    debug("leaveParty failed due to query error 2");
                                    debug(error.message);
                                    res.status(400).send(error.message);
                                }

                                else if(rows.length==0){//(party_id, userid, username) doesn't exists
                                    debug(`(PARTY ID : ${party_id}, USERID : ${userid}, UESERNAME : ${username}) doesn't exist in party_user table.`);
                                    res.status(400).send("The PARTY ID and USREID and USERNAME doesn't exist in party_user table.");
                                }
                                else if(current_count==0){
                                    //if current_count==maximum_cout, (party_id, userid) cannot be inserted
                                    debug(`cannot delete] CURRENT_COUNT : ${current_count}, it's zero.`);
                                    res.status(400).send(`cannot delete] CURRENT_COUNT : ${current_count}, it's zero.`);
                                }
                                else{//(party_id, userid) exists-> need to delete
                                    //delete row(party_id, userid, username) in party_user
                                    debug(`ROWS : ${rows}`)
                                    connection.query('delete from party_user where party_id=? and userid=? and username=?', [party_id,userid, username], async(error, rows, field)=> {
                                        if (error) {
                                            // Query error again..
                                            debug("leaveParty failed due to query error 3");
                                            debug(error.message);
                                            res.status(400).send(error.message);
                                        }
                                        else {//delete
                                            debug(`leaveParty PARTY ID : ${party_id}, USERID: ${userid}, USERNAME : ${username} successfully deleted.`);
                                            debug(`Rows : ${JSON.stringify(rows)}`)//not that important data included
                                            //update current_count in type table
                                            connection.query('update ?? set current_count=?, count_difference=? where party_id=?',[type, current_count-1, maximum_count-current_count+1,party_id])
                                            res.status(200).send({current_count: current_count-1});//"joinParty succeeded."
                                        }
                                    })
                                }
                            })
                            }
                        })
                    }
                });
                }
                else {
                    res.status(400).send("Bad request body; you must include party_id.");
                }
            }
        })
    }
    else {
        res.status(400).send("Bad request body; you must include party_id or userid.");
    }
}

//edit taxi-party
const editTaxiParty=function (req, res){
    
    const party_head=req.body.common.party_head;//userid of the head of the party
    const party_id=req.body.common.party_id;///TODO
    
    const party_name=req.body.common.party_name;
    const has_place=req.body.common.place.has_place;
    const place1=req.body.common.place.place1;
    const place2=req.body.common.place.place2;
    const place3=req.body.common.place.place3;
    const detailed_start_place=req.body.extra.detailed_start_place;
    const destination=req.body.extra.destination;
    const party_date=req.body.extra.party_date;
    const party_time=req.body.extra.party_time;
    
    const maximum_count=req.body.common.maximum_count;
    const detailed_description=req.body.common.detailed_description;
    
    
    debug(`POST /edit-party/\ttaxi-party`);
    
    //TODO : party_head은 변경 불가하게 한다면 또 얘기가 달라질 수 있음!
    if(party_head!==undefined){
        //check whether the party_head is valid (userid exists)
        connection.query('select userid from users where userid=?', [party_head], async(error, rows, field)=>{
            if(error){
                // Query error.
                debug("editTaxiParty_checking userid failed due to query error 0");
                debug(error.message);
                res.status(400).send(error.message);
            }
            else if(rows.length==0){
                debug(`PARTY HEAD is not valid. USERID ${party_head} doesn't exist.`);
                res.status(400).send("PARTY HEAD is not valid. The USERID doesn't exist.");
            }
            else{//party_head is valid (userid exists)
                if (party_head!==undefined&&party_id!==undefined&& party_name !== undefined && has_place!==undefined && place1!==undefined && place2!==undefined &&
                    place3!==undefined && detailed_start_place!==undefined && destination!==undefined && party_date!==undefined &&
                    party_time!==undefined && maximum_count!==undefined && detailed_description!==undefined
                    ) {
                        connection.query('select party_id, current_count from taxi_party where party_id=?', [party_id], async (error, rows, field) =>{
                            if (error) {
                                // Query error.
                                debug("editTaxiParty failed due to query error 1");
                                debug(error.message);
                                res.status(400).send(error.message);
                            }
                            else if (rows.length == 0) {
                                debug(`PARTY ID ${party_id} doesn't exist.`);
                                res.status(400).send("The PARTY ID doesn't exist.");
                            }
                            else {
                                debug("OK, PARTY ID, CURRENT_COUNT exists..");

                                const current_count=rows[0].current_count;
                                const count_difference=maximum_count-current_count;

                                if (maximum_count<current_count){
                                    debug(`MAXIMUM_COUNT : ${maximum_count} < CURRENT_cOUNT : ${current_count} doesn't make sense.`);
                                    res.status(400).send(`MAXIMUM_COUNT : ${maximum_count} < CURRENT_cOUNT : ${current_count} doesn't make sense.`);
                                }
                                else{
                                        // 1. edit new row in type-party //TODO PARTY HEAD 수정 가능하게 일단 짬.
                                    connection.query('update taxi_party set party_head=?, party_name=?, has_place=?, place1=?, place2=?, place3=?, detailed_start_place=?, destination=?, party_date=?, party_time=?, maximum_count=?, count_difference=?, detailed_description=? where party_id=?',
                                    [party_head, party_name,has_place,place1,place2,place3,detailed_start_place,destination,party_date,party_time,maximum_count,count_difference, detailed_description, party_id],
                                    async(error, rows, field)=> {
                                                if (error) {
                                                    // Query error again..
                                                    debug("editTaxiParty failed due to query error 2");
                                                    debug(error.message);
                                                    res.status(400).send(error.message);
                                                }
                                                else {
                                                    debug(`editTaxiParty PARTY ID : ${party_id}, successfully edited.`);
                                                    res.status(200).send({party_id: party_id});//TODO : response 추후 수정하기!!!  //"editTaxiParty, successfully edited."
                                                }
                                            })
                                }
                                
                            }
                        
                        });
                }
                else {
                    debug(`party_head : ${party_head}`)
                    debug(`party_id : ${party_id}`)
                    debug(`party_name : ${party_name}`)
                    debug(`has_place : ${has_place}`)
                    debug(`place1 : ${place1}`)
                    debug(`place2 : ${place2}`)
                    debug(`place3 : ${place3}`)
                    debug(`detailed_start_place : ${detailed_start_place}`)
                    debug(`destination : ${destination}`)
                    debug(`party_date : ${party_date}`)
                    debug(`party_time : ${party_time}`)
                    debug(`current_count : ${current_count}`)
                    debug(`maximum_count : ${maximum_count}`)
                    debug(`count_difference : ${count_difference}`)
                    debug(`detailed_description : ${detailed_description}`)

                    res.status(400).send("Bad request body; you must include party_id, party_head, party_name etc.");
                }
            }
        })
    }
    else {
        res.status(400).send("Bad request body; you must include party_head.");
    }
}

//get Party
const getParty=function (req, res){
    const input_type=req.query.type;
    const type=input_type.replace('-','_');
    const party_id=req.query.party_id;

    debug(`GET /party\tTYPE : ${type}, PARTY_ID : ${party_id}`);

    if (type != undefined&&party_id!==undefined ) {

        connection.query('select * from ?? where party_id=?',[type, party_id], async(error, rows, field)=>{
            if (error) {
                // Query error.->mysql error: not null but null or not database or no table etc
                debug("party failed due to query error.");
                debug(error.message);
                res.status(400).send(error.message);
            }
            else if(rows.length==0){//the party_id doesn't exist.
                debug(`PARTY ID : ${party_id} doesn't exist.`)
                res.status(400).send(`PARTY ID : ${party_id} doesn't exist.`)//400: bad request (client's request is wrong.)
            }
            else {
                debug("Rows : "+JSON.stringify(rows));
                debug("party success.");
                const rows_json={
                        common: {
                        party_id : rows[0].party_id,
                        party_name : rows[0].party_name,
                        party_head : rows[0].party_head,
                        place : {
                        has_place : rows[0].has_place,
                        place1 : rows[0].place1,
                        place2 : rows[0].place2,
                        place3 : rows[0].place3
                    },
                        current_count : rows[0].current_count,
                        maximum_count : rows[0].maximum_count,
                        detailed_description : rows[0].detailed_description,
                        count_difference : rows[0].count_difference
                    },
                        extra : {
                        detailed_start_place : rows[0].detailed_start_place,
                        destination: rows[0].destination,
                        party_date: rows[0].party_date,
                        party_time: rows[0].party_time
                        }
                    }
                res.status(200).send(rows_json);
            }
        });
    }
    else{
        res.status(400).send("Bad request body; you must include type or party_id.");
    }
}

//delete-party
const deleteParty=function (req, res){
    const input_type=req.params.type;
    const type=input_type.replace('-','_');
    const party_id=req.body.party_id;

    debug(`POST /delete-party/\tTYPE : ${type}, PARTY ID : ${party_id}`);
    
    if(type!==undefined && party_id!==undefined){
        //check whether the type exists
        const type_arr=['taxi_party', 'meal_party', 'night_meal_party','study_party','custom_party']
        if(type_arr.includes(type)){//type exists
            debug(`TYPE : ${type} exists.`)
            //check whether party_id exists
            connection.query('select * from ?? where party_id=?', [type, party_id], async(error, rows, field)=>{
                if(error){
                    // Query error.
                    debug("deleteParty failed due to query error 0");
                    debug(error.message);
                    res.status(400).send(error.message);
                }
                else if(rows.length==0){
                    debug(`PARTY ID ${party_id} doesn't exist.`);
                    res.status(400).send("PARTY ID doesn't exist.");
                }
                else{//PARTY ID exists
                    //delete that party in type table / delete users in party_user table
                    connection.query('delete from ?? where party_id=?', [type, party_id], async (error, rows, field) =>{
                    if (error) {
                        // Query error.
                        debug("deleteParty failed due to query error 1");
                        debug(error.message);
                        res.status(400).send(error.message);
                    }
                    else {
                        debug(`deleteParty PARTY ID : ${party_id} in TYPE : ${type} succeed.`);
                        debug(`ROWS : ${JSON.stringify(rows)}`)

                        //delete users in party_user included in that party_id
                        connection.query('delete from party_user where party_id=?', [party_id], async(error, rows, field)=>{
                            if(error){
                                // Query error again..
                                debug("deleteParty failed due to query error 2");
                                debug(error.message);
                                res.status(400).send(error.message);
                            }
                            else{
                                debug(`deleteParty in party_user succeed.`);
                                res.status(200).send(`deleteParty in table TYPE : ${type}, party_user succeed.`);
                            }
                        })
                    }
                });
                    
                }
            })
        }
        else{
            debug(`TYPE : ${type} doesn't exist.`)
            res.status(400).send("TYPE doesn't exist.");
        }
        
    }
    else {
        res.status(400).send("Bad request body; you must include type or party_id.");
    }
}


export {getPartyList, createTaxiParty, joinParty, leaveParty, editTaxiParty, getParty, deleteParty};