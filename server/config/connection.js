import express from 'express';
import mysql from 'mysql2';
import fs from 'fs';
import http from 'http';//socket.io
import {Server} from 'socket.io'

const conf=JSON.parse(fs.readFileSync('../config/database.json'));

const connection = mysql.createConnection({
    host: conf.host,
    user: conf.user,
    password: conf.password,
    database: conf.database
    });

///socket.io code
const app=express();
const server=http.createServer(app);
const io=new Server(server);

// Converts plain JSON object to hierarchical JSON object.
const makeHierarchy = (party) => {
    var object = {common: {}, party_type: party.party_type}

    object.common = {
        party_id: party.party_id,
        party_name: party.party_name,
        party_head: party.party_head,
        place: {
            has_place: party.has_place,
            place1: party.place1,
            place2: party.place2,
            place3: party.place3
        },
        current_count: party.current_count,
        maximum_count: party.maximum_count,
        detailed_description: party.detailed_description,
        count_difference: party.count_difference
    }

    switch (party.party_type) {
        case "taxi_party":
            object.extra = {
                detailed_start_place: party.detailed_start_place,
                destination: party.destination,
                party_date: party.party_date,
                party_time: party.party_time
            }
            break;
        case "meal_party":
            object.extra = {
                meal_type: party.meal_type,
                outside: party.outside,
                party_date: party.party_date,
                party_time: party.party_time
            }
            break;
        case "night_meal_party":
            object.extra = {
                meal_type: party.meal_type,
                outside: party.outside,
                party_date: party.party_date,
                party_time: party.party_time
            }
            break;
        default:
            break;

    }

    return object
}

const convertRowsHierarchical = (rows, partyType) => {
    let rows2 = rows.map((party) => {
        party.party_type = partyType
        return party
    })
    return rows2.map(makeHierarchy)
}

export {connection, app, io, server, convertRowsHierarchical};