import debug from '../../debug.js';
import {connection, convertRowsHierarchical} from '../../config/connection.js';

const myParty=function (req, res){
    const userid=req.params.userid;
    
    debug(`GET /my-party\tUSREID : ${userid}`);

    if (userid != undefined ) {
        connection.query('select * from users where userid=?',[userid],async(error, rows, field)=>{
            if (error) {
                // Query error.->mysql error: not null but null or not database or no table etc
                debug("myParty failed due to query error 0.");
                debug(error.message);
                res.status(400).send(error.message);
            }
            else if(rows.length==0){
                debug(`USERID :${userid} doesn't exist.`)
                res.status(400).send(`USERID :${userid} doesn't exist.`);
            }
            else {//userid exists
                debug("Rows : "+JSON.stringify(rows));
                //find the type
                //userid를 알고 있고, 해당 user가 들어있는 목록을 보내줘야 함.
                //party_user랑 type table(taxi_party, ... etc)을 join 시켜줘야 함.
                //inner join 시켜주고 나면 교집합만 생성되는데 거기서 해당 userid만 가지고 select.
                //다른 type에 대해서도 반복해주고, 이를 큰 리스트에 담기.
                //그 리스트를 보내주기
                const type_arr=['taxi_party', 'meal_party','night_meal_party','study_party', 'custom_party'];
                var party_json={
                    'taxi_party':[],
                    'meal_party':[],
                    'night_meal_party':[],
                    'study_party':[],
                    'custom_party':[]
                };
                for(let i=0; i<5;i++){
                    connection.query(`select * from party_user as u inner join ?? as t on u.party_id=t.party_id and u.userid=?`,[type_arr[i],userid],async(error, rows, field)=>{
                        if(error){
                            debug("myParty failed due to query error 1.");
                            debug(error.message);
                            res.status(400).send(error.message);
                        }
                        else if(rows.length==0){
                            debug(`myParty PARTY TYPE : ${type_arr[i]}, USERID : ${userid} has no data.`);
                            party_json[type_arr[i]]=[];
                        }
                        else{
                            debug(`!!!!!!!!!!!!`);
                            debug(`${type_arr[i]} Rows : ${JSON.stringify(rows)}`);
                            party_json[type_arr[i]]=convertRowsHierarchical(rows,type_arr[i]);
                        }
                        if(i==4){
                            res.status(200).send(party_json);
                        }
                    })
                }
            }
        })
    }
    else{
        res.status(400).send("Bad request body; you must include type.");
    }
}


export {myParty};
