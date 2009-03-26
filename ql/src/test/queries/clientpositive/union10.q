set hive.map.aggr = true;

-- union case: all subqueries are a map-reduce jobs, 3 way union, same input for all sub-queries, followed by filesink

explain 
  select unionsrc.key, unionsrc.value FROM (select 'tst1' as key, count(1) as value from src s1
                                        UNION  ALL  
                                            select 'tst2' as key, count(1) as value from src s2
                                        UNION ALL
                                            select 'tst3' as key, count(1) as value from src s3) unionsrc;



  select unionsrc.key, unionsrc.value FROM (select 'tst1' as key, count(1) as value from src s1
                                        UNION  ALL  
                                            select 'tst2' as key, count(1) as value from src s2
                                        UNION ALL
                                            select 'tst3' as key, count(1) as value from src s3) unionsrc;



