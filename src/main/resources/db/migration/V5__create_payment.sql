create table "payment"
(
   id serial,
   cart_id integer not null references "cart" (id),
   payment_id bigint not null,
   status varchar(32) not null,
   external_reference varchar(255) not null,
   merchant_order_id bigint not null,
   detail jsonb not null,
   version integer not null,
   created_at timestamp,
   modified_at timestamp,
   primary key (id)
);
