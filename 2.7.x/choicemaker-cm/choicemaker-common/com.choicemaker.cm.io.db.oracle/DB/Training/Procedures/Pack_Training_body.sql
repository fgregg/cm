CREATE OR REPLACE PACKAGE BODY CMTTraining AS
--*******************************************************
-- Created on: 10/06/02
-- by T.Malyuta
-- Revision: Revision: 1.1.2.6
-- Last Modified: Date: 2013/08/22 11:15:00
-- by rphall
--*******************************************************

	TYPE tpConfig		IS				-- type for data from TB_CMT_CONFIG
					RECORD (
						sConfig 		tb_cmt_config.Config%TYPE,
						sName			tb_cmt_config.Name%TYPE,
						sValue		tb_cmt_config.Value%TYPE);
	TYPE tpConfigs		IS 	TABLE OF tpConfig;

	TYPE tpQuery_Cursor	IS				-- type for queries for cursors for Matching
					RECORD (
						sConfig 		tb_cmt_cursors.Config%TYPE,
						sQueryMulti		VARCHAR2(4000),
						sQuerySingle	VARCHAR2(4000),
						sOrderBy		tb_cmt_cursors.OrderBy%TYPE,
						sApproach		CHAR(1) );
	TYPE tpQuery_Cursors	IS	TABLE OF tpQuery_Cursor;

	tConfig			tpConfigs;
	tQuery_Cursors		tpQuery_Cursors;

	CURSOR cConfig IS SELECT * FROM tb_cmt_config WHERE Name = 'MASTERID' ORDER BY Config;
	CURSOR cCursors IS SELECT * FROM tb_cmt_cursors ORDER BY Config;

	nNumb_Rows			INTEGER;

----------------------------------------------------------
PROCEDURE Access_Snapshot (p_sSelection				VARCHAR2,
					p_sConfig_Rd			VARCHAR2,
					p_tpCursor_Pairs	IN OUT 	tpCursor, 
					p_tpCursor		IN OUT 	tpCursor )
IS
	sSubquery	VARCHAR2(4000);
BEGIN

	sSubquery := 'SELECT ID FROM (' || p_sSelection || ') UNION ' ||
		'SELECT ID_Matched FROM (' || p_sSelection || ')';		-- subquery with criteria for pairs
		
	EXECUTE IMMEDIATE 'INSERT INTO tb_cmt_temp_ids (' || sSubquery ||')';

	FOR i IN 1 .. tQuery_Cursors.COUNT LOOP
		IF tQuery_Cursors(i).sConfig = p_sConfig_Rd THEN
			sSubquery := tQuery_Cursors(i).sQueryMulti;
			EXIT;
		END IF;
	END LOOP;

	OPEN p_tpCursor_Pairs FOR
		'SELECT p.* FROM tb_cmt_pairs p, (' || p_sSelection  || ') i ' ||
			'WHERE p.ID = i.ID AND p.ID_Matched = i.ID_Matched ORDER BY p.ID';
 
	OPEN p_tpCursor FOR sSubquery;

END;

-----------------------------------------------------------------

PROCEDURE Rs_Snapshot (p_sSelection				VARCHAR2,
					   p_sConfig_Rd				VARCHAR2,
					   p_tpCursor		IN OUT	tpCursor)
IS
	sSubquery	VARCHAR2(4000);
BEGIN
	
	EXECUTE IMMEDIATE 'truncate table tb_cmt_temp_ids';
	
	EXECUTE IMMEDIATE 'INSERT INTO tb_cmt_temp_ids (SELECT id FROM (' || p_sSelection || '))';
	
	FOR i IN 1 .. tQuery_Cursors.COUNT LOOP
		IF tQuery_Cursors(i).sConfig = p_sConfig_Rd THEN
			sSubquery := tQuery_Cursors(i).sQueryMulti;
			EXIT;
		END IF;
	END LOOP;
		
	OPEN p_tpCursor FOR sSubquery;
	
END;
	
BEGIN
	-- EXECUTE IMMEDIATE 'alter session set nls_date_format = ''YYYY-MM-DD''';

	nNumb_Rows := 0;
	tConfig := tpConfigs();

	FOR cConfig_rec IN cConfig LOOP
		nNumb_Rows := nNumb_Rows + 1;
		tConfig.EXTEND;
		tConfig(nNumb_Rows).sConfig := cConfig_rec.Config;
		tConfig(nNumb_Rows).sName := cConfig_rec.Name;
		tConfig(nNumb_Rows).sValue := cConfig_rec.Value;
	END LOOP;

	nNumb_Rows := 0;
	tQuery_Cursors := tpQuery_Cursors();

	FOR cCursors_rec IN cCursors LOOP
		nNumb_Rows := nNumb_Rows + 1;
		tQuery_Cursors.EXTEND;
		tQuery_Cursors(nNumb_Rows).sConfig := cCursors_rec.Config;
		tQuery_Cursors(nNumb_Rows).sQueryMulti := cCursors_rec.Multi;
		tQuery_Cursors(nNumb_Rows).sQuerySingle := cCursors_rec.Single;
		tQuery_Cursors(nNumb_Rows).sOrderBy := cCursors_rec.OrderBy;
		tQuery_Cursors(nNumb_Rows).sApproach := cCursors_rec.Approach;

	END LOOP;

END;
/

