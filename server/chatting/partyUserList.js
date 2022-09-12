import debug from '../debug.js';
import {connection} from '../config/connection.js';

//코드 수정 처음부터 해야함! TODO//////여기부터 다시 하기!!!!! 8/21~
const getPartyUserList=function (req, res){
    const party_id=req.params.party_id;
    
    debug(`GET /party-user-list\tPARTY_ID : ${party_id}`);

    if (party_id != undefined ) {
        
        connection.query('select * from party_user where party_id=?',[party_id], async(error, rows, field)=>{
            var rows_list=[];
            if (error) {
                // Query error.->mysql error: not null but null or not database or no table etc
                debug("partyUserList failed due to query error.");
                debug(error.message);
                res.status(400).send(error.message);
            }
            else if(rows.length==0){
                debug(`PARTY ID : ${party_id} doesn't exist.`)
            }
            else {
                debug("Rows : "+JSON.stringify(rows));//rows: 배열->해당 party들의 배열
                debug("partyUSerList success.");
                for(let i=0;i<rows.length;i++){
                    rows_list.push({
                        party_id: rows[i].party_id,
                        userid: rows[i].userid,
                        username:rows[i].username
                    }
                    )
                }
                res.status(200).send(rows_list);
            }
        });
    }
    else{
        res.status(400).send("Bad request body; you must include party_id.");
    }
}

//party-chat-list
const partyChatList=function(req, res){
    
    const a=1;
    const party_id=req.query.party_id;
    const offset=req.query.offset;
    
    //TODO 여기부터 다시 짜기!!!!
    debug(`GET /party-chat-list\tPARTY ID : ${party_id}, OFFSET : ${offset}`);

    if (party_id !== undefined &&offset!==undefined) {
        connection.query('select * from chatRoom where party_id=? order by chat_id desc limit ?,100',[party_id, parseInt(offset)], async(error, rows, field)=>{
            if (error) {
                // Query error.->mysql error: not null but null or not database or no table etc
                debug("partyChatList failed due to query error 0.");
                debug(error.message);
                res.status(400).send(error.message);
            }
            else {//rows.length exists
                debug("Rows : "+JSON.stringify(rows));//rows: 배열->해당 party들의 배열
                debug("partyChatList success.");
                res.status(200).send(rows);
            }
        });
    }
    else{
        res.status(400).send("Bad request body; you must include party_id or offset.");
    }
}

export {getPartyUserList, partyChatList};
