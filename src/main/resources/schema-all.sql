DROP TABLE people IF EXISTS;
DROP TABLE person IF EXISTS;

CREATE TABLE people  (
    person_id BIGINT IDENTITY NOT NULL PRIMARY KEY,
    first_name VARCHAR(20),
    last_name VARCHAR(20)
);

create table person (
    id BIGINT IDENTITY NOT NULL PRIMARY KEY,
    email varchar(100) not null,
    age int not null,
    first_name varchar(100) not null
);