alter table cell
    alter column SUN_BLOCKING_5 rename to sun_blocking_near;
alter table cell
    alter column SUN_BLOCKING_10 rename to sun_blocking_far;
alter table cell
    add column rank NUMERIC(12, 4);
alter table cell
    add column description varchar(512);