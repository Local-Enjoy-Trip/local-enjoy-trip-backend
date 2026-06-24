DROP INDEX IF EXISTS uk_notifications_business_reference;

CREATE UNIQUE INDEX uk_notifications_business_reference
    ON notifications (recipient_member_id, type, reference_type, reference_id);
