alter table members
    drop constraint if exists chk_members_representative_location_pair,
    drop constraint if exists chk_members_representative_longitude,
    drop constraint if exists chk_members_representative_latitude;

alter table members
    drop column if exists representative_region_name,
    drop column if exists representative_longitude,
    drop column if exists representative_latitude;
