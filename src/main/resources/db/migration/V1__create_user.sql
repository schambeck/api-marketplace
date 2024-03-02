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

INSERT INTO "user" (username, email, full_name, version, created_at, modified_at) VALUES ('mschambeck@gmail.com', 'mschambeck@gmail.com', 'Marcelo Schambeck', 0, current_timestamp, current_timestamp);
INSERT INTO "user" (username, email, full_name, version, created_at, modified_at) VALUES ('lupin@gmail.com', 'lupin@gmail.com', 'Assane Lupin', 0, current_timestamp, current_timestamp);
