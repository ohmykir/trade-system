INSERT INTO instruments (seccode, type, lot_size, min_step) VALUES
    ('SBER',         'STOCK',         10,   0.0100),
    ('GAZP',         'STOCK',         10,   0.0500),
    ('SU26238RMFS4', 'BOND',          1,    0.0100),
    ('CNYRUB_TOM',   'CURRENCY_PAIR', 1000, 0.0010);

INSERT INTO accounts (id, user_id, currency, balance, reserved) VALUES
    (1, 1, 'RUB', 1000000.0000, 0),
    (2, 2, 'RUB', 1000000.0000, 0),
    (3, 3, 'RUB',  500000.0000, 0),
    (4, 4, 'RUB',    1000.0000, 0);