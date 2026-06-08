-- Enable PostGIS extension if not already enabled
create extension if not exists postgis;

-- Table for attractions
create table attractions (
    id bigint primary key,
    title varchar(255) not null,
    addr1 varchar(512),
    addr2 varchar(512),
    zipcode varchar(20),
    tel varchar(100),
    first_image varchar(1024),
    first_image2 varchar(1024),
    read_count integer default 0,
    sido_code integer,
    gugun_code integer,
    mlevel varchar(20),
    content_type_id varchar(20),
    overview text,
    -- PostGIS Point (longitude, latitude)
    location geometry(Point, 4326),
    created_at timestamp(6) not null default current_timestamp
);

-- Table for EV chargers
create table ev_chargers (
    id serial primary key,
    stat_id varchar(100) not null,
    stat_nm varchar(255) not null,
    chger_id varchar(50),
    chger_type varchar(50),
    addr varchar(512),
    location_desc varchar(512),
    use_time varchar(255),
    busi_nm varchar(255),
    busi_call varchar(100),
    stat varchar(50),
    -- PostGIS Point (longitude, latitude)
    location geometry(Point, 4326),
    created_at timestamp(6) not null default current_timestamp,
    unique (stat_id, chger_id)
);

-- Table for News Items
create table news_items (
    id varchar(128) primary key,
    title varchar(512) not null,
    link varchar(1024) not null,
    summary text,
    source varchar(255),
    published_at varchar(100),
    created_at timestamp(6) not null default current_timestamp
);

-- Indexes for spatial queries
create index idx_attractions_location on attractions using gist (location);
create index idx_ev_chargers_location on ev_chargers using gist (location);

-- Index for area search
create index idx_attractions_area on attractions (sido_code, gugun_code);
