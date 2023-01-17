DROP TABLE IF EXISTS g551;
CREATE TABLE g551 (
i0 real,
i1 real,
i2 real,
startdate timestamp,
enddate timestamp
);
COPY g551 (i0,i1,i2, startdate, enddate) FROM '/data/12_random_mix_no_rec/g551_date_10.csv' DELIMITER ',' CSV HEADER;
DROP TABLE IF EXISTS g552;
CREATE TABLE g552 (
startdate timestamp,
enddate timestamp
);
COPY g552 (startdate, enddate) FROM '/data/12_random_mix_no_rec/g552_date_10.csv' DELIMITER ',' CSV HEADER;

WITH 
g558 AS (SELECT g551.i0 as i0, g551.i1 as i1, g551.i2 as i2, startdate, enddate FROM g551), 
g555 AS (SELECT g558.i1 as i0,g558.i0 as i1,g558.i2 as i2, GREATEST(g558.startdate, g552.startdate) as startdate, LEAST(g558.enddate,g552.enddate) as enddate FROM g558 JOIN g552 on (true) WHERE GREATEST(g558.startdate, g552.startdate) <= LEAST(g558.enddate,g552.enddate)), 
g619 AS (SELECT g555.i0 as i0, g555.i1 as i1, g555.i2 as i2, date_trunc('day', startdate) as startdate, date_trunc('day', enddate) + interval '1 day' as enddate  FROM g555), 
g562_timeseries AS (SELECT i0,i1,i2, generate_series(g619.startdate, g619.enddate, '1 seconds') as timeunit FROM g619 ),
g562_aggregation AS (SELECT DISTINCT g562_timeseries.i0 as i0, g562_timeseries.i1 as i1, min(g562_timeseries.i2) OVER(partition by timeunit, g562_timeseries.i0, g562_timeseries.i1  ) as i2, timeunit as startdate, timeunit + INTERVAL '1 seconds' as enddate FROM g562_timeseries),
g562_step_marker AS (SELECT *, lag(enddate) OVER (PARTITION BY i0,i1,i2 ORDER BY startdate) < startdate OR NULL AS step FROM g562_aggregation),
g562_group_marker AS (SELECT *, count(step) OVER (PARTITION BY i0,i1,i2 ORDER BY startdate) AS grp FROM g562_step_marker),
g562_merged AS (SELECT i0,i1,i2, min(startdate) as startdate, max(enddate) as enddate FROM g562_group_marker GROUP BY grp , i0,i1,i2),
g562 AS (SELECT * FROM g562_merged), 
g557 AS (SELECT g562.i1 as i0, g562.i0 as i1, g562.i2 as i2, startdate - interval '78.0 seconds' as startdate, enddate - interval '19.0 seconds' as enddate FROM g562), 
g554 AS (SELECT g557.i1 as i0, g557.i0 as i1, g557.i2 as i2, startdate + interval '17.0 seconds' as startdate, enddate + interval '73.0 seconds' as enddate FROM g557), 
g575_step_marker AS (SELECT *, lag(enddate) OVER ( ORDER BY startdate) < startdate OR NULL AS step FROM g554),
g575_group_marker AS (SELECT *, count(step) OVER ( ORDER BY startdate) AS grp FROM g575_step_marker),
g575_merged AS (SELECT  min(startdate) as startdate, max(enddate) as enddate FROM g575_group_marker GROUP BY grp ),
g575 AS (SELECT  startdate + interval '79.0 seconds' as startdate, enddate + interval '16.0 seconds' as enddate FROM g575_merged WHERE startdate + interval '79.0 seconds' < enddate + interval '16.0 seconds') , 
g622 AS (SELECT g557.i0 as i0, g557.i1 as i1, g557.i2 as i2, startdate, enddate FROM g557), 
g623 AS (SELECT g622.i0 as i0,g622.i2 as i1,g622.i1 as i2, GREATEST(g622.startdate, g575.startdate) as startdate, LEAST(g622.enddate,g575.enddate) as enddate FROM g622 JOIN g575 on (true) WHERE GREATEST(g622.startdate, g575.startdate) <= LEAST(g622.enddate,g575.enddate)), 
g624 AS (SELECT g623.i0 as i0, g623.i1 as i1, g623.i2 as i2, startdate - interval '74.0 seconds' as startdate, enddate - interval '18.0 seconds' as enddate FROM g623), 
g556 AS (SELECT g622.i0 as i0,g622.i2 as i1,g622.i1 as i2, GREATEST(g622.startdate, g624.startdate) as startdate, LEAST(g622.enddate,g624.enddate) as enddate FROM g622 JOIN g624 on (g622.i0=g624.i0 AND g622.i2=g624.i1) WHERE GREATEST(g622.startdate, g624.startdate) <= LEAST(g622.enddate,g624.enddate)), 
g553 AS (SELECT g556.i2 as i0, g556.i1 as i1, g556.i0 as i2, startdate, enddate FROM g556), 
output_step_marker AS (SELECT *, lag(enddate) OVER (PARTITION BY i0,i1,i2 ORDER BY startdate) < startdate OR NULL AS step FROM g553),
output_group_marker AS (SELECT *, count(step) OVER (PARTITION BY i0,i1,i2 ORDER BY startdate) AS grp FROM output_step_marker),
output_merged AS (SELECT i0,i1,i2, min(startdate) as startdate, max(enddate) as enddate FROM output_group_marker GROUP BY grp , i0,i1,i2)
SELECT * FROM output_merged;