set hive.support.concurrency=true;
set hive.txn.manager=org.apache.hadoop.hive.ql.lockmgr.DbTxnManager;
set hive.input.format=org.apache.hadoop.hive.ql.io.HiveInputFormat;
set hive.enforce.bucketing=true;
set hive.exec.reducers.max = 1;

create table acid_danp(a int, b varchar(128)) clustered by (a) into 2 buckets stored as orc;

insert into table acid_danp select cint, cast(cstring1 as varchar(128)) from alltypesorc where cint < 0 order by cint limit 10;

select a,b from acid_danp order by a;

delete from acid_danp;

select a,b from acid_danp;


