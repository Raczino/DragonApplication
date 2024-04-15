create table testTable
(
    id int primary key not null ,
    email text not null
);

ALTER TABLE testTable
    ADD name text not null default null;
