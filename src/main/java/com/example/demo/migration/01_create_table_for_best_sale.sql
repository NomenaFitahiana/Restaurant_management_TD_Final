create table if not exists best_sale(
    id serial primary key ,
    updatedAt timestamp,
    id_sale bigint references sale(id)
);