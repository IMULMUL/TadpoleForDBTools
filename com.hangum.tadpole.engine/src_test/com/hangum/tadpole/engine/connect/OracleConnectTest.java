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
package com.hangum.tadpole.engine.connect;

/**
 * oracle connect test 
 * 
 * @author hangum
 *
 */
public class OracleConnectTest extends AbstractDriverInfo {

	public static void main(String args[]) {
//		try {
//			Class.forName("oracle.jdbc.driver.OracleDriver");
//			String url = "jdbc:oracle:thin:@192.168.216.129:1521:XE";
//	
//			Properties props = new Properties();
//			props.put("user", "HR");
//			props.put("password", "tadpole");
//			//props.put("ResultSetMetaDataOptions", "1");
//	
//			Connection conn = DriverManager.getConnection(url, props);
//			
////			System.out.println("===> catalog : " + conn.getCatalog() );
////			System.out.println("===> schema : " + conn.getSchema() );
//			printMetaData(conn.getMetaData());
//			
//			
////	
//			PreparedStatement preStatement = conn.prepareStatement(
//"begin execute immediate 'CREATE OR REPLACE and RESOLVE JAVA SOURCE NAMED TESTJ AS public class TESTJ { public static String helloworld(String str) { return str;}}'; end; ");//select * from v$version");
//			ResultSet result = preStatement.executeQuery();
//			
//			preStatement.cancel();
//			
//			ResultSetMetaData rsm = result.getMetaData();
//			OracleResultSetMetaData orsm = (OracleResultSetMetaData)rsm;
//			System.out.println("Table name is " + rsm.getTableName(1) + "." + " column is " + rsm.getColumnName(1)) ;
//			System.out.println("Table name is " + orsm.getTableName(1) + "." + orsm.getCatalogName(1)) ;
//			
//			while (result.next()) {
//				System.out.println("Information is : " + result.getString(1) + ", table name is " + result.getString(1));
//			}
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
	}

}
