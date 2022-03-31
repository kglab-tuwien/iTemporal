DROP TABLE IF EXISTS a;
CREATE TABLE a (
i0 real,
i1 real,
i2 real,
i3 real,
startdate timestamp,
enddate timestamp
);
COPY a (i0,i1,i2,i3, startdate, enddate) FROM '/data/11_diamond_form/a_date_10.csv' DELIMITER ',' CSV HEADER;
WITH 
c_timeseries AS (SELECT i0,i1,i2,i3, generate_series(a.startdate, a.enddate, '1 seconds') as timeunit FROM a ),
c_contributor_aggregation AS (SELECT DISTINCT max(c_timeseries.i0) OVER(partition by timeunit, c_timeseries.i1, c_timeseries.i2, c_timeseries.i3) as i0, c_timeseries.i1 as i1, c_timeseries.i2 as i2, c_timeseries.i3 as i3, timeunit FROM c_timeseries),
c_aggregation AS (SELECT DISTINCT c_contributor_aggregation.i2 as i0, sum(c_contributor_aggregation.i0) OVER(partition by timeunit, c_contributor_aggregation.i2  ) as i1, timeunit as startdate, timeunit + INTERVAL '1 seconds' as enddate FROM c_contributor_aggregation),
c_step_marker AS (SELECT *, lag(enddate) OVER (PARTITION BY i0,i1 ORDER BY startdate) < startdate OR NULL AS step FROM c_aggregation),
c_group_marker AS (SELECT *, count(step) OVER (PARTITION BY i0,i1 ORDER BY startdate) AS grp FROM c_step_marker),
c_merged AS (SELECT i0,i1, min(startdate) as startdate, max(enddate) as enddate FROM c_group_marker GROUP BY grp, i0,i1),
c AS (SELECT * FROM c_merged), 
b AS (SELECT a.i1 as i0, a.i2 as i1, a.i3 as i2, a.i0 as i3, startdate + interval '2.0 seconds' as startdate, enddate + interval '5.0 seconds' as enddate FROM a), 
d AS (SELECT b.i0 as i0,c.i1 as i1, GREATEST(b.startdate, c.startdate) as startdate, LEAST(b.enddate,c.enddate) as enddate FROM b JOIN c on (b.i0=c.i0) WHERE GREATEST(b.startdate, c.startdate) <= LEAST(b.enddate,c.enddate)), 
output_step_marker AS (SELECT *, lag(enddate) OVER (PARTITION BY i0,i1 ORDER BY startdate) < startdate OR NULL AS step FROM d),
output_group_marker AS (SELECT *, count(step) OVER (PARTITION BY i0,i1 ORDER BY startdate) AS grp FROM output_step_marker),
output_merged AS (SELECT i0,i1, min(startdate) as startdate, max(enddate) as enddate FROM output_group_marker GROUP BY grp, i0,i1)SELECT * FROM output_merged;