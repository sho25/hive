CREATE TABLE IF NOT EXISTS  "KEY_CONSTRAINTS"
(
  "CHILD_CD_ID" BIGINT,
  "CHILD_TBL_ID" BIGINT,
  "PARENT_CD_ID" BIGINT NOT NULL,
  "PARENT_TBL_ID" BIGINT NOT NULL,
  "POSITION" BIGINT NOT NULL,
  "CONSTRAINT_NAME" VARCHAR(400) NOT NULL,
  "CONSTRAINT_TYPE" SMALLINT NOT NULL,
  "UPDATE_RULE" SMALLINT,
  "DELETE_RULE" SMALLINT,
  "ENABLE_VALIDATE_RELY" SMALLINT NOT NULL,
  PRIMARY KEY ("CONSTRAINT_NAME", "POSITION")
) ;
CREATE INDEX "CONSTRAINTS_PARENT_TBLID_INDEX" ON "KEY_CONSTRAINTS" USING BTREE ("PARENT_TBL_ID");
