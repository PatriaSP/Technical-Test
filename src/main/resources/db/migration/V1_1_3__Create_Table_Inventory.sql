CREATE TYPE IF NOT EXISTS type_enum AS ENUM (
    'T',
    'W'
);

DROP TABLE IF EXISTS inventory CASCADE;

CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    qty INT NOT NULL,
    type type_enum NOT NULL
);

ALTER TABLE inventory
    ADD CONSTRAINT fk_inventory_item
    FOREIGN KEY (item_id) REFERENCES item(id);