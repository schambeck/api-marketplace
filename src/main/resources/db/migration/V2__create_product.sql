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

insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Bamboo Watch', 'Bamboo Watch', 65.00, 24.00, 1, 'Accessories', 'INSTOCK', '5', 'bamboo-watch.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Black Watch', 'Black Watch', 72.00, 61.00, 1, 'Accessories', 'OUTOFSTOCK', '4', 'black-watch.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Blue Band', 'Blue Band', 79.00, 2.00, 1, 'Fitness', 'LOWSTOCK', '3', 'blue-band.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Blue T-Shirt', 'Blue T-Shirt', 29.00, 25.00, 1, 'Clothing', 'INSTOCK', '5', 'blue-t-shirt.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Bracelet', 'Bracelet', 15.00, 73.00, 1, 'Accessories', 'INSTOCK', '4', 'bracelet.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Brown Purse', 'Brown Purse', 120.00, 0.00, 1, 'Accessories', 'OUTOFSTOCK', '4', 'brown-purse.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Chakra Bracelet', 'Chakra Bracelet', 32.00, 5.00, 1, 'Accessories', 'LOWSTOCK', '3', 'chakra-bracelet.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Galaxy Earrings', 'Galaxy Earrings', 34.00, 23.00, 1, 'Accessories', 'INSTOCK', '5', 'galaxy-earrings.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Game Controller', 'Game Controller', 99.00, 2.00, 1, 'Electronics', 'LOWSTOCK', '4', 'game-controller.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Gaming Set', 'Gaming Set', 299.00, 63.00, 1, 'Electronics', 'INSTOCK', '3', 'gaming-set.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Gold Phone Case', 'Gold Phone Case', 24.00, 0.00, 1, 'Accessories', 'OUTOFSTOCK', '4', 'gold-phone-case.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Green Earbuds', 'Green Earbuds', 89.00, 23.00, 1, 'Electronics', 'INSTOCK', '4', 'green-earbuds.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Green T-Shirt', 'Green T-Shirt', 49.00, 74.00, 1, 'Clothing', 'INSTOCK', '5', 'green-t-shirt.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Grey T-Shirt', 'Grey T-Shirt', 48.00, 0.00, 1, 'Clothing', 'OUTOFSTOCK', '3', 'grey-t-shirt.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Headphones', 'Headphones', 175.00, 8.00, 1, 'Electronics', 'LOWSTOCK', '5', 'headphones.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Light Green T-Shirt', 'Light Green T-Shirt', 49.00, 34.00, 1, 'Clothing', 'INSTOCK', '4', 'light-green-t-shirt.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Lime Band', 'Lime Band', 79.00, 12.00, 1, 'Fitness', 'INSTOCK', '3', 'lime-band.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Mini Speakers', 'Mini Speakers', 85.00, 42.00, 1, 'Clothing', 'INSTOCK', '4', 'mini-speakers.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Painted Phone Case', 'Painted Phone Case', 56.00, 41.00, 1, 'Accessories', 'INSTOCK', '5', 'painted-phone-case.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Pink Band', 'Pink Band', 79.00, 63.00, 1, 'Fitness', 'INSTOCK', '4', 'pink-band.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Pink Purse', 'Pink Purse', 110.00, 0.00, 1, 'Accessories', 'OUTOFSTOCK', '4', 'pink-purse.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Purple Band', 'Purple Band', 79.00, 6.00, 1, 'Fitness', 'LOWSTOCK', '3', 'purple-band.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Purple Gemstone Necklace', 'Purple Gemstone Necklace', 45.00, 62.00, 1, 'Accessories', 'INSTOCK', '4', 'purple-gemstone-necklace.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Purple T-Shirt', 'Purple T-Shirt', 49.00, 2.00, 1, 'Clothing', 'LOWSTOCK', '5', 'purple-t-shirt.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Shoes', 'Shoes', 64.00, 0.00, 1, 'Clothing', 'INSTOCK', '4', 'shoes.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Sneakers', 'Sneakers', 78.00, 52.00, 1, 'Clothing', 'INSTOCK', '4', 'sneakers.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Teal T-Shirt', 'Teal T-Shirt', 49.00, 3.00, 1, 'Clothing', 'LOWSTOCK', '3', 'teal-t-shirt.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Yellow Earbuds', 'Yellow Earbuds', 89.00, 35.00, 1, 'Electronics', 'INSTOCK', '3', 'yellow-earbuds.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Yoga Mat', 'Yoga Mat', 20.00, 15.00, 1, 'Fitness', 'INSTOCK', '5', 'yoga-mat.jpg', 0, current_timestamp, current_timestamp);
insert into product (name, description, price, quantity, user_id, category, inventory_status, rating, image, version, created_at, modified_at) values ('Yoga Set', 'Yoga Set', 20.00, 25.00, 1, 'Fitness', 'INSTOCK', '8', 'yoga-set.jpg', 0, current_timestamp, current_timestamp);
