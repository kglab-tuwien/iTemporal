DROP TABLE IF EXISTS start_date;

CREATE TABLE start_date (
	i0 DOUBLE PRECISION,
	i1 DOUBLE PRECISION,
	i2 DOUBLE PRECISION,
	i3 timestamp,
	i4 timestamp
);

COPY start_date (i0, i1, i2, i3, i4) FROM '/data/01_sta/start_date_10.csv' DELIMITER ',' CSV HEADER;


--SELECT DISTINCT i0,i1, date_trunc('month', i3) as start_month, date_trunc('month', i4) + interval '1 month'  as end_month, SUM(i2) OVER(PARTITION BY i3, i0, i1) 
--FROM start_date 
--GROUP BY i0,i1,start_month, end_month, i2,i3;

WITH a as (SELECT DISTINCT  i0,i1,i2, generate_series(
	date_trunc('month', i3),
	date_trunc('month', i4),
	'1 month'
)::date as month from start_date)
SELECT DISTINCT month, i0, i1, sum(i2) OVER(partition by month,i0,i1) FROM a;