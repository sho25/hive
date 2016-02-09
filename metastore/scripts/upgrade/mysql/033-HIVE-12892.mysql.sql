-- Table structure for CHANGE_VERSION
--
CREATE TABLE IF NOT EXISTS `CHANGE_VERSION` (
  `CHANGE_VERSION_ID` BIGINT NOT NULL,
  `VERSION` BIGINT NOT NULL,
  `TOPIC` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`CHANGE_VERSION_ID`),
  UNIQUE KEY `UNIQUECHANGEVERSION` (`TOPIC`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
--
