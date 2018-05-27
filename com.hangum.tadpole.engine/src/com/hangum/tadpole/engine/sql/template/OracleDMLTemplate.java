/*******************************************************************************
 * Copyright (c) 2013 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.hangum.tadpole.engine.sql.template;

import com.tadpole.common.define.core.define.PublicTadpoleDefine;

/**
 * Oracle DB의 디비를 정의합니다.
 * 
 * @author hangum
 *
 */
public class OracleDMLTemplate extends MySQLDMLTemplate {
	
	/* 참조 : http://devhome.tistory.com/22 */
	public static final String TMP_GET_PARTDATA = "SELECT * FROM (	" +
												 "    SELECT tdb__a.*, ROWNUM AS " + TDB_CUSTOME_COLUMN +
												 "    FROM ( " + PublicTadpoleDefine.LINE_SEPARATOR + "%s" + PublicTadpoleDefine.LINE_SEPARATOR + " ) tdb__a " +
												 " 	) WHERE " + TDB_CUSTOME_COLUMN + " > %s AND " + TDB_CUSTOME_COLUMN + " <= %s";
	
	/** table - oracle */
	public static final String TMP_CREATE_TABLE_STMT = "CREATE TABLE {#schema#}.sample_table ( " + PublicTadpoleDefine.LINE_SEPARATOR + 
						 "	id number primary key, " + PublicTadpoleDefine.LINE_SEPARATOR +
						 "	name varchar2(30) " + PublicTadpoleDefine.LINE_SEPARATOR +
						");";

	/** constraints  */
	public static final String  TMP_CREATE_CONSTRAINTS_STMT = "ALTER TABLE {#schema#}.table_name "+ PublicTadpoleDefine.LINE_SEPARATOR
			+ " ADD CONSTRAINT constraint_name UNIQUE (column1, column2, ... column_n); "+ PublicTadpoleDefine.LINE_SEPARATOR;
	
	// plan_table	
	public static final String TMP_EXPLAIN_EXTENDED = "EXPLAIN PLAN SET statement_id = '" + PublicTadpoleDefine.STATEMENT_ID + "' INTO " + PublicTadpoleDefine.DELIMITER + " FOR ";

	/** procedure */
	public static final String  TMP_CREATE_PROCEDURE_STMT = "CREATE OR REPLACE PROCEDURE {#schema#}.simpleproc2 (param1 out INT) " + PublicTadpoleDefine.LINE_SEPARATOR +
														  "IS " + PublicTadpoleDefine.LINE_SEPARATOR +
														  "BEGIN " + PublicTadpoleDefine.LINE_SEPARATOR +
														  "	SELECT COUNT(*) INTO param1 FROM {#schema#}.sample_table; " + PublicTadpoleDefine.LINE_SEPARATOR +
														  "END;";
	/** function */
	public static final String TMP_CREATE_FUNCTION_STMT = "CREATE OR REPLACE FUNCTION {#schema#}.hello (param1 VARCHAR2) RETURN VARCHAR2 AS " + PublicTadpoleDefine.LINE_SEPARATOR + 
														  "BEGIN " + 
														  "RETURN CONCAT('Hello', param1);" + PublicTadpoleDefine.LINE_SEPARATOR + 
														  "END; ";

	/** sysnonym */
	public static final String TMP_CREATE_SYNONYM_STMT = "CREATE SYNONYM {#schema#}.sn_sample FOR {#schema#}.sample_table;";

	/** sequence */
	public static final String TMP_CREATE_SEQUENCE_STMT = "CREATE SEQUENCE {#schema#}.seq_sample START WITH 1;";

	/** database link */
	public static final String TMP_CREATE_LINK_STMT = "CREATE DATABASE LINK \"TADPOLEHUB_DBLINK\" CONNECT TO {#schema#} IDENTIFIED BY \"<password>\" USING 'XE' ";

	/** package */
	public static final String  TMP_CREATE_PACKAGE_STMT = "CREATE OR REPLACE PACKAGE {#schema#}.PKG_SAMPLE " + PublicTadpoleDefine.LINE_SEPARATOR +
														 " AS " + PublicTadpoleDefine.LINE_SEPARATOR +
														  " FUNCTION F_TEST RETURN VARCHAR2;  " + PublicTadpoleDefine.LINE_SEPARATOR +
														  " PROCEDURE P_TEST (param1 IN VARCHAR2);  " + PublicTadpoleDefine.LINE_SEPARATOR +
														  " END;" + PublicTadpoleDefine.LINE_SEPARATOR +
														  " /" + PublicTadpoleDefine.LINE_SEPARATOR + PublicTadpoleDefine.LINE_SEPARATOR +
														  "CREATE or replace PACKAGE BODY {#schema#}.PKG_SAMPLE " + PublicTadpoleDefine.LINE_SEPARATOR +
														  " AS " + PublicTadpoleDefine.LINE_SEPARATOR +
														  " FUNCTION F_TEST RETURN VARCHAR2  " + PublicTadpoleDefine.LINE_SEPARATOR +
														  " IS  " + PublicTadpoleDefine.LINE_SEPARATOR +
														  " BEGIN  " + PublicTadpoleDefine.LINE_SEPARATOR +
														  "     return to_char(sysdate, 'yyyymmdd');  " + PublicTadpoleDefine.LINE_SEPARATOR +
														  " end;  " + PublicTadpoleDefine.LINE_SEPARATOR + PublicTadpoleDefine.LINE_SEPARATOR +
														  " PROCEDURE P_TEST (param1 IN VARCHAR2)  " + PublicTadpoleDefine.LINE_SEPARATOR +
														  " IS  " + PublicTadpoleDefine.LINE_SEPARATOR +
														  " BEGIN  " + PublicTadpoleDefine.LINE_SEPARATOR +
														  "		dbms_output.put_line(param1); " + PublicTadpoleDefine.LINE_SEPARATOR +
														  " END;" + PublicTadpoleDefine.LINE_SEPARATOR +
														  "END;";

	/** trigger */
	public static final String TMP_CREATE_TRIGGER_STMT = "CREATE TRIGGER {#schema#}.testref BEFORE INSERT ON {#schema#}.test1  " + PublicTadpoleDefine.LINE_SEPARATOR +
													"FOR EACH ROW BEGIN   " + PublicTadpoleDefine.LINE_SEPARATOR +
													"	INSERT INTO {#schema#}.test2 SET a2 = :NEW.a1;   " + PublicTadpoleDefine.LINE_SEPARATOR +
													"	DELETE FROM {#schema#}.test3 WHERE a3 = :NEW.a1;   " + PublicTadpoleDefine.LINE_SEPARATOR +
													"	UPDATE {#schema#}.test4 SET b4 = b4 + 1 WHERE a4 = :NEW.a1;   " + PublicTadpoleDefine.LINE_SEPARATOR +
													"END;";

}
