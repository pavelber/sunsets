create table CELL
(
    date   DATE,
    square_size  NUMERIC(12,4),
    latitude  NUMERIC(12,4),
    longitude NUMERIC(12,4),
    low NUMERIC(12,4),
    medium NUMERIC(12,4),
    high NUMERIC(12,4),
    sunset_near NUMERIC(12,4),
    sunset_far NUMERIC(12,4),
    sun_blocking_5 NUMERIC(12,4),
    sun_blocking_10 NUMERIC(12,4),
    PRIMARY KEY (date,latitude,longitude)
)