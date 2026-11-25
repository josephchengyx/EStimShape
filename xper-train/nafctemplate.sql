USE *;

CREATE TABLE NAFCPairLibrary (
  tstamp bigint(20) NOT NULL default '0',
  sample_path longtext NOT NULL,
  match_path longtext NOT NULL,
  PRIMARY KEY  (tstamp)
) ENGINE=MyISAM;

CREATE TABLE NAFCStimLibrary (
  tstamp bigint(20) NOT NULL default '0',
  stim_path longtext NOT NULL,
  PRIMARY KEY  (tstamp)
) ENGINE=MyISAM;

CREATE TABLE NAFCPairParams (
  tstamp bigint(20) NOT NULL default '0',
  xml longtext NOT NULL,
  PRIMARY KEY (tstamp)
) ENGINE=MyISAM COMMENT='Generation parameters per NAFC pair trial session.';