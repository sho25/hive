DROP TABLE IF EXISTS DECIMAL_PRECISION;

CREATE TABLE DECIMAL_PRECISION(dec decimal(60,30)) 
ROW FORMAT DELIMITED
   FIELDS TERMINATED BY ' '
STORED AS TEXTFILE;

LOAD DATA LOCAL INPATH '../data/files/kv8.txt' INTO TABLE DECIMAL_PRECISION;

SELECT * FROM DECIMAL_PRECISION ORDER BY dec;

SELECT dec, dec + 1, dec - 1 FROM DECIMAL_PRECISION ORDER BY dec;
SELECT dec, dec * 2, dec / 3  FROM DECIMAL_PRECISION ORDER BY dec;
SELECT dec, dec / 9 FROM DECIMAL_PRECISION ORDER BY dec;
SELECT dec, dec / 27 FROM DECIMAL_PRECISION ORDER BY dec;
SELECT dec, dec * dec FROM DECIMAL_PRECISION ORDER BY dec;

SELECT avg(dec), sum(dec) FROM DECIMAL_PRECISION;

SELECT dec * cast('123456789012345678901234567890.123456789' as decimal(39,9)) FROM DECIMAL_PRECISION LIMIT 1;
SELECT * from DECIMAL_PRECISION WHERE dec > cast('123456789012345678901234567890.123456789' as decimal(39,9)) LIMIT 1;
SELECT dec * 123456789012345678901234567890.123456789 FROM DECIMAL_PRECISION LIMIT 1;

SELECT MIN(cast('123456789012345678901234567890.123456789' as decimal(39,9))) FROM DECIMAL_PRECISION;
SELECT COUNT(cast('123456789012345678901234567890.123456789' as decimal(39,9))) FROM DECIMAL_PRECISION;

DROP TABLE DECIMAL_PRECISION;
