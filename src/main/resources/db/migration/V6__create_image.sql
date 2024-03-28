create table "image"
(
    id serial,
    product_id integer not null references "product" (id),
    object_id varchar(255) not null,
    file_name varchar(255) not null,
    content_type varchar(255) not null,
    size integer not null,
    version integer not null,
    created_at timestamp,
    modified_at timestamp,
    primary key (id)
);

insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (1, 'bamboo-watch.jpg', '4fbf7be9-5a63-4e40-9d0e-372251cd8644.jpg', 'image/jpeg', 10104, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (2, 'black-watch.jpg', '5226a815-3516-4304-baa1-d05415cb32d6.jpg', 'image/jpeg', 9942, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (3, 'blue-band.jpg', '9672d940-6656-4b4e-97a8-6f34fb081e82.jpg', 'image/jpeg', 3199, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (4, 'blue-t-shirt.jpg', 'd8df41d5-4f50-476a-8a4a-f630d74cd422.jpg', 'image/jpeg', 7577, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (5, 'bracelet.jpg', '9c26ece3-a64d-42d5-8643-d6a0c0939ef7.jpg', 'image/jpeg', 4741, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (6, 'brown-purse.jpg', 'd2f66dd8-5f78-47a6-8a69-a2b28f7d7d28.jpg', 'image/jpeg', 5720, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (7, 'chakra-bracelet.jpg', 'd62c74af-2ad1-4044-ba6f-2aed8a040c70.jpg', 'image/jpeg', 4587, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (8, 'galaxy-earrings.jpg', '543ca717-db49-4f88-a914-f0eeea9f63b4.jpg', 'image/jpeg', 3208, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (9, 'game-controller.jpg', '5146ff08-2ea8-4458-9928-bf0d2acbb03a.jpg', 'image/jpeg', 3982, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (10, 'gaming-set.jpg', 'f2b57a0d-bc28-4a38-8b48-379bef1938ca.jpg', 'image/jpeg', 9530, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (11, 'gold-phone-case.jpg', 'e4ead907-ad73-4f95-98e6-d1917c91ee13.jpg', 'image/jpeg', 7818, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (12, 'green-earbuds.jpg', 'a7d654b4-07c6-4b8b-b9b8-364c6c89b4ad.jpg', 'image/jpeg', 6996, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (13, 'green-t-shirt.jpg', '687e38e4-01c5-46e1-be6f-b619e570cb47.jpg', 'image/jpeg', 7263, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (14, 'grey-t-shirt.jpg', '0f7c0c28-8e88-4478-a19c-145c025962fc.jpg', 'image/jpeg', 7330, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (15, 'headphones.jpg', '1b163e1b-335c-4c52-9768-5ed512f43a3c.jpg', 'image/jpeg', 4497, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (16, 'light-green-t-shirt.jpg', '4cd95c51-2dcc-4434-8b01-6a85df977841.jpg', 'image/jpeg', 7347, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (17, 'lime-band.jpg', 'c5aa51e5-3699-46fd-8e6c-24642239300e.jpg', 'image/jpeg', 3212, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (18, 'mini-speakers.jpg', '9b8fd5f8-eb2d-475e-811f-698716427638.jpg', 'image/jpeg', 3684, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (19, 'painted-phone-case.jpg', '63889cac-9963-4690-a247-7d17f265db03.jpg', 'image/jpeg', 9455, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (20, 'pink-band.jpg', 'da74ea14-a9fe-4ed2-ab27-b4a4bc6321cb.jpg', 'image/jpeg', 3093, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (21, 'pink-purse.jpg', '0784db0d-4c6a-42cb-887e-824b7ac3cbaf.jpg', 'image/jpeg', 4668, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (22, 'purple-band.jpg', 'a004e922-5754-477d-9fb3-c15f57c625a5.jpg', 'image/jpeg', 3336, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (23, 'purple-gemstone-necklace.jpg', 'dfbc7c17-be0f-43dc-96fe-5211e3de437d.jpg', 'image/jpeg', 3084, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (24, 'purple-t-shirt.jpg', 'aac7b528-468b-4ea1-b811-404d4869d4a3.jpg', 'image/jpeg', 7522, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (25, 'shoes.jpg', '28e5f934-3013-4d38-9fc9-e653a7de443b.jpg', 'image/jpeg', 6855, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (26, 'sneakers.jpg', 'e6fe2621-47eb-4cae-9dcb-30e9c679b253.jpg', 'image/jpeg', 7067, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (27, 'teal-t-shirt.jpg', '9ef1eeaf-c355-45fd-a3a1-063b3a0477ab.jpg', 'image/jpeg', 7498, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (28, 'yellow-earbuds.jpg', '24ec52dc-27b0-47b5-baf0-8450a2701b6d.jpg', 'image/jpeg', 7173, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (29, 'yoga-mat.jpg', '7a7120e9-dcf4-4f13-b3cf-7b08a0eafaa7.jpg', 'image/jpeg', 6918, 0, current_timestamp, current_timestamp);
insert into "image" (product_id, file_name, object_id, content_type, size, version, created_at, modified_at) values (30, 'yoga-set.jpg', 'ccd922c3-cf11-44c5-bfc7-cb59d642ff3c.jpg', 'image/jpeg', 5133, 0, current_timestamp, current_timestamp);
