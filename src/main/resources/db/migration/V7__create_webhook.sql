create table "webhook"
(
   id serial,
   action varchar(255) not null,
   api_version varchar(255) not null,
   data jsonb not null,
   date_created varchar(255) not null,
   external_id varchar(255) not null,
   live_mode boolean not null,
   type varchar(255) not null,
   user_id bigint not null,
   version integer not null,
   created_at timestamp,
   modified_at timestamp,
   primary key (id)
);
