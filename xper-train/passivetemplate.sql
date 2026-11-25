USE *;

CREATE TABLE PassivePairLibrary (
  tstamp bigint(20) NOT NULL default '0',
  sample_path longtext NOT NULL,
  match_path longtext NOT NULL,
  PRIMARY KEY  (tstamp)
) ENGINE=MyISAM;

CREATE TABLE PassivePairParams (
  tstamp bigint(20) NOT NULL default '0',
  xml longtext NOT NULL,
  PRIMARY KEY (tstamp)
) ENGINE=MyISAM COMMENT='Generation parameters per passive pair trial session.';