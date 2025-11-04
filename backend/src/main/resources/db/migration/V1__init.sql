-- Schema initialization for JPA-managed tables

CREATE TABLE IF NOT EXISTS plate_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plate_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS plate_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plate_result_id BIGINT NOT NULL,
    food VARCHAR(100) NOT NULL,
    remaining_ratio DOUBLE NOT NULL,
    CONSTRAINT fk_plate_item_result FOREIGN KEY (plate_result_id)
        REFERENCES plate_result(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_plate_result_created_at ON plate_result(created_at);
CREATE INDEX IF NOT EXISTS idx_plate_item_food ON plate_item(food);

CREATE TABLE IF NOT EXISTS distribution_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu_name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    remaining_count INT NOT NULL,
    active BOOLEAN NOT NULL,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS distribution_claim (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    claimed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dist_claim_session FOREIGN KEY (session_id)
        REFERENCES distribution_session(id) ON DELETE CASCADE,
    CONSTRAINT uq_claim_unique UNIQUE (session_id, user_name)
);

CREATE INDEX IF NOT EXISTS idx_distribution_session_active ON distribution_session(active);

CREATE TABLE IF NOT EXISTS menu_recommendation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    week_start_date DATE NOT NULL,
    day_of_week INT NOT NULL,
    main VARCHAR(100) NOT NULL,
    side1 VARCHAR(100) NOT NULL,
    side2 VARCHAR(100) NOT NULL,
    notes VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_menu_rec_week_day ON menu_recommendation(week_start_date, day_of_week);

-- Users for auth
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(120) NOT NULL,
    password VARCHAR(200) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_email UNIQUE (email)
);

-- Leftover foods
CREATE TABLE IF NOT EXISTS food (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu VARCHAR(255) NOT NULL,
    capacity INT NOT NULL,
    remaining_count INT NOT NULL,
    date DATE NOT NULL,
    note VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    version INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS food_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    food_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    pickup_time TIME NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_food_req_food FOREIGN KEY (food_id) REFERENCES food(id) ON DELETE CASCADE,
    CONSTRAINT fk_food_req_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_food_user UNIQUE (food_id, user_id)
);

-- Waste data
CREATE TABLE IF NOT EXISTS waste_entry (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu VARCHAR(255) NOT NULL,
    remaining INT NOT NULL,
    served INT NOT NULL,
    date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
