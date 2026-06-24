alter table courses
    add column date varchar(32);

alter table course_items
    drop column if exists day,
    drop column if exists memo,
    drop column if exists stay_minutes;
