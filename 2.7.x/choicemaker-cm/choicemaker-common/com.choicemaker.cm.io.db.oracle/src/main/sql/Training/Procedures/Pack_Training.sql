CREATE OR REPLACE PACKAGE CMTTraining AS
--*******************************************************
-- Created on: 10/06/02
-- by T.Malyuta
-- Revision: Revision: 1.1.2.5
-- Last Modified: Date: 2013/08/22 11:15:00
-- by rphall
--*******************************************************

	TYPE tpCursor		IS	REF CURSOR;

	PROCEDURE Access_Snapshot (p_sSelection			VARCHAR2,
					p_sConfig_Rd			VARCHAR2,
					p_tpCursor_Pairs	IN OUT 	tpCursor,
					p_tpCursor	IN OUT 		tpCursor );
					
	PROCEDURE Rs_Snapshot (p_sSelection		VARCHAR2,
					p_sConfig_Rd			VARCHAR2,
					p_tpCursor		IN OUT	tpCursor);
					
END;
/

