create table if not exists best_dish_sale(
    id serial primary key,
    name varchar(100),
    quantity bigint, 
    amount float
);