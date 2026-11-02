-- Admins deactivate accounts instead of deleting them, so bookings, payments and orders stay on record.
ALTER TABLE user_account ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

-- Photos uploaded from the admin screens. Stored in the database so they survive redeploys of the API container.
CREATE TABLE uploaded_image (
    id INT NOT NULL AUTO_INCREMENT,
    content_type VARCHAR(20) NOT NULL,
    data MEDIUMBLOB NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id)
);
