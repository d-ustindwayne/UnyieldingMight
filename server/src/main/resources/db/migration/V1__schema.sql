-- Unyielding Might schema (MySQL 8 / MariaDB 10.4+).
-- Translated from the original SQL Server scripts in db_queries/legacy-mssql and restructured:
--   * one user_account table with a role column instead of UserProfile + Customer duplicates
--   * body stats live in customer_profile; TDEE/BMR/age are derived, not stored
--   * bookings, payments, shop items and orders are first-class tables
-- All timestamps are stored as UTC DATETIME values written by the server.

CREATE TABLE address (
    id INT NOT NULL AUTO_INCREMENT,
    street VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    region VARCHAR(50) NOT NULL,
    postcode VARCHAR(10) NOT NULL,
    country VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE membership_plan (
    id INT NOT NULL AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    price_cents INT NOT NULL,
    duration_days INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uq_membership_plan_code UNIQUE (code),
    CONSTRAINT ck_membership_plan_price CHECK (price_cents >= 0),
    CONSTRAINT ck_membership_plan_duration CHECK (duration_days > 0)
);

-- A membership is issued by the gym (front desk) or bought in the app.
-- Gym-issued cards carry the member's details so registration can be prefilled when linking.
CREATE TABLE membership (
    id INT NOT NULL AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(6) NOT NULL,
    height_cm DECIMAL(5,2) NOT NULL,
    weight_kg DECIMAL(5,2) NOT NULL,
    activity_level VARCHAR(10) NOT NULL,
    address_id INT NULL,
    plan_id INT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    starts_at DATETIME NOT NULL,
    expires_at DATETIME NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_membership_address FOREIGN KEY (address_id) REFERENCES address (id),
    CONSTRAINT fk_membership_plan FOREIGN KEY (plan_id) REFERENCES membership_plan (id),
    CONSTRAINT ck_membership_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    CONSTRAINT ck_membership_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED'))
);

CREATE INDEX idx_membership_email ON membership (email);

CREATE TABLE user_account (
    id INT NOT NULL AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(72) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(6) NOT NULL,
    role VARCHAR(8) NOT NULL DEFAULT 'CUSTOMER',
    address_id INT NULL,
    newsletter_opt_in BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_user_account_email UNIQUE (email),
    CONSTRAINT fk_user_account_address FOREIGN KEY (address_id) REFERENCES address (id),
    CONSTRAINT ck_user_account_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    CONSTRAINT ck_user_account_role CHECK (role IN ('ADMIN', 'CUSTOMER'))
);

-- Only customers have body stats; admins don't need them.
CREATE TABLE customer_profile (
    user_id INT NOT NULL,
    membership_id INT NULL,
    height_cm DECIMAL(5,2) NOT NULL,
    weight_kg DECIMAL(5,2) NOT NULL,
    activity_level VARCHAR(10) NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT uq_customer_profile_membership UNIQUE (membership_id),
    CONSTRAINT fk_customer_profile_user FOREIGN KEY (user_id) REFERENCES user_account (id) ON DELETE CASCADE,
    CONSTRAINT fk_customer_profile_membership FOREIGN KEY (membership_id) REFERENCES membership (id),
    CONSTRAINT ck_customer_profile_height CHECK (height_cm BETWEEN 50 AND 272),
    CONSTRAINT ck_customer_profile_weight CHECK (weight_kg BETWEEN 20 AND 400)
);

CREATE TABLE email_otp (
    id INT NOT NULL AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL,
    purpose VARCHAR(16) NOT NULL,
    code_hash VARCHAR(64) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    expires_at DATETIME NOT NULL,
    consumed_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_email_otp_lookup ON email_otp (email, purpose);

CREATE TABLE trainer (
    id INT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    bio VARCHAR(500) NOT NULL,
    specialty VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
);

CREATE TABLE gym_class (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    category VARCHAR(16) NOT NULL,
    trainer_id INT NULL,
    starts_at DATETIME NOT NULL,
    ends_at DATETIME NOT NULL,
    max_capacity INT NOT NULL,
    booked_count INT NOT NULL DEFAULT 0,
    avg_calories INT NOT NULL,
    price_cents INT NOT NULL DEFAULT 0,
    status VARCHAR(10) NOT NULL DEFAULT 'SCHEDULED',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_gym_class_trainer FOREIGN KEY (trainer_id) REFERENCES trainer (id) ON DELETE SET NULL,
    CONSTRAINT ck_gym_class_time CHECK (ends_at > starts_at),
    CONSTRAINT ck_gym_class_capacity CHECK (max_capacity > 0 AND booked_count >= 0 AND booked_count <= max_capacity),
    CONSTRAINT ck_gym_class_price CHECK (price_cents >= 0),
    CONSTRAINT ck_gym_class_status CHECK (status IN ('SCHEDULED', 'CANCELLED', 'COMPLETED'))
);

CREATE INDEX idx_gym_class_starts_at ON gym_class (starts_at);

CREATE TABLE payment (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    purpose VARCHAR(16) NOT NULL,
    reference_id INT NOT NULL,
    amount_cents INT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    provider VARCHAR(8) NOT NULL,
    provider_ref VARCHAR(100) NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES user_account (id),
    CONSTRAINT ck_payment_amount CHECK (amount_cents >= 0),
    CONSTRAINT ck_payment_status CHECK (status IN ('PENDING', 'SUCCEEDED', 'FAILED', 'CANCELLED', 'REFUNDED'))
);

CREATE INDEX idx_payment_user ON payment (user_id);

CREATE TABLE class_booking (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    class_id INT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'BOOKED',
    payment_id INT NULL,
    booked_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_class_booking UNIQUE (user_id, class_id),
    CONSTRAINT fk_class_booking_user FOREIGN KEY (user_id) REFERENCES user_account (id) ON DELETE CASCADE,
    CONSTRAINT fk_class_booking_class FOREIGN KEY (class_id) REFERENCES gym_class (id) ON DELETE CASCADE,
    CONSTRAINT fk_class_booking_payment FOREIGN KEY (payment_id) REFERENCES payment (id),
    CONSTRAINT ck_class_booking_status CHECK (status IN ('BOOKED', 'CANCELLED', 'ATTENDED', 'CLASS_CANCELLED'))
);

CREATE INDEX idx_class_booking_class ON class_booking (class_id);

CREATE TABLE shop_item (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    category VARCHAR(16) NOT NULL,
    price_cents INT NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    image_url VARCHAR(500) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_shop_item_price CHECK (price_cents >= 0),
    CONSTRAINT ck_shop_item_stock CHECK (stock >= 0)
);

CREATE TABLE shop_order (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    total_cents INT NOT NULL,
    payment_id INT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_shop_order_user FOREIGN KEY (user_id) REFERENCES user_account (id) ON DELETE CASCADE,
    CONSTRAINT fk_shop_order_payment FOREIGN KEY (payment_id) REFERENCES payment (id),
    CONSTRAINT ck_shop_order_status CHECK (status IN ('PENDING', 'PAID', 'CANCELLED'))
);

CREATE TABLE shop_order_line (
    order_id INT NOT NULL,
    item_id INT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    unit_price_cents INT NOT NULL,
    PRIMARY KEY (order_id, item_id),
    CONSTRAINT fk_shop_order_line_order FOREIGN KEY (order_id) REFERENCES shop_order (id) ON DELETE CASCADE,
    CONSTRAINT fk_shop_order_line_item FOREIGN KEY (item_id) REFERENCES shop_item (id),
    CONSTRAINT ck_shop_order_line_quantity CHECK (quantity > 0)
);
