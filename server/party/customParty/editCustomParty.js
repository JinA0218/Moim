import debug from '../../debug.js';
import { connection } from '../../config/connection.js';

//edit custom-party
const editCustomParty=function (req, res){
    
    const party_head=req.body.common.party_head;//userid of the head of the party
    const party_id=req.body.common.party_id;///TODO
    
    const party_name=req.body.common.party_name;
    const has_place=req.body.common.place.has_place;
    const place1=req.body.common.place.place1;
    const place2=req.body.common.place.place2;
    const place3=req.body.common.place.place3;
    
    const maximum_count=req.body.common.maximum_count;
    const detailed_description=req.body.common.detailed_description;
    
    
    debug(`POST /edit-party/\tcustom-party`);
    
    //TODO : party_head은 변경 불가하게 한다면 또 얘기가 달라질 수 있음!
    if(party_head!==undefined){
        //check whether the party_head is valid (userid exists)
        connection.query('select userid from users where userid=?', [party_head], async(error, rows, field)=>{
            if(error){
                // Query error.
                debug("editCustomParty_checking userid failed due to query error 0");
                debug(error.message);
                res.status(400).send(error.message);
            }
            else if(rows.length==0){
                debug(`PARTY HEAD is not valid. USERID ${party_head} doesn't exist.`);
                res.status(400).send("PARTY HEAD is not valid. The USERID doesn't exist.");
            }
            else{//party_head is valid (userid exists)
                if (party_head!==undefined&&party_id!==undefined&& party_name !== undefined && has_place!==undefined && place1!==undefined && place2!==undefined &&
                    place3!==undefined && maximum_count!==undefined && detailed_description!==undefined
                    ) {
                        connection.query('select party_id, current_count from custom_party where party_id=?', [party_id], async (error, rows, field) =>{
                            if (error) {
                                // Query error.
                                debug("editCustomParty failed due to query error 1");
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
                                    connection.query('update custom_party set party_head=?, party_name=?, has_place=?, place1=?, place2=?, place3=?, maximum_count=?, count_difference=?, detailed_description=? where party_id=?',
                                    [party_head, party_name,has_place,place1,place2,place3,maximum_count,count_difference, detailed_description, party_id],
                                    async(error, rows, field)=> {
                                                if (error) {
                                                    // Query error again..
                                                    debug("editCustomParty failed due to query error 2");
                                                    debug(error.message);
                                                    res.status(400).send(error.message);
                                                }
                                                else {
                                                    debug(`editCustomParty PARTY ID : ${party_id}, successfully edited.`);
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
                    
                    debug(`current_count : SPACE OF CURRENT_COUNT`)
                    debug(`maximum_count : ${maximum_count}`)
                    debug(`count_difference : SPACE OF COUNT_DIFFERENCE`)
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

export {editCustomParty};