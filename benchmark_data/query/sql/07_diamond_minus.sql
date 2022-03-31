DROP TABLE IF EXISTS g707;

CREATE TABLE g707 (
	i0 real,
	i1 real,
	i2 timestamp,
	i3 timestamp
);


COPY g707 (i0, i1, i2, i3) FROM '/data/07_diamond_minus/g707_date_10.csv' DELIMITER ',' CSV HEADER;


SELECT i0,i1, i2 + interval '7 seconds' as startdate, i3 + interval '97 seconds' as enddate FROM g707;

