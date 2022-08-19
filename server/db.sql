-- Active: 1660637152703@@127.0.0.1@3306@week5
drop database week5;
create database if not exists week5;
use week5;

create table if not exists users(
    username varchar(20) character set utf8 collate utf8_general_ci not null,
    userid varchar(20) not null primary key,
    pw varchar(88) not null,
    age int(10),
    place1 varchar(20) character set utf8 collate utf8_general_ci not null,
    place2 varchar(20) character set utf8 collate utf8_general_ci,
    place3 varchar(20) character set utf8 collate utf8_general_ci not null,
    salt varchar(88) not null
);

create table if not exists taxi_party(
    party_id int(6) not null primary key,
    party_head varchar(20) not null,
    party_name varchar(30) character set utf8 collate utf8_general_ci not null,
    has_place boolean not null default 1,
    place1 varchar(20) character set utf8 collate utf8_general_ci not null,
    place2 varchar(20) character set utf8 collate utf8_general_ci,
    place3 varchar(20) character set utf8 collate utf8_general_ci not null,
    detailed_start_place varchar(20) character set utf8 collate utf8_general_ci not null,
    destination varchar(20) character set utf8 collate utf8_general_ci not null,
    party_date DATE not null,
    party_time TIME not null,
    current_count int(2) not null,
    maximum_count int(2) not null,
    count_difference int(2) not null,
    detailed_description varchar(500) character set utf8 collate utf8_general_ci not null
);

create table if not exists meal_party(
    party_id int(6) not null primary key,
    party_head varchar(20) not null,
    party_name varchar(30) character set utf8 collate utf8_general_ci not null,
    has_place boolean not null default 1,
    place1 varchar(20) character set utf8 collate utf8_general_ci not null,
    place2 varchar(20) character set utf8 collate utf8_general_ci,
    place3 varchar(20) character set utf8 collate utf8_general_ci not null,
    meal_type varchar(10) character set utf8 collate utf8_general_ci not null,
    outside boolean not null,
    party_date DATE not null,
    party_time TIME not null,
    current_count int(2) not null,
    maximum_count int(2) not null,
    count_difference int(2) not null,
    detailed_description varchar(500) character set utf8 collate utf8_general_ci not null
);

create table if not exists night_meal_party(
    party_id int(6) not null primary key,
    party_head varchar(20) not null,
    party_name varchar(30) character set utf8 collate utf8_general_ci not null,
    has_place boolean not null default 1,
    place1 varchar(20) character set utf8 collate utf8_general_ci not null,
    place2 varchar(20) character set utf8 collate utf8_general_ci,
    place3 varchar(20) character set utf8 collate utf8_general_ci not null,
    meal_type varchar(10) character set utf8 collate utf8_general_ci not null,
    outside boolean not null default 0,
    party_date DATE not null,
    party_time TIME not null,
    current_count int(2) not null,
    maximum_count int(2) not null,
    count_difference int(2) not null,
    detailed_description varchar(500) character set utf8 collate utf8_general_ci not null
);

create table if not exists study_party(
    party_id int(6) not null primary key,
    party_head varchar(20) not null,
    party_name varchar(30) character set utf8 collate utf8_general_ci not null,
    has_place boolean not null default 1,
    place1 varchar(20) character set utf8 collate utf8_general_ci not null,
    place2 varchar(20) character set utf8 collate utf8_general_ci,
    place3 varchar(20) character set utf8 collate utf8_general_ci not null,
    current_count int(2) not null,
    maximum_count int(2) not null,
    count_difference int(2) not null,
    detailed_description varchar(500) character set utf8 collate utf8_general_ci not null
);

create table if not exists custom_party(
    party_id int(6) not null primary key,
    party_head varchar(20) not null,
    party_name varchar(30) character set utf8 collate utf8_general_ci not null,
    has_place boolean not null default 1,
    place1 varchar(20) character set utf8 collate utf8_general_ci not null,
    place2 varchar(20) character set utf8 collate utf8_general_ci,
    place3 varchar(20) character set utf8 collate utf8_general_ci not null,
    current_count int(2) not null,
    maximum_count int(2) not null,
    count_difference int(2) not null,
    detailed_description varchar(500) character set utf8 collate utf8_general_ci not null
);
