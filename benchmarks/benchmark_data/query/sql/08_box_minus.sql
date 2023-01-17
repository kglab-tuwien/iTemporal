DROP TABLE IF EXISTS g732;

CREATE TABLE g732 (
	i0 real,
	i1 real,
	startdate timestamp,
	enddate timestamp
);


COPY g732 (i0, i1, startdate, enddate) FROM '/data/08_box_minus/g732_date_10.csv' DELIMITER ',' CSV HEADER;


-- Box minus requires to merge intervals together before shifting intervals

WITH g732_step_marker as (SELECT *, lag(enddate) OVER (PARTITION BY i0,i1 ORDER BY startdate) < startdate OR NULL AS step FROM g732),
g732_group_marker as (SELECT *, count(step) OVER (PARTITION BY i0,i1 ORDER BY startdate) AS grp FROM g732_step_marker),
g732_merged as (SELECT i0,i1, min(startdate) as startdate, max(enddate) as enddate FROM g732_group_marker GROUP BY grp, i0, i1)
SELECT i1, i0, startdate + interval '76 seconds' as startdate, enddate + interval '29 seconds' as enddate FROM g732_merged where startdate + interval '76 seconds' < enddate + interval '29 seconds';

