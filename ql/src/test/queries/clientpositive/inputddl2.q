CREATE TABLE INPUTDDL2(key INT, value STRING) PARTITIONED BY(ds DATETIME, country STRING);
DESCRIBE INPUTDDL2;
DROP TABLE INPUTDDL2;

