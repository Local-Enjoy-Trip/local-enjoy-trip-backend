alter table courses
    add column start_location geometry(Point, 4326);

create index idx_courses_start_location
    on courses using gist (start_location)
    where start_location is not null
      and deleted_at is null;
