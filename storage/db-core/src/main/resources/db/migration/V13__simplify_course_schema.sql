alter table course_items
    add column distance_to_next integer,
    add column duration_to_next integer;

alter table course_items
    add constraint chk_course_items_distance_to_next
        check (distance_to_next is null or distance_to_next >= 0),
    add constraint chk_course_items_duration_to_next
        check (duration_to_next is null or duration_to_next >= 0);

update course_items ci
set distance_to_next = (
    select s.distance_meters
    from course_route_segments s
    where s.course_id = ci.course_id
      and s.from_course_item_id = ci.id
),
duration_to_next = (
    select s.duration_seconds
    from course_route_segments s
    where s.course_id = ci.course_id
      and s.from_course_item_id = ci.id
);

drop table if exists course_route_segments;

alter table courses
    drop column if exists description,
    drop column if exists cover_image_url,
    drop column if exists visibility,
    drop column if exists status,
    drop column if exists curation_section,
    drop column if exists curation_order;
