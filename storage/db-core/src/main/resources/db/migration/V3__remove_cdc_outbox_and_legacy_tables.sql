alter table if exists notifications drop constraint if exists uk_notifications_outbox_event;
alter table if exists notifications drop constraint if exists fk_notifications_outbox;
alter table if exists notifications drop column if exists outbox_event_id;

create unique index if not exists uk_notifications_business_reference
    on notifications (recipient_user_id, type, reference_type, reference_id);

drop table if exists notification_outbox;
drop table if exists attraction_photos;
drop table if exists sunrise_sunset;
drop table if exists member_settings;
