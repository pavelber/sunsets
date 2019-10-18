alter table DARKSKY RENAME TO CACHE;
alter TABLE CACHE add COLUMN source VARCHAR(32);
UPDATE CACHE set source = 'darksky';