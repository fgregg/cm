--*******************************************************
-- Revision: Revision: 1.1.2.6 
-- Last Modified: Date: 2013/08/22 11:15:00 
-- by rphall
--*******************************************************
CREATE TABLE  TB_CMT_Pairs (
	ID 			NUMBER,
	ID_MATCHED		NUMBER,
	DECISION		CHAR(1),
	LAST_MOD_DATE	DATE ,
	LAST_MOD_USER_ID	VARCHAR2(256) ,
	Pair_SOURCE		VARCHAR2(256),
	Pair_COMMENTS	VARCHAR2(256),
	CONSTRAINT TB_CMT_PAIRS_PK PRIMARY KEY (ID, ID_MATCHED) ENABLE
	) ;

ALTER TABLE TB_CMT_PAIRS
ADD CONSTRAINT TB_CMT_PAIRS_CHK_ID_ORDER CHECK (ID < ID_MATCHED) ENABLE;

COMMENT ON TABLE TB_CMT_PAIRS IS 'Used by CMTTRAINING.Access_Snapshot';

-- Defined in Main/DDL/DDL_Tables.sql
--CREATE TABLE tb_cmt_config (
--	Config		VARCHAR2(256),
--	Name		VARCHAR2(256),
--	Value		VARCHAR2(256),
--	PRIMARY KEY (Config, Name) );

-- Defined in Main/DDL/DDL_Tables.sql
-- CREATE TABLE tb_cmt_cursors (
--	Config	VARCHAR2(256) PRIMARY KEY,
--	Multi		VARCHAR2(4000),
--	Single	VARCHAR2(4000),
--	OrderBy	VARCHAR2(256),
--	Approach	CHAR(1),
--	Numb		NUMBER);

--------------------------------
create global temporary table tb_cmt_temp_ids (id NUMBER);
