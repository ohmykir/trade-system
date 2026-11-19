CREATE SEQUENCE IF NOT EXISTS accounts_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS orders_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS trades_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE accounts
(
    id       BIGINT         PRIMARY KEY,
    user_id  BIGINT         NOT NULL,
    currency VARCHAR(3)     NOT NULL,
    balance  DECIMAL(19, 4) NOT NULL DEFAULT 0,
    reserved DECIMAL(19, 4) NOT NULL DEFAULT 0,

    CONSTRAINT uq_accounts_user_currency UNIQUE (user_id, currency),
    CONSTRAINT ck_accounts_balance  CHECK (balance >= 0),
    CONSTRAINT ck_accounts_reserved CHECK (reserved >= 0 AND reserved <= balance)
);

CREATE TABLE instruments
(
    seccode  VARCHAR(16)    PRIMARY KEY,
    type     VARCHAR(32)    NOT NULL,
    lot_size INTEGER        NOT NULL,
    min_step DECIMAL(19, 4) NOT NULL,

    CONSTRAINT ck_instruments_type CHECK (type IN ('STOCK', 'BOND', 'CURRENCY_PAIR')),
    CONSTRAINT ck_instruments_lot  CHECK (lot_size > 0),
    CONSTRAINT ck_instruments_step CHECK (min_step > 0)
);

CREATE TABLE orders
(
    id              BIGINT                      PRIMARY KEY,
    seccode         VARCHAR(16)                 NOT NULL REFERENCES instruments(seccode),
    type            VARCHAR(4)                  NOT NULL,
    account_id      BIGINT                      NOT NULL REFERENCES accounts(id),
    price           DECIMAL(19, 4)              NOT NULL,
    quantity        INTEGER                     NOT NULL,
    filled_quantity INTEGER                     NOT NULL DEFAULT 0,
    status          VARCHAR(16)                 NOT NULL DEFAULT 'NEW',
    idempotency_key VARCHAR(64),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT ck_orders_type     CHECK (type IN ('BUY', 'SELL')),
    CONSTRAINT ck_orders_status   CHECK (status IN ('NEW', 'PARTIALLY_FILLED', 'FILLED', 'CANCELLED')),
    CONSTRAINT ck_orders_price    CHECK (price > 0),
    CONSTRAINT ck_orders_quantity CHECK (quantity > 0),
    CONSTRAINT ck_orders_filled   CHECK (filled_quantity >= 0 AND filled_quantity <= quantity)
);

CREATE UNIQUE INDEX uq_orders_idempotency
    ON orders (account_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;

CREATE INDEX ix_orders_buy
    ON orders (seccode, price DESC, created_at, id)
    WHERE type = 'BUY' AND status IN ('NEW', 'PARTIALLY_FILLED');

CREATE INDEX ix_orders_sell
    ON orders (seccode, price ASC, created_at, id)
    WHERE type = 'SELL' AND status IN ('NEW', 'PARTIALLY_FILLED');

CREATE TABLE trades
(
    id            BIGINT                      PRIMARY KEY,
    seccode       VARCHAR(16)                 NOT NULL REFERENCES instruments(seccode),
    buy_order_id  BIGINT                      NOT NULL REFERENCES orders(id),
    sell_order_id BIGINT                      NOT NULL REFERENCES orders(id),
    price         DECIMAL(19, 4)              NOT NULL,
    quantity      INTEGER                     NOT NULL,
    executed_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT ck_trades_price    CHECK (price > 0),
    CONSTRAINT ck_trades_quantity CHECK (quantity > 0)
);

CREATE INDEX ix_trades_seccode_time ON trades (seccode, executed_at DESC);