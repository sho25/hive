set hive.mapred.mode=nonstrict;
set hive.explain.user=false;
set hive.fetch.task.conversion=none;
set tez.grouping.min-size=1;
set tez.grouping.max-size=2;
set hive.exec.dynamic.partition.mode=nonstrict;
set hive.support.concurrency=true;
set hive.txn.manager=org.apache.hadoop.hive.ql.lockmgr.DbTxnManager;


-- Force multiple writers when reading
drop table intermediate;
create table intermediate(key int) partitioned by (p int) stored as orc;
insert into table intermediate partition(p='455') select distinct key from src where key >= 0 order by key desc limit 2;
insert into table intermediate partition(p='456') select distinct key from src where key is not null order by key asc limit 2;
insert into table intermediate partition(p='457') select distinct key from src where key >= 100 order by key asc limit 2;


drop table part_mm;
create table part_mm(key int) partitioned by (key_mm int) stored as orc tblproperties ("transactional"="true", "transactional_properties"="insert_only");
explain insert into table part_mm partition(key_mm=455) select key from intermediate;
insert into table part_mm partition(key_mm=455) select key from intermediate;
insert into table part_mm partition(key_mm=456) select key from intermediate;
insert into table part_mm partition(key_mm=455) select key from intermediate;
select * from part_mm order by key, key_mm;

-- TODO: doesn't work truncate table part_mm partition(key_mm=455);
select * from part_mm order by key, key_mm;
truncate table part_mm;
select * from part_mm order by key, key_mm;
drop table part_mm;

drop table simple_mm;
create table simple_mm(key int) stored as orc tblproperties ("transactional"="true", "transactional_properties"="insert_only");
insert into table simple_mm select key from intermediate;
select * from simple_mm order by key;
insert into table simple_mm select key from intermediate;
select * from simple_mm order by key;
truncate table simple_mm;
select * from simple_mm;
drop table simple_mm;


-- simple DP (no bucketing)
drop table dp_mm;

set hive.exec.dynamic.partition.mode=nonstrict;

set hive.merge.mapredfiles=false;
set hive.merge.sparkfiles=false;
set hive.merge.tezfiles=false;

create table dp_mm (key int) partitioned by (key1 string, key2 int) stored as orc
  tblproperties ("transactional"="true", "transactional_properties"="insert_only");

insert into table dp_mm partition (key1='123', key2) select key, key from intermediate;

select * from dp_mm order by key;

drop table dp_mm;


-- union

create table union_mm(id int)  tblproperties ("transactional"="true", "transactional_properties"="insert_only");
insert into table union_mm 
select temps.p from (
select key as p from intermediate 
union all 
select key + 1 as p from intermediate ) temps;

select * from union_mm order by id;

insert into table union_mm 
select p from
(
select key + 1 as p from intermediate
union all
select key from intermediate
) tab group by p
union all
select key + 2 as p from intermediate;

select * from union_mm order by id;

insert into table union_mm
SELECT p FROM
(
  SELECT key + 1 as p FROM intermediate
  UNION ALL
  SELECT key as p FROM ( 
    SELECT distinct key FROM (
      SELECT key FROM (
        SELECT key + 2 as key FROM intermediate
        UNION ALL
        SELECT key FROM intermediate
      )t1 
    group by key)t2
  )t3
)t4
group by p;


select * from union_mm order by id;
drop table union_mm;


create table partunion_mm(id int) partitioned by (key int) tblproperties ("transactional"="true", "transactional_properties"="insert_only");
insert into table partunion_mm partition(key)
select temps.* from (
select key as p, key from intermediate 
union all 
select key + 1 as p, key + 1 from intermediate ) temps;

select * from partunion_mm order by id;
drop table partunion_mm;



create table skew_mm(k1 int, k2 int, k4 int) skewed by (k1, k4) on ((0,0),(1,1),(2,2),(3,3))
 stored as directories tblproperties ("transactional"="true", "transactional_properties"="insert_only");

insert into table skew_mm 
select key, key, key from intermediate;

select * from skew_mm order by k2, k1, k4;
drop table skew_mm;


create table skew_dp_union_mm(k1 int, k2 int, k4 int) partitioned by (k3 int) 
skewed by (k1, k4) on ((0,0),(1,1),(2,2),(3,3)) stored as directories tblproperties ("transactional"="true", "transactional_properties"="insert_only");

insert into table skew_dp_union_mm partition (k3)
select key as i, key as j, key as k, key as l from intermediate
union all 
select key +1 as i, key +2 as j, key +3 as k, key +4 as l from intermediate;


select * from skew_dp_union_mm order by k2, k1, k4;
drop table skew_dp_union_mm;



set hive.merge.orcfile.stripe.level=true;
set hive.merge.tezfiles=true;
set hive.merge.mapfiles=true;
set hive.merge.mapredfiles=true;


create table merge0_mm (id int) stored as orc tblproperties("transactional"="true", "transactional_properties"="insert_only");

insert into table merge0_mm select key from intermediate;
select * from merge0_mm;

set tez.grouping.split-count=1;
insert into table merge0_mm select key from intermediate;
set tez.grouping.split-count=0;
select * from merge0_mm;

drop table merge0_mm;


create table merge2_mm (id int) tblproperties("transactional"="true", "transactional_properties"="insert_only");

insert into table merge2_mm select key from intermediate;
select * from merge2_mm;

set tez.grouping.split-count=1;
insert into table merge2_mm select key from intermediate;
set tez.grouping.split-count=0;
select * from merge2_mm;

drop table merge2_mm;


create table merge1_mm (id int) partitioned by (key int) stored as orc tblproperties("transactional"="true", "transactional_properties"="insert_only");

insert into table merge1_mm partition (key) select key, key from intermediate;
select * from merge1_mm order by id, key;

set tez.grouping.split-count=1;
insert into table merge1_mm partition (key) select key, key from intermediate;
set tez.grouping.split-count=0;
select * from merge1_mm order by id, key;

drop table merge1_mm;

set hive.merge.tezfiles=false;
set hive.merge.mapfiles=false;
set hive.merge.mapredfiles=false;

-- TODO: need to include merge+union+DP, but it's broken for now


drop table load0_mm;
create table load0_mm (key string, value string) stored as textfile tblproperties("transactional"="true", "transactional_properties"="insert_only");
load data local inpath '../../data/files/kv1.txt' into table load0_mm;
select count(1) from load0_mm;
load data local inpath '../../data/files/kv2.txt' into table load0_mm;
select count(1) from load0_mm;
load data local inpath '../../data/files/kv2.txt' overwrite into table load0_mm;
select count(1) from load0_mm;
drop table load0_mm;


drop table intermediate2;
create table intermediate2 (key string, value string) stored as textfile
location 'file:${system:test.tmp.dir}/intermediate2';
load data local inpath '../../data/files/kv1.txt' into table intermediate2;
load data local inpath '../../data/files/kv2.txt' into table intermediate2;
load data local inpath '../../data/files/kv3.txt' into table intermediate2;

drop table load1_mm;
create table load1_mm (key string, value string) stored as textfile tblproperties("transactional"="true", "transactional_properties"="insert_only");
load data inpath 'file:${system:test.tmp.dir}/intermediate2/kv2.txt' into table load1_mm;
load data inpath 'file:${system:test.tmp.dir}/intermediate2/kv1.txt' into table load1_mm;
select count(1) from load1_mm;
load data local inpath '../../data/files/kv1.txt' into table intermediate2;
load data local inpath '../../data/files/kv2.txt' into table intermediate2;
load data local inpath '../../data/files/kv3.txt' into table intermediate2;
load data inpath 'file:${system:test.tmp.dir}/intermediate2/kv*.txt' overwrite into table load1_mm;
select count(1) from load1_mm;
load data local inpath '../../data/files/kv2.txt' into table intermediate2;
load data inpath 'file:${system:test.tmp.dir}/intermediate2/kv2.txt' overwrite into table load1_mm;
select count(1) from load1_mm;
drop table load1_mm;

drop table load2_mm;
create table load2_mm (key string, value string)
  partitioned by (k int, l int) stored as textfile tblproperties("transactional"="true", "transactional_properties"="insert_only");
load data local inpath '../../data/files/kv1.txt' into table intermediate2;
load data local inpath '../../data/files/kv2.txt' into table intermediate2;
load data local inpath '../../data/files/kv3.txt' into table intermediate2;
load data inpath 'file:${system:test.tmp.dir}/intermediate2/kv*.txt' into table load2_mm partition(k=5, l=5);
select count(1) from load2_mm;
drop table load2_mm;
drop table intermediate2;


drop table multi0_1_mm;
drop table multi0_2_mm;
create table multi0_1_mm (key int, key2 int)  tblproperties("transactional"="true", "transactional_properties"="insert_only");
create table multi0_2_mm (key int, key2 int)  tblproperties("transactional"="true", "transactional_properties"="insert_only");


set hive.optimize.index.filter=true;
set hive.auto.convert.join=false;
CREATE TABLE parquet1_mm(id INT) STORED AS PARQUET tblproperties ("transactional"="true", "transactional_properties"="insert_only");
INSERT INTO parquet1_mm VALUES(1), (2);
CREATE TABLE parquet2_mm(id INT, value STRING) STORED AS PARQUET tblproperties ("transactional"="true", "transactional_properties"="insert_only");
INSERT INTO parquet2_mm VALUES(1, 'value1');
INSERT INTO parquet2_mm VALUES(1, 'value2');
select parquet1_mm.id, t1.value, t2.value FROM parquet1_mm
  JOIN parquet2_mm t1 ON parquet1_mm.id=t1.id
  JOIN parquet2_mm t2 ON parquet1_mm.id=t2.id
where t1.value = 'value1' and t2.value = 'value2';
drop table parquet1_mm;
drop table parquet2_mm;

set hive.auto.convert.join=true;

drop table intermediate;



