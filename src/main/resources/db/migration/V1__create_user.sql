create table "user"
(
   id serial,
   username varchar(255) not null,
   email varchar(255) not null,
   full_name varchar(255) not null,
   version integer not null,
   created_at timestamp,
   modified_at timestamp,
   primary key (id)
);

create unique index idx_user_username on "user" (username);

insert into "user" (username, email, full_name, version, created_at, modified_at) values ('mschambeck@gmail.com', 'mschambeck@gmail.com', 'Marcelo Schambeck', 0, current_timestamp, current_timestamp);
insert into "user" (username, email, full_name, version, created_at, modified_at) values ('lupin@gmail.com', 'lupin@gmail.com', 'Assane Lupin', 0, current_timestamp, current_timestamp);
insert into "user" (username, email, full_name, version, created_at, modified_at) values ('google-oauth2|109094994856035521294', 'google-oauth2|109094994856035521294', 'google-oauth2|109094994856035521294', 0, current_timestamp, current_timestamp);
