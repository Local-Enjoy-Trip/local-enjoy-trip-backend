-- Phase B: add MVP note image model with at most one uploaded object reference per note.
-- Existing databases that already applied V1 receive the columns here; fresh databases apply V1 then V2.
-- Legacy note_photos remains for compatibility, but new note mutations no longer depend on it.

alter table notes
    add column image_object_key varchar(512),
    add column image_url varchar(1024),
    add column image_content_type varchar(100);

alter table notes
    add constraint chk_notes_single_image_reference_pair
        check (
            (image_object_key is null and image_url is null and image_content_type is null)
            or
            (image_object_key is not null and image_url is not null and image_content_type is not null)
        ),
    add constraint chk_notes_image_content_type
        check (image_content_type is null or image_content_type like 'image/%');

create index idx_notes_image_object_key
    on notes (image_object_key)
    where image_object_key is not null;
