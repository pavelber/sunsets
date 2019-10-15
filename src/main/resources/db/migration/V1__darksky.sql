CREATE CACHED TABLE darksky (
  latitude  INT,
  longitude INT,
  time      INT,
  json      LONGVARCHAR,
  PRIMARY KEY (latitude, longitude, time)
)