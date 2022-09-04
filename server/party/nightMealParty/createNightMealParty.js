import debug from '../../debug.js';
import { connection } from '../../config/connection.js';

//create new party (insert to partylist)
const createNightMealParty=function (req, res){
    debug(`req : ${JSON.stringify(req.body)}`)
    const party_head=req.body.common.party_head;//userid of the head of the party
    const party_id=global_id;
    const party_name=req.body.common.party_name;
    const has_place=req.body.common.place.has_place;
    const place1=req.body.common.place.place1;
    const place2=req.body.common.place.place2;
    const place3=req.body.common.place.place3;
    const meal_type=req.body.extra.meal_type;
    const outside=req.body.extra.outside;
    // const detailed_start_place=req.body.extra.detailed_start_place;
    // const destination=req.body.extra.destination;
    const party_date=req.body.extra.party_date;
    const party_time=req.body.extra.party_time;
    const current_count=req.body.common.current_count;//1
    const maximum_count=req.body.common.maximum_count;
    const detailed_description=req.body.common.detailed_description;
    const count_difference=maximum_count-current_count;
    
    debug(`POST /create-party/\tnightMealParty`);
    
    if(party_head!==undefined){
        //check whether the party_head is valid (userid exists)
        connection.query('select userid, username from users where userid=?', [party_head], async(error, rows, field)=>{
            if(error){
                // Query error.
                debug("createNightMealParty_checking userid failed due to query error 0");
                debug(error.message);
                res.status(400).send(error.message);
            }
            else if(rows.length==0){
                debug(`PARTY HEAD is not valid. USERID ${party_head} doesn't exist.`);
                res.status(400).send("PARTY HEAD is not valid. The USERID doesn't exist.");
            }
            else{//party_head is valid (userid exists)
                if (party_id !== undefined &&party_head!==undefined&& party_name !== undefined && has_place!==undefined && place1!==undefined && place2!==undefined &&
                    place3!==undefined && party_date!==undefined &&
                    party_time!==undefined && current_count!==undefined && maximum_count!==undefined && detailed_description!==undefined && count_difference!==undefined
                    ) {
                        const username=rows[0].username;
                        connection.query('select party_id from night_meal_party where party_id=?', [party_id], async (error, rows, field) =>{
                    if (error) {
                        // Query error.
                        debug("createNightMealParty failed due to query error 1");
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
                            connection.query('insert into night_meal_party(party_id, party_head, party_name, has_place, place1, place2, place3, meal_type, outside, party_date, party_time, current_count, maximum_count, count_difference, detailed_description)\
                            values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)',
                            [party_id,party_head, party_name,has_place,place1,place2,place3,meal_type,outside,party_date,party_time,current_count,maximum_count,count_difference, detailed_description],
                            async(error, rows, field)=> {
                                        if (error) {
                                            // Query error again..
                                            debug("createNightMealParty failed due to query error 2");
                                            debug(error.message);
                                            res.status(400).send(error.message);
                                        }
                                        else {
                                            
                                            debug(`createNightMealParty PARTY ID : ${party_id}, successfully registered.`);
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
                    debug(`meal_type : ${meal_type}`)
                    debug(`outside : ${outside}`)
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

export {createNightMealParty};