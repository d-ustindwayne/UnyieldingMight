-- Class photos. Either an https URL or a path to an image the API hosts under /images/.
ALTER TABLE gym_class ADD COLUMN image_url VARCHAR(500) NULL;
