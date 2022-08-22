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

export {getPartyUserList};
