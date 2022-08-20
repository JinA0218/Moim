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
                        date: rows[i].party_date,
                        time: rows[i].party_time
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
    global_id+=1;
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
        connection.query('select userid from users where userid=?', [party_head], async(error, rows, field)=>{
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

                                        // 2. insert new row(party_head) in party_user
                                connection.query('insert into party_user(party_id, userid) values (?,?)', [party_id, party_head], async(error, rows, field)=> {
                                    if (error) {
                                        // Query error again..
                                        debug("createPartyUser_addPartyHead failed due to query error 2");
                                        debug(error.message);
                                        res.status(400).send(error.message);
                                    }
                                    else {
                                        debug(`createPartyUser_addPartyHead PARTY ID : ${party_id}, PARTY_HEAD : ${party_head} successfully registered.`);
                                        res.status(200).send("createTaxiParty and createPartyUser_addPartyHead succeeded.");
                                    }
                                })
                            }
                        })
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

                        //check whether (party_id, userid) exist
                        connection.query('select * from party_user where party_id=? and userid=?',[party_id,userid],async(error, rows, field)=>{
                            if(error){
                                 // Query error again..
                                 debug("joinParty failed due to query error 2");
                                 debug(error.message);
                                 res.status(400).send(error.message);
                            }

                            else if(rows.length>0){//(party_id, userid) already exists
                                debug(`(PARTY ID : ${party_id}, USERID : ${userid}) already exist in party_user table.`);
                                res.status(400).send("The PARTY ID and USREID already exist in party_user table.");
                            }
                            else if(current_count==maximum_count){
                                //if current_count==maximum_cout, (party_id, userid) cannot be inserted
                                debug(`cannot insert] CURRENT_COUNT : ${current_count} == MAXIMUM_COUNT : ${maximum_count}`);
                                res.status(400).send(`cannot insert] CURRENT_COUNT : ${current_count} == MAXIMUM_COUNT : ${maximum_count}`);
                            }
                            else{//(party_id, userid) doesn't exist-> need to insert
                                //insert new row(party_id, userid) in party_user
                                connection.query('insert into party_user(party_id, userid) values (?,?)', [party_id,userid], async(error, rows, field)=> {
                                    if (error) {
                                        // Query error again..
                                        debug("joinParty failed due to query error 3");
                                        debug(error.message);
                                        res.status(400).send(error.message);
                                    }
                                    else {//insert
                                        debug(`joinParty PARTY ID : ${party_id}, USERID: ${userid} successfully registered.`);
                                        debug(`Rows : ${JSON.stringify(rows)}`)//not that important data included
                                        //update current_count in type table
                                        connection.query('update ?? set current_count=?, count_difference=? where party_id=?',[type, current_count+1, maximum_count-current_count-1,party_id])
                                        res.status(200).send({current_count: current_count+1});//"joinParty succeeded."
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

                        //check whether (party_id, userid) already exists
                        connection.query('select * from party_user where party_id=? and userid=?',[party_id,userid],async(error, rows, field)=>{
                            if(error){
                                 // Query error again..
                                 debug("leaveParty failed due to query error 2");
                                 debug(error.message);
                                 res.status(400).send(error.message);
                            }
                            else if(rows.length==0){//(party_id, userid) doesn't exists
                                debug(`(PARTY ID : ${party_id}, USERID : ${userid}) doesn't exist in party_user table.`);
                                res.status(400).send("The PARTY ID and USREID doesn't exist in party_user table.");
                            }
                            else if(current_count==0){
                                //if current_count==0, (party_id, userid) cannot be deleted
                                debug(`cannot delete] CURRENT_COUNT : ${current_count}, less than 0 people is impossible.`);
                                res.status(400).send(`cannot delete] CURRENT_COUNT : ${current_count}, less than 0 people is impossible.`);
                            }
                            else{//(party_id, userid) exists-> need to delete
                                //delete row(party_id, userid) in party_user
                                connection.query('delete from party_user where party_id=? and userid=?', [party_id,userid], async(error, rows, field)=> {
                                    if (error) {
                                        // Query error again..
                                        debug("leaveParty failed due to query error 3");
                                        debug(error.message);
                                        res.status(400).send(error.message);
                                    }
                                    else {//delete
                                        debug(`leaveParty PARTY ID : ${party_id}, USERID: ${userid} successfully deleted.`);
                                        debug(`Rows : ${JSON.stringify(rows)}`)//not that important data included
                                        //update current_count in type table
                                        connection.query('update ?? set current_count=?, count_difference=? where party_id=?',[type, current_count-1, maximum_count-current_count+1,party_id])
                                        res.status(200).send({current_count: current_count-1});//"leaveParty succeeded."
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



export {getPartyList, createTaxiParty, joinParty, leaveParty};