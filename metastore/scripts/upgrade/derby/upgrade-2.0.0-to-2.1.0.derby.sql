-- Upgrade MetaStore schema from 2.0.0 to 2.1.0
RUN '033-HIVE-12892.derby.sql';

UPDATE "APP".VERSION SET SCHEMA_VERSION='2.1.0', VERSION_COMMENT='Hive release version 2.1.0' where VER_ID=1;
