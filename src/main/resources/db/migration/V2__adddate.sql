alter TABLE DARKSKY add COLUMN day char (10);
update DARKSKY set day = cast(TIMESTAMP (time) as date);