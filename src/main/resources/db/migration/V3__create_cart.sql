create table "cart"
(
    id serial,
    user_id integer not null references "user" (id),
    total decimal(12,2) not null,
    version integer not null,
    created_at timestamp,
    modified_at timestamp,
    primary key (id)
);

create unique index idx_cart_user_id on cart (user_id);
