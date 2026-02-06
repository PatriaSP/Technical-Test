DROP TABLE IF EXISTS orders CASCADE;

CREATE TABLE orders (
    order_no VARCHAR(255) PRIMARY KEY,
    item_id BIGINT NOT NULL,
    inventory_id BIGINT NOT NULL,
    qty INT NOT NULL,
    price DECIMAL(15, 2) NOT NULL
);

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_item
    FOREIGN KEY (item_id) REFERENCES item(id);
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_inventory
    FOREIGN KEY (inventory_id) REFERENCES inventory(id);