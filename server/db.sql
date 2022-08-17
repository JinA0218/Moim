-- Active: 1660637152703@@127.0.0.1@3306@week5
drop database week5;
create database if not exists week5;
use week5;

create table if not exists users(
    userid varchar(20) not null primary key,
    username varchar(20) not null,
    pw varchar(88) not null,
    age varchar(10),
    place varchar(200)
);