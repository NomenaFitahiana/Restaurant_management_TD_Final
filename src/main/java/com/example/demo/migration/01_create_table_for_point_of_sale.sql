create table if not exists point_of_sale(
    id serial primary key ,
    name varchar(100),
    synchronisation_date timestamp,
    id_dish bigint references best_dish_sale(id)
);