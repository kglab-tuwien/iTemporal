DROP TABLE IF EXISTS g774;

CREATE TABLE g774 (
	i0 real,
	i1 real,
	startdate timestamp,
	enddate timestamp
);


COPY g774 (i0, i1, startdate, enddate) FROM '/data/09_box_diamond_mix/g774_date_10.csv' DELIMITER ',' CSV HEADER;

DROP TABLE IF EXISTS g775;

CREATE TABLE g775 (
	i0 real,
	i1 real,
	startdate timestamp,
	enddate timestamp
);


COPY g775 (i0, i1, startdate, enddate) FROM '/data/09_box_diamond_mix/g775_date_10.csv' DELIMITER ',' CSV HEADER;

WITH g780 AS (
	SELECT i0,i1, startdate - interval '77 seconds' as startdate, enddate - interval '30 seconds' as enddate FROM g774
),
g781 AS (
	SELECT i0,i1, startdate + interval '29 seconds' as startdate, enddate + interval '74 seconds' as enddate FROM g780
),
g777 AS (
	SELECT * FROM g781 UNION SELECT * FROM g775
),
g786 AS (
	SELECT i0,i1, startdate - interval '78 seconds' as startdate, enddate - interval '30 seconds' as enddate FROM g777
),
g779_step_marker as (SELECT *, lag(enddate) OVER (PARTITION BY i0,i1 ORDER BY startdate) < startdate OR NULL AS step FROM g786),
g779_group_marker as (SELECT *, count(step) OVER (PARTITION BY i0,i1 ORDER BY startdate) AS grp FROM g779_step_marker),
g779_merged as (SELECT i0,i1, min(startdate) as startdate, max(enddate) as enddate FROM g779_group_marker GROUP BY grp, i0, i1),
g779 AS (
	SELECT i0, i1, startdate - interval '28 seconds' as startdate, enddate - interval '76 seconds' as enddate FROM g779_merged where startdate - interval '28 seconds' < enddate - interval '76 seconds'
),
g795 AS (
	-- We know that g779 is merged so we can skip the merging code again
	SELECT i0, i1, startdate + interval '75 seconds' as startdate, enddate + interval '29 seconds' as enddate FROM g779 where startdate + interval '75 seconds' < enddate + interval '29 seconds'
),
g798 AS (
	SELECT i0,i1, startdate + interval '28 seconds' as startdate, enddate + interval '77 seconds' as enddate FROM g779
),
g778 AS (
	SELECT g795.i0, g795.i1, GREATEST(g798.startdate, g795.startdate) as startdate, LEAST(g798.enddate,g795.enddate) as enddate FROM g798 JOIN g795 on (g798.i0=g795.i0) WHERE GREATEST(g798.startdate, g795.startdate) <= LEAST(g798.enddate,g795.enddate)
),
g801 AS (
	SELECT i0,i1, startdate - interval '75 seconds' as startdate, enddate - interval '30 seconds' as enddate FROM g778
),
g776 AS (
	SELECT i0,i1, startdate + interval '29 seconds' as startdate, enddate + interval '78 seconds' as enddate FROM g801
),
output_step_marker as (SELECT *, lag(enddate) OVER (PARTITION BY i0,i1 ORDER BY startdate) < startdate OR NULL AS step FROM g776),
output_group_marker as (SELECT *, count(step) OVER (PARTITION BY i0,i1 ORDER BY startdate) AS grp FROM output_step_marker),
output_merged as (SELECT i0,i1, min(startdate) as startdate, max(enddate) as enddate FROM output_group_marker GROUP BY grp, i0, i1)
SELECT * FROM output_merged;