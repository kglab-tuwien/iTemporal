DROP TABLE IF EXISTS start_date;

CREATE TABLE start_date (
	i0 real,
	i1 real,
	i2 real,
	i3 timestamp,
	i4 timestamp
);

COPY start_date (i0, i1, i2, i3, i4) FROM '/data/02_mwta/start_date_10.csv' DELIMITER ',' CSV HEADER;

--INSERT INTO start_date (i0, i1, i2, i3, i4) VALUES (532.0,568.0,131.0,'2020-01-31 14:51:30','2020-01-31 14:51:30');

-- Note this query merges intervals at the end together. If this is not required, b already presents the result


WITH a as (SELECT DISTINCT i0,i1,i2, generate_series(i3, i4 + interval '10 seconds', '1 seconds') as timeunit
FROM start_date),
b as (SELECT DISTINCT timeunit as startdate,timeunit + INTERVAL '1 seconds' as enddate, i0, i1, sum(i2) OVER(partition by timeunit,i0,i1) FROM a),
c as (SELECT *, lag(enddate) OVER (PARTITION BY i0,i1,sum ORDER BY startdate) < startdate OR NULL AS step FROM b),
d as (SELECT *, count(step) OVER (PARTITION BY i0,i1,sum ORDER BY startdate) AS grp FROM c)
SELECT i0,i1,sum, min(startdate), max(enddate) FROM d GROUP BY grp, i0, i1, sum;

