ALTER TABLE notifications
ADD COLUMN transfer_id UUID;

UPDATE notifications
SET transfer_id = gen_random_uuid()
WHERE transfer_id IS NULL;

ALTER TABLE notifications
ALTER COLUMN transfer_id SET NOT NULL;

CREATE INDEX idx_notification_transfer
ON notifications (transfer_id);

ALTER TABLE notifications
ADD CONSTRAINT uk_notification_transfer_user_type
UNIQUE (transfer_id, user_id, type);