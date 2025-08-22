create table "user"
(
   id serial,
   external_id text not null,
   username varchar(255) not null,
   email varchar(255) not null,
   full_name varchar(255) not null,
   version integer not null,
   created_at timestamp,
   modified_at timestamp,
   public_key text,
   public_key_modified_at timestamp,
   primary key (id)
);

create unique index idx_user_username on "user" (username);

insert into "user" (external_id, username, email, full_name, version, created_at, modified_at) values ('99dcc9b8-f0c7-4ebe-9405-781cdde17282', 'mschambeck@gmail.com', 'mschambeck@gmail.com', 'Marcelo Schambeck', 0, current_timestamp, current_timestamp);
insert into "user" (external_id, username, email, full_name, version, created_at, modified_at) values ('5f7b6f99-14d5-4aea-8d2a-6cce942248d9', 'lupin@gmail.com', 'lupin@gmail.com', 'Assane Lupin', 0, current_timestamp, current_timestamp);
insert into "user" (external_id, username, email, full_name, version, created_at, modified_at) values ('2faac695-67fb-4abb-9845-3df55ed6f248', 'lgazzolation@gmail.com', 'lgazzolation@gmail.com', 'Leonardo Gazzola', 0, current_timestamp, current_timestamp);
