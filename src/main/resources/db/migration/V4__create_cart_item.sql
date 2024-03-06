create table "cart_item"
(
    id serial,
    cart_id integer not null references "cart" (id),
    product_id integer not null references "product" (id),
    price decimal(12,2) not null,
    quantity decimal(12,2) not null,
    total decimal(12,2) not null,
    version integer not null,
    created_at timestamp,
    modified_at timestamp,
    primary key (id)
);
