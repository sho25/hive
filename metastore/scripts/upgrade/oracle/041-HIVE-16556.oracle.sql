CREATE TABLE METASTORE_DB_PROPERTIES
(
  PROPERTY_ID NUMBER NOT NULL,
  PROPERTY_KEY VARCHAR(255) NOT NULL,
  PROPERTY_VALUE VARCHAR(1000) NOT NULL,
  DESCRIPTION VARCHAR(1000)
);

ALTER TABLE METASTORE_DB_PROPERTIES ADD CONSTRAINT UNIQUE_PROPERTY_KEY UNIQUE (PROPERTY_KEY);

ALTER TABLE METASTORE_DB_PROPERTIES ADD CONSTRAINT PROPERTY_ID_PK PRIMARY KEY (PROPERTY_ID);
