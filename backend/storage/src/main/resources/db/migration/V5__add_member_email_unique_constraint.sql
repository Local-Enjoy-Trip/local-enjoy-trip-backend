alter table members
    add constraint uk_members_email unique (email);
