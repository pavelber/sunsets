ALTER TABLE CACHE
    DROP CONSTRAINT SYS_PK_10108;
ALTER TABLE CACHE
    ADD PRIMARY KEY (latitude, longitude, time, source);