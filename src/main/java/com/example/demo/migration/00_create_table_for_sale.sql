create table if not exists sale(
    id serial primary key,
    sale_point varchar(100),
    dish varchar(100),
    quantity_sold bigint, 
    total_amount float
);