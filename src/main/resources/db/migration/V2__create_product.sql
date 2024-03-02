create table "product"
(
    id serial,
    name varchar(255) not null,
    description varchar(255) not null,
    price decimal(12,2) not null,
    quantity decimal(12,2) not null,
    user_id integer not null references "user" (id),
    category varchar(255) not null,
    inventory_status varchar(255) not null,
    rating varchar(255) not null,
    image varchar(255) not null,
    version integer not null,
    created_at timestamp,
    modified_at timestamp,
    primary key (id)
);
