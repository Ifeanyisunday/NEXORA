CREATE INDEX idx_transfer_sender
    ON transfers(sender_id);

CREATE INDEX idx_transfer_receiver
    ON transfers(receiver_id);

CREATE INDEX idx_transfer_created_at
    ON transfers(created_at);