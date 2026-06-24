alter table attraction_tags rename to tags;

drop table if exists attraction_tag_mappings;

create table course_tags (
    course_id varchar(128) not null,
    tag_id bigint not null,
    created_at timestamp without time zone default current_timestamp,
    constraint pk_course_tags primary key (course_id, tag_id),
    constraint fk_course_tags_course foreign key (course_id) references courses (id) on delete cascade,
    constraint fk_course_tags_tag foreign key (tag_id) references tags (id) on delete cascade
);
