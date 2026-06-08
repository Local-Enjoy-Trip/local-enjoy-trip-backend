-- Table for additional attraction photos
create table attraction_photos (
    id serial primary key,
    content_id bigint not null,
    origin_img_url varchar(1024) not null,
    small_img_url varchar(1024),
    img_name varchar(255),
    created_at timestamp(6) not null default current_timestamp,
    foreign key (content_id) references attractions(id) on delete cascade
);

-- Table for sunrise and sunset information
create table sunrise_sunset (
    id serial primary key,
    loc_name varchar(100) not null,
    loc_date varchar(8) not null,
    sunrise varchar(10),
    sunset varchar(10),
    moonrise varchar(10),
    moonset varchar(10),
    created_at timestamp(6) not null default current_timestamp,
    unique (loc_name, loc_date)
);

create index idx_attraction_photos_content_id on attraction_photos (content_id);
create index idx_sunrise_sunset_loc on sunrise_sunset (loc_name, loc_date);
