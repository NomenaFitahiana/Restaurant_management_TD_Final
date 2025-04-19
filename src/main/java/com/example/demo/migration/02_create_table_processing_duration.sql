create enum as "duration_unit"('SECONDS', 'MINUTES', 'HOUR');

create table if not exists processing_time(
    id serial primary key, 
    updatedat timestamp,
    sales_point varchar(100),
    id_dish integer,
    dish_name varchar(100),
    preparation_duration float,
    duration_unit "duration_unit"
);