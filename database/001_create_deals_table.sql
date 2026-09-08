CREATE TABLE deals (
    id BIGSERIAL PRIMARY KEY,

    quote_number VARCHAR(100) NOT NULL UNIQUE,

    customer_name VARCHAR(255) NOT NULL,

    deal_value NUMERIC(15, 2) NOT NULL,

    discount_percentage NUMERIC(5, 2) NOT NULL,

    margin_percentage NUMERIC(5, 2) NOT NULL,

    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);
