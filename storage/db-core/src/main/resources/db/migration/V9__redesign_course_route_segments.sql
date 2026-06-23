update courses
set status = 'READY'
where status = 'DRAFT';

alter table courses
    alter column status set default 'READY';

alter table courses
    drop constraint if exists chk_courses_status;

alter table courses
    add constraint chk_courses_status
        check (status in ('READY', 'IN_PROGRESS', 'COMPLETED', 'ARCHIVED'));

alter table course_items
    add constraint uk_course_items_course_id_id unique (course_id, id);

create table course_route_segments (
    course_id varchar(128) not null,
    from_course_item_id bigint not null,
    to_course_item_id bigint not null,
    segment_order integer not null,
    travel_mode varchar(20) not null,
    duration_seconds integer not null,
    distance_meters integer not null,
    primary key (course_id, segment_order),
    constraint fk_course_route_segments_course
        foreign key (course_id) references courses (id) on delete cascade,
    constraint fk_course_route_segments_from_item
        foreign key (course_id, from_course_item_id)
            references course_items (course_id, id) on delete cascade,
    constraint fk_course_route_segments_to_item
        foreign key (course_id, to_course_item_id)
            references course_items (course_id, id) on delete cascade,
    constraint chk_course_route_segments_order check (segment_order > 0),
    constraint chk_course_route_segments_duration check (duration_seconds >= 0),
    constraint chk_course_route_segments_distance check (distance_meters >= 0)
);

insert into course_route_segments (
    course_id,
    from_course_item_id,
    to_course_item_id,
    segment_order,
    travel_mode,
    duration_seconds,
    distance_meters
)
select course_id,
       from_course_item_id,
       to_course_item_id,
       segment_order,
       'WALK',
       0,
       0
from (
    select course_id,
           id as from_course_item_id,
           lead(id) over (partition by course_id order by position asc, id asc) as to_course_item_id,
           row_number() over (partition by course_id order by position asc, id asc) as segment_order
    from course_items
) existing_items
where to_course_item_id is not null
on conflict do nothing;
