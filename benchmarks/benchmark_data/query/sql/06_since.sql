DROP TABLE IF EXISTS g1;
DROP TABLE IF EXISTS g2;

CREATE TABLE g1 (
	i0 real,
	i1 real,
	i2 timestamp,
	i3 timestamp
);


CREATE TABLE g2 (
	i0 real,
	i1 real,
	i2 timestamp,
	i3 timestamp
);

COPY g1 (i0, i1, i2, i3) FROM '/data/06_since/g1_date_10.csv' DELIMITER ',' CSV HEADER;
COPY g2 (i0, i1, i2, i3) FROM '/data/06_since/g2_date_10.csv' DELIMITER ',' CSV HEADER;

WITH a as (
	-- Diamond Minus
	SELECT g1.i0,g1.i1, g1.i2 + interval '1 seconds' as startdate, g1.i3 + interval '3 seconds' as enddate FROM g1
),
b as (
	-- First intersection
	SELECT g2.i0, g2.i1, GREATEST(g1.i2, g2.i2) as startdate, LEAST(g1.i3,g2.i3) as enddate FROM g1 JOIN g2 on (g1.i1=g2.i0 and g1.i0=g2.i1) WHERE GREATEST(g1.i2, g2.i2) <= LEAST(g1.i3,g2.i3)

)
-- Second intersection
SELECT a.i0, a.i1, GREATEST(a.startdate, b.startdate) as startdate, LEAST(a.enddate,b.enddate) as enddate FROM a JOIN b on (a.i1=b.i0 and a.i0=b.i1) WHERE GREATEST(a.startdate, b.startdate) <= LEAST(a.enddate,b.enddate);

