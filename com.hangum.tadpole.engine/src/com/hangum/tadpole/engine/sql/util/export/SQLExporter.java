/*******************************************************************************
 * Copyright (c) 2015 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.hangum.tadpole.engine.sql.util.export;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.hangum.tadpole.engine.sql.util.RDBTypeToJavaTypeUtils;
import com.hangum.tadpole.engine.sql.util.SQLUtil;
import com.hangum.tadpole.engine.sql.util.resultset.QueryExecuteResultDTO;
import com.tadpole.common.define.core.define.PublicTadpoleDefine;

/**
 * SQL exporter
 * 
 * @author hangum
 *
 */
public class SQLExporter extends AbstractTDBExporter {

	/**
	 * make value object
	 * 
	 * @param obVal
	 * @param intColumnType
	 * @return
	 */
	private static Object _makeValueObject(Object obVal, int intColumnType) {
		if(obVal == null) return "null";
		
		Object strValue = obVal;
		if(!RDBTypeToJavaTypeUtils.isNumberType(intColumnType)) {
			String strStrValue = strValue.toString();
			strValue = StringEscapeUtils.escapeSql(strStrValue);
			strValue = SQLUtil.makeQuote(strStrValue);
		}
		
		return strValue;
	}

	/**
	 * MERGE 문을 생성합니다. (대상 자료가 있으면 update, 없으면 insert할 수 있습니다. 오라클, MSSQL등에서 지원됩니다.)
	 * 
	 * @param tableName
	 * @param rsDAO
	 * @param listWhere  where 조건
	 * @param intLimitCnt
	 * @param commit
	 * 
	 * @throws Exception
	 */
	public static String makeMergeStatment(String tableName, QueryExecuteResultDTO rsDAO, List<String> listWhere, int intLimitCnt, int commit) throws Exception {
		if(rsDAO.getDataList() == null) return "";
		
		final String MERGE_STMT = "MERGE INTO " + tableName + " A USING (\n SELECT %s FROM DUAL) B \n ON ( %s ) \n WHEN NOT MATCHED THEN \n INSERT ( %s ) \n VALUES ( %s ) \n WHEN MATCHED THEN \n UPDATE SET %s ;" + PublicTadpoleDefine.LINE_SEPARATOR; 		
		Map<Integer, String> mapColumnName = rsDAO.getColumnLabelName();
		
		// 데이터를 담는다.
		StringBuffer sbInsertInto = new StringBuffer();
		List<Map<Integer, Object>> dataList = rsDAO.getDataList().getData();
		
		Map<Integer, Integer> mapColumnType = rsDAO.getColumnType();
		String strSource = "";
		String strInsertColumn = "";
		String strInsertValue = "";
		String strUpdate = "";
		String strMatchConditon = "";
		for(int i=0; i<dataList.size(); i++) {
			Map<Integer, Object> mapColumns = dataList.get(i);
			
			strSource = "";
			strInsertColumn = "";
			strInsertValue = "";
			strUpdate = "";
			strMatchConditon = "";
			for(int j=0; j<mapColumnName.size(); j++) {
				String strColumnName = mapColumnName.get(j);
				
				Object strValue = _makeValueObject(mapColumns.get(j), mapColumnType.get(j));
//				strValue = strValue == null?"null":strValue;
//				
//				if(!RDBTypeToJavaTypeUtils.isNumberType(mapColumnType.get(j))) {
//					strValue = StringEscapeUtils.escapeSql(strValue.toString());
//					strValue = StringHelper.escapeSQL(strValue.toString());
//					strValue = SQLUtil.makeQuote(strValue.toString());
//				}
				
				boolean isWhere = false;
				for (String strTmpColumn : listWhere) {
					if(strColumnName.equals(strTmpColumn)) {
						isWhere = true;
						break;
					}
				}
				
				// tdb 내부적으로 사용하는 컬럼을 보이지 않도록 합니다.
				if(!SQLUtil.isTDBSpecialColumn(strColumnName)) {
					strSource += String.format("%s as %s ,", strValue, strColumnName);
					strInsertColumn += String.format(" %s,", strColumnName);
					strInsertValue += String.format(" B.%s,", strColumnName);
					
					if(isWhere) strMatchConditon += String.format(" A.%s = B.%s and", strColumnName, strColumnName);
					else strUpdate += String.format(" A.%s = B.%s,", strColumnName, strColumnName);
				}
			}
			strSource = StringUtils.removeEnd(strSource, ",");
			strInsertColumn = StringUtils.removeEnd(strInsertColumn, ",");
			strInsertValue = StringUtils.removeEnd(strInsertValue, ",");
			strUpdate = StringUtils.removeEnd(strUpdate, ",");
			strMatchConditon = StringUtils.removeEnd(strMatchConditon, "and");
			
			sbInsertInto.append(String.format(MERGE_STMT, strSource, strMatchConditon, strInsertColumn, strInsertValue, strUpdate));
			
			if (intLimitCnt == i) break;
			
			if(commit > 0 && (i%commit) == 0) {
				sbInsertInto.append("COMMIT" + PublicTadpoleDefine.SQL_DELIMITER + PublicTadpoleDefine.LINE_SEPARATOR);
			} 
		}
		
		return sbInsertInto.toString();
	}
	
	/**
	 * 
	 * @param strFullPath
	 * @param tableName
	 * @param rsDAO
	 * @param listWhere
	 * @param commit
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public static String makeFileMergeStatment(String strFullPath, String tableName, QueryExecuteResultDTO rsDAO, List<String> listWhere, int commit, String encoding) throws Exception {
		if(rsDAO.getDataList() == null) return "";
		final String MERGE_STMT = "MERGE INTO " + tableName + " A USING (\n SELECT %s FROM DUAL) B \n ON ( %s ) \n WHEN NOT MATCHED THEN \n INSERT ( %s ) \n VALUES ( %s ) \n WHEN MATCHED THEN \n UPDATE SET %s ;" + PublicTadpoleDefine.LINE_SEPARATOR; 		
		Map<Integer, String> mapColumnName = rsDAO.getColumnLabelName();
		
		// 데이터를 담는다.
		StringBuffer sbInsertInto = new StringBuffer();
		int DATA_COUNT = 1000;
		List<Map<Integer, Object>> dataList = rsDAO.getDataList().getData();
		Map<Integer, Integer> mapColumnType = rsDAO.getColumnType();
		String strSource = "";
		String strInsertColumn = "";
		String strInsertValue = "";
		String strUpdate = "";
		String strMatchConditon = "";
		for(int i=0; i<dataList.size(); i++) {
			Map<Integer, Object> mapColumns = dataList.get(i);
			
			strSource = "";
			strInsertColumn = "";
			strInsertValue = "";
			strUpdate = "";
			strMatchConditon = "";
			for(int j=0; j<mapColumnName.size(); j++) {
				String strColumnName = mapColumnName.get(j);
				
//				Object strValue = mapColumns.get(j);
//				strValue = strValue == null?"null":strValue;
//				
//				if(!RDBTypeToJavaTypeUtils.isNumberType(mapColumnType.get(j))) {
//					strValue = StringEscapeUtils.escapeSql(strValue.toString());
//					strValue = StringHelper.escapeSQL(strValue.toString());
//					strValue = SQLUtil.makeQuote(strValue.toString());
//				}
				Object strValue = _makeValueObject(mapColumns.get(j), mapColumnType.get(j));
				
				boolean isWhere = false;
				for (String strTmpColumn : listWhere) {
					if(strColumnName.equals(strTmpColumn)) {
						isWhere = true;
						break;
					}
				}
				
				// tdb 내부적으로 사용하는 컬럼을 보이지 않도록 합니다.
				if(!SQLUtil.isTDBSpecialColumn(mapColumnName.get(j))) {
					strSource += String.format("%s as %s ,", strValue, strColumnName);
					strInsertColumn += String.format(" %s,", strColumnName);
					strInsertValue += String.format(" B.%s,", strColumnName);
					
					if(isWhere) strMatchConditon += String.format(" A.%s = B.%s and", strColumnName, strColumnName);
					else strUpdate += String.format(" A.%s = B.%s,", strColumnName, strColumnName);
				}
			}
			strSource = StringUtils.removeEnd(strSource, ",");
			strInsertColumn = StringUtils.removeEnd(strInsertColumn, ",");
			strInsertValue = StringUtils.removeEnd(strInsertValue, ",");
			strUpdate = StringUtils.removeEnd(strUpdate, ",");
			strMatchConditon = StringUtils.removeEnd(strMatchConditon, "and");
			
			sbInsertInto.append(String.format(MERGE_STMT, strSource, strMatchConditon, strInsertColumn, strInsertValue, strUpdate));
			
			if(commit > 0 && (i%commit) == 0) {
				sbInsertInto.append("COMMIT" + PublicTadpoleDefine.SQL_DELIMITER + PublicTadpoleDefine.LINE_SEPARATOR);
			} 

			if((i%DATA_COUNT) == 0) {
				FileUtils.writeStringToFile(new File(strFullPath), sbInsertInto.toString(), encoding, true);
				sbInsertInto.setLength(0);
			}
		}
		if(sbInsertInto.length() > 0) {
			if(commit > 0) {
				sbInsertInto.append("COMMIT" + PublicTadpoleDefine.SQL_DELIMITER + PublicTadpoleDefine.LINE_SEPARATOR);
			} 

			FileUtils.writeStringToFile(new File(strFullPath), sbInsertInto.toString(), encoding, true);
		}
		
		return strFullPath;
	}
	
	/**
	 * UPDATE 문을 생성합니다.
	 * 
	 * @param tableName
	 * @param rsDAO
	 * @param listWhere  where 조건
	 * @param intLimitCnt
	 * @param commit
	 * 
	 * @throws Exception
	 */
	public static String makeUpdateStatment(String tableName, QueryExecuteResultDTO rsDAO, List<String> listWhere, int intLimitCnt, int commit) throws Exception {
		if(rsDAO.getDataList() == null) return "";
		
		final String UPDATE_STMT = "UPDATE " + tableName + " SET %s WHERE 1=1 and %s;" + PublicTadpoleDefine.LINE_SEPARATOR; 		
		Map<Integer, String> mapColumnName = rsDAO.getColumnLabelName();
		
		// 데이터를 담는다.
		StringBuffer sbInsertInto = new StringBuffer();
		List<Map<Integer, Object>> dataList = rsDAO.getDataList().getData();
		Map<Integer, Integer> mapColumnType = rsDAO.getColumnType();
		String strStatement = "";
		String strWhere = "";
		for(int i=0; i<dataList.size(); i++) {
			Map<Integer, Object> mapColumns = dataList.get(i);
			
			strStatement = "";
			strWhere = "";
			for(int j=0; j<mapColumnName.size(); j++) {
				String strColumnName = mapColumnName.get(j);
				
//				Object strValue = mapColumns.get(j);
//				strValue = strValue == null?"null":strValue;
//				if(!RDBTypeToJavaTypeUtils.isNumberType(mapColumnType.get(j))) {
//					strValue = StringEscapeUtils.escapeSql(strValue.toString());
//					strValue = StringHelper.escapeSQL(strValue.toString());
//					strValue = SQLUtil.makeQuote(strValue.toString());
//				}
				Object strValue = _makeValueObject(mapColumns.get(j), mapColumnType.get(j));
				
				boolean isWhere = false;
				for (String strTmpColumn : listWhere) {
					if(strColumnName.equals(strTmpColumn)) {
						isWhere = true;
						break;
					}
				}
				
				// tdb 내부적으로 사용하는 컬럼을 보이지 않도록 합니다.
				if(!SQLUtil.isTDBSpecialColumn(mapColumnName.get(j))) {
					if(isWhere) strWhere += String.format(" %s=%s and", strColumnName, strValue);
					else strStatement += String.format(" %s=%s,", strColumnName, strValue);
				}
			}
			strStatement = StringUtils.removeEnd(strStatement, ",");
			strWhere = StringUtils.removeEnd(strWhere, "and");
			sbInsertInto.append(String.format(UPDATE_STMT, strStatement, strWhere));
			
			if (intLimitCnt == i) break;

			if(commit > 0 && (i%commit) == 0) {
				sbInsertInto.append("COMMIT" + PublicTadpoleDefine.SQL_DELIMITER + PublicTadpoleDefine.LINE_SEPARATOR);
			} 
		}
		
		return sbInsertInto.toString();
	}

	public static String makeFileUpdateStatment(String strFullPath, String tableName, QueryExecuteResultDTO rsDAO, List<String> listWhere, int commit, String encoding) throws Exception {
		if(rsDAO.getDataList() == null) return "";
		
		final String UPDATE_STMT = "UPDATE " + tableName + " SET %s WHERE 1=1 and %s;" + PublicTadpoleDefine.LINE_SEPARATOR; 		
		Map<Integer, String> mapColumnName = rsDAO.getColumnLabelName();
		
		// 데이터를 담는다.
		StringBuffer sbInsertInto = new StringBuffer();
		int DATA_COUNT = 1000;
		List<Map<Integer, Object>> dataList = rsDAO.getDataList().getData();
		Map<Integer, Integer> mapColumnType = rsDAO.getColumnType();
		String strStatement = "";
		String strWhere = "";
		for(int i=0; i<dataList.size(); i++) {
			Map<Integer, Object> mapColumns = dataList.get(i);
			
			strStatement = "";
			strWhere = "";
			for(int j=0; j<mapColumnName.size(); j++) {
				String strColumnName = mapColumnName.get(j);
				
//				Object strValue = mapColumns.get(j);
//				strValue = strValue == null?"null":strValue;
//				if(!RDBTypeToJavaTypeUtils.isNumberType(mapColumnType.get(j))) {
//					strValue = StringEscapeUtils.escapeSql(strValue.toString());
//					strValue = StringHelper.escapeSQL(strValue.toString());
//					strValue = SQLUtil.makeQuote(strValue.toString());
//				}
				Object strValue = _makeValueObject(mapColumns.get(j), mapColumnType.get(j));
				
				boolean isWhere = false;
				for (String strTmpColumn : listWhere) {
					if(strColumnName.equals(strTmpColumn)) {
						isWhere = true;
						break;
					}
				}
				// tdb 내부적으로 사용하는 컬럼을 보이지 않도록 합니다.
				if(!SQLUtil.isTDBSpecialColumn(mapColumnName.get(j))) {
					if(isWhere) strWhere += String.format(" %s=%s and", strColumnName, strValue);
					else strStatement += String.format(" %s=%s,", strColumnName, strValue);
				}
			}
			strStatement = StringUtils.removeEnd(strStatement, ",");
			strWhere = StringUtils.removeEnd(strWhere, "and");
			sbInsertInto.append(String.format(UPDATE_STMT, strStatement, strWhere));

			if(commit > 0 && (i%commit) == 0) {
				sbInsertInto.append("COMMIT" + PublicTadpoleDefine.SQL_DELIMITER + PublicTadpoleDefine.LINE_SEPARATOR);
			} 

			if((i%DATA_COUNT) == 0) {
				FileUtils.writeStringToFile(new File(strFullPath), sbInsertInto.toString(), encoding, true);
				sbInsertInto.setLength(0);
			}
		}
		if(sbInsertInto.length() > 0) {
			if(commit > 0) {
				sbInsertInto.append("COMMIT" + PublicTadpoleDefine.SQL_DELIMITER + PublicTadpoleDefine.LINE_SEPARATOR);
			} 

			FileUtils.writeStringToFile(new File(strFullPath), sbInsertInto.toString(), encoding, true);
		}
		
		return strFullPath;
	}
	
	/**
	 * INSERT 문을 생성합니다.
	 * 
	 * @param tableName
	 * @param rs
	 * @return 파일 위치
	 * 
	 * @throws Exception
	 */
	public static String makeInsertStatment(String tableName, QueryExecuteResultDTO rsDAO, int intLimitCnt, int commit) throws Exception {
		if(rsDAO.getDataList() == null) return "";
		
		final String INSERT_INTO_STMT = "INSERT INTO " + tableName + " (%s) VALUES (%S);" + PublicTadpoleDefine.LINE_SEPARATOR; 
		
		// 컬럼 이름.
		String strColumns = "";
		Map<Integer, String> mapLabelName = rsDAO.getColumnLabelName();
		for( int i=0 ;i<mapLabelName.size(); i++ ) {
			// tdb 내부적으로 사용하는 컬럼을 보이지 않도록 합니다.
			if(!SQLUtil.isTDBSpecialColumn(mapLabelName.get(i))) {
				if(i != (mapLabelName.size()-1)) strColumns += mapLabelName.get(i) + ",";
				else strColumns += mapLabelName.get(i);
			}
		}
		
		// 데이터를 담는다.
		StringBuffer sbInsertInto = new StringBuffer();
		List<Map<Integer, Object>> dataList = rsDAO.getDataList().getData();
		Map<Integer, Integer> mapColumnType = rsDAO.getColumnType();
		String strResult = new String();		
		for(int i=0; i<dataList.size(); i++) {
			Map<Integer, Object> mapColumns = dataList.get(i);
			
			strResult = "";
			for(int j=0; j<mapColumnType.size(); j++) {
//				Object strValue = mapColumns.get(j);
//				strValue = strValue == null?"null":strValue;
//				if(!RDBTypeToJavaTypeUtils.isNumberType(mapColumnType.get(j))) {
//					strValue = StringEscapeUtils.escapeSql(strValue.toString());
//					strValue = StringHelper.escapeSQL(strValue.toString());
//					
//					strValue = SQLUtil.makeQuote(strValue.toString());
//				}
				Object strValue = _makeValueObject(mapColumns.get(j), mapColumnType.get(j));
				
				// tdb 내부적으로 사용하는 컬럼을 보이지 않도록 합니다.
				if(!SQLUtil.isTDBSpecialColumn(mapLabelName.get(j))) {
					if(j != (mapLabelName.size()-1)) strResult += strValue + ",";
					else strResult += strValue;
				}
			}
			
			strColumns = StringUtils.removeEnd(strColumns, ",");
			strResult = StringUtils.removeEnd(strResult, ",");
			sbInsertInto.append(String.format(INSERT_INTO_STMT, strColumns, strResult));

			if(commit > 0 && (i%commit) == 0) {
				sbInsertInto.append("COMMIT" + PublicTadpoleDefine.SQL_DELIMITER + PublicTadpoleDefine.LINE_SEPARATOR);
			} 
			
			if(i == intLimitCnt) break;
		}
		
		return sbInsertInto.toString();
	}
	
	/**
	 * 
	 * 
	 * @param strFullPath
	 * @param tableName
	 * @param rsDAO
	 * @param commit
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public static String makeFileInsertStatment(String strFullPath, String tableName, QueryExecuteResultDTO rsDAO, int commit, String encoding) throws Exception {
		if(rsDAO.getDataList() == null) return "";
		
		final String INSERT_INTO_STMT = "INSERT INTO " + tableName + " (%s) VALUES (%S);" + PublicTadpoleDefine.LINE_SEPARATOR; 
		
		// 컬럼 이름.
		String strColumns = "";
		Map<Integer, String> mapLabelName = rsDAO.getColumnLabelName();
		for( int i=0 ;i<mapLabelName.size(); i++ ) {
			// tdb 내부적으로 사용하는 컬럼을 보이지 않도록 합니다.
			if(!SQLUtil.isTDBSpecialColumn(mapLabelName.get(i))) {
				if(i != (mapLabelName.size()-1)) strColumns += mapLabelName.get(i) + ",";
				else strColumns += mapLabelName.get(i);
			}
		}
		
		// 데이터를 담는다.
		StringBuffer sbInsertInto = new StringBuffer();
		int DATA_COUNT = 1000;
		List<Map<Integer, Object>> dataList = rsDAO.getDataList().getData();
		Map<Integer, Integer> mapColumnType = rsDAO.getColumnType();
		String strResult = new String();		
		for(int i=0; i<dataList.size(); i++) {
			Map<Integer, Object> mapColumns = dataList.get(i);
			
			strResult = "";
			for(int j=0; j<mapColumnType.size(); j++) {
//				Object strValue = mapColumns.get(j);
//				strValue = strValue == null?"null":strValue;
//				if(!RDBTypeToJavaTypeUtils.isNumberType(mapColumnType.get(j))) {
//					strValue = StringEscapeUtils.escapeSql(strValue.toString());
//					strValue = StringHelper.escapeSQL(strValue.toString());
//					
//					strValue = SQLUtil.makeQuote(strValue.toString());
//				}
				Object strValue = _makeValueObject(mapColumns.get(j), mapColumnType.get(j));
				
				// tdb 내부적으로 사용하는 컬럼을 보이지 않도록 합니다.
				if(!SQLUtil.isTDBSpecialColumn(mapLabelName.get(j))) {
					if(j != (mapLabelName.size()-1)) strResult += strValue + ",";
					else strResult += strValue;
				}
			}
			strColumns = StringUtils.removeEnd(strColumns, ",");
			strResult = StringUtils.removeEnd(strResult, ",");
			sbInsertInto.append(String.format(INSERT_INTO_STMT, strColumns, strResult));

			if(commit > 0 && (i%commit) == 0) {
				sbInsertInto.append("COMMIT" + PublicTadpoleDefine.SQL_DELIMITER + PublicTadpoleDefine.LINE_SEPARATOR);
			} 

			if((i%DATA_COUNT) == 0) {
				FileUtils.writeStringToFile(new File(strFullPath), sbInsertInto.toString(), encoding, true);
				sbInsertInto.setLength(0);
			}
		}
		if(sbInsertInto.length() > 0) {
			if(commit > 0) {
				sbInsertInto.append("COMMIT" + PublicTadpoleDefine.SQL_DELIMITER + PublicTadpoleDefine.LINE_SEPARATOR);
			} 

			FileUtils.writeStringToFile(new File(strFullPath), sbInsertInto.toString(), encoding, true);
		}
		
		return strFullPath;
	}
	
	/**
	 * 배치 INSERT 문을 생성합니다. (SQLite, MySQL등의 DBMS에서 지원합니다.)
	 * 
	 * @param tableName
	 * @param rs
	 * @return 파일내용
	 * 
	 * @throws Exception
	 */
	public static String makeBatchInsertStatment(String tableName, QueryExecuteResultDTO rsDAO, int intLimitCnt, int commit) throws Exception {
		if(rsDAO.getDataList() == null) return "";
		
		boolean isFirst = true;
		
		final String INSERT_INTO_STMT = "INSERT INTO " + tableName + " (%s) VALUES (%S)" ;
		final String NEXT_INSERT_INTO_STMT = ", (%S)" ;
		
		// 컬럼 이름.
		String strColumns = "";
		Map<Integer, String> mapLabelName = rsDAO.getColumnLabelName();
		for( int i=0 ;i<mapLabelName.size(); i++ ) {
			// tdb 내부적으로 사용하는 컬럼을 보이지 않도록 합니다.
			if(!SQLUtil.isTDBSpecialColumn(mapLabelName.get(i))) {
				if(i != (mapLabelName.size()-1)) strColumns += mapLabelName.get(i) + ",";
				else strColumns += mapLabelName.get(i);
			}
		}
		
		// 데이터를 담는다.
		StringBuffer sbInsertInto = new StringBuffer();
		int DATA_COUNT = 1000;
		List<Map<Integer, Object>> dataList = rsDAO.getDataList().getData();
		Map<Integer, Integer> mapColumnType = rsDAO.getColumnType();
		String strResult = new String();		
		for(int i=0; i<dataList.size(); i++) {
			Map<Integer, Object> mapColumns = dataList.get(i);
			
			strResult = "";
			for(int j=0; j<mapColumnType.size(); j++) {
//				Object strValue = mapColumns.get(j);
//				strValue = strValue == null?"null":strValue;
//				if(!RDBTypeToJavaTypeUtils.isNumberType(mapColumnType.get(j))) {
//					strValue = StringEscapeUtils.escapeSql(strValue.toString());
//					strValue = StringHelper.escapeSQL(strValue.toString());
//					
//					strValue = SQLUtil.makeQuote(strValue.toString());
//				}
				Object strValue = _makeValueObject(mapColumns.get(j), mapColumnType.get(j));
				
				// tdb 내부적으로 사용하는 컬럼을 보이지 않도록 합니다.
				if(!SQLUtil.isTDBSpecialColumn(mapLabelName.get(j))) {
					if(j != (mapLabelName.size()-1)) strResult += strValue + ",";
					else strResult += strValue;
				}
			}
			
			strColumns = StringUtils.removeEnd(strColumns, ",");
			strResult = StringUtils.removeEnd(strResult, ",");
			
			if (isFirst) {
				isFirst = false;
				sbInsertInto.append(String.format(INSERT_INTO_STMT, strColumns, strResult));
			}else{
				sbInsertInto.append(String.format(NEXT_INSERT_INTO_STMT, strResult));
			}
			
			if( dataList.size() > 1 && i > 1 && (i%DATA_COUNT) == 0) {
				isFirst = true;
				sbInsertInto.append(PublicTadpoleDefine.SQL_DELIMITER); 
				sbInsertInto.append(PublicTadpoleDefine.LINE_SEPARATOR); 

				if(commit > 0) {
					sbInsertInto.append("COMMIT" + PublicTadpoleDefine.SQL_DELIMITER + PublicTadpoleDefine.LINE_SEPARATOR);
				} 

				if(i == intLimitCnt) break;
			}
		}
		
		return sbInsertInto.toString();
	}
	
	public static String makeFileBatchInsertStatment(String strFullPath, String tableName, QueryExecuteResultDTO rsDAO, int commit, String encoding) throws Exception {
		if(rsDAO.getDataList() == null) return "";
		boolean isFirst = true;
		
		final String INSERT_INTO_STMT = "INSERT INTO " + tableName + " (%s) VALUES (%S)" ;
		final String NEXT_INSERT_INTO_STMT = ", (%S)" ;
		
		// 컬럼 이름.
		String strColumns = "";
		Map<Integer, String> mapLabelName = rsDAO.getColumnLabelName();
		for( int i=0 ;i<mapLabelName.size(); i++ ) {
			// tdb 내부적으로 사용하는 컬럼을 보이지 않도록 합니다.
			if(!SQLUtil.isTDBSpecialColumn(mapLabelName.get(i))) {
				if(i != (mapLabelName.size()-1)) strColumns += mapLabelName.get(i) + ",";
				else strColumns += mapLabelName.get(i);
			}
		}
		
		// 데이터를 담는다.
		StringBuffer sbInsertInto = new StringBuffer();
		int DATA_COUNT = 1000;
		List<Map<Integer, Object>> dataList = rsDAO.getDataList().getData();
		Map<Integer, Integer> mapColumnType = rsDAO.getColumnType();
		String strResult = new String();		
		for(int i=0; i<dataList.size(); i++) {
			Map<Integer, Object> mapColumns = dataList.get(i);
			
			strResult = "";
			for(int j=0; j<mapColumnType.size(); j++) {
//				Object strValue = mapColumns.get(j);
//				strValue = strValue == null?"null":strValue;
//				if(!RDBTypeToJavaTypeUtils.isNumberType(mapColumnType.get(j))) {
//					strValue = StringEscapeUtils.escapeSql(strValue.toString());
//					strValue = StringHelper.escapeSQL(strValue.toString());
//					
//					strValue = SQLUtil.makeQuote(strValue.toString());
//				}
				Object strValue = _makeValueObject(mapColumns.get(j), mapColumnType.get(j));
				
				// tdb 내부적으로 사용하는 컬럼을 보이지 않도록 합니다.
				if(!SQLUtil.isTDBSpecialColumn(mapLabelName.get(j))) {
					if(j != (mapLabelName.size()-1)) strResult += strValue + ",";
					else strResult += strValue;
				}
			}
			
			strColumns = StringUtils.removeEnd(strColumns, ",");
			strResult = StringUtils.removeEnd(strResult, ",");
			
			if (isFirst) {
				isFirst = false;
				sbInsertInto.append(String.format(INSERT_INTO_STMT, strColumns, strResult));
			}else{
				sbInsertInto.append(String.format(NEXT_INSERT_INTO_STMT, strResult));
			}
			
			if( dataList.size() > 1 && i > 1 && (i%DATA_COUNT) == 0) {
				isFirst = true;
				sbInsertInto.append(PublicTadpoleDefine.SQL_DELIMITER); 
				sbInsertInto.append(PublicTadpoleDefine.LINE_SEPARATOR); 

				if(commit > 0) {
					sbInsertInto.append("COMMIT" + PublicTadpoleDefine.SQL_DELIMITER + PublicTadpoleDefine.LINE_SEPARATOR);
				} 

				FileUtils.writeStringToFile(new File(strFullPath), sbInsertInto.toString(), encoding, true);
				sbInsertInto.setLength(0);
			}
			
		}
		if(sbInsertInto.length() > 0) {
			sbInsertInto.append(PublicTadpoleDefine.SQL_DELIMITER); 
			sbInsertInto.append(PublicTadpoleDefine.LINE_SEPARATOR); 
			
			if(commit > 0) {
				sbInsertInto.append("COMMIT" + PublicTadpoleDefine.SQL_DELIMITER + PublicTadpoleDefine.LINE_SEPARATOR);
			} 
			
			FileUtils.writeStringToFile(new File(strFullPath), sbInsertInto.toString(), encoding, true);
		}
		
		return strFullPath;
	}
}
