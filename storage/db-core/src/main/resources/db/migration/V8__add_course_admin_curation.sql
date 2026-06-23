alter table members
    add column role varchar(20) not null default 'USER';

alter table members
    add constraint chk_members_role check (role in ('USER', 'ADMIN'));

alter table courses
    add column curation_section varchar(50),
    add column curation_order integer;

alter table courses
    add constraint chk_courses_curation_section
        check (curation_section is null or curation_section in ('MD_RECOMMENDED'));

alter table courses
    add constraint chk_courses_curation_order
        check (curation_order is null or curation_order > 0);

alter table attractions
    add column status varchar(20) not null default 'ACTIVE',
    add column updated_at timestamp(6),
    add column deleted_at timestamp(6),
    add column duplicate_of_attraction_id bigint,
    add column duplicate_reason text;

alter table attractions
    add constraint chk_attractions_status check (status in ('ACTIVE', 'HIDDEN')),
    add constraint fk_attractions_duplicate
        foreign key (duplicate_of_attraction_id) references attractions (id);

create index idx_courses_curation_section_order
    on courses (curation_section, curation_order, updated_at desc, id)
    where deleted_at is null;

create index idx_course_saves_course on course_saves (course_id);

create index idx_attractions_public_status
    on attractions (status, deleted_at, duplicate_of_attraction_id, id);
