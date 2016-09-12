ALTER TABLE TBLS ADD MM_WATERMARK_WRITE_ID NUMBER DEFAULT -1;
ALTER TABLE TBLS ADD MM_NEXT_WRITE_ID NUMBER DEFAULT 0;

CREATE TABLE TBL_WRITES
(
  TW_ID NUMBER NOT NULL,
  TBL_ID NUMBER NOT NULL,
  WRITE_ID NUMBER NOT NULL,
  STATE CHAR(1) NOT NULL,
  LAST_HEARTBEAT NUMBER NOT NULL
);
ALTER TABLE TBL_WRITES ADD CONSTRAINT TBL_WRITES_PK PRIMARY KEY (TW_ID);
ALTER TABLE TBL_WRITES ADD CONSTRAINT TBL_WRITES_FK1 FOREIGN KEY (TBL_ID) REFERENCES TBLS (TBL_ID) INITIALLY DEFERRED ;
CREATE UNIQUE INDEX UNIQUEWRITE ON TBL_WRITES (TBL_ID, WRITE_ID);
