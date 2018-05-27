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
package com.hangum.tadpole.engine.sql.util.executer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;

import com.hangum.tadpole.commons.libs.core.message.CommonMessages;
import com.hangum.tadpole.engine.Messages;
import com.hangum.tadpole.engine.define.DBGroupDefine;
import com.hangum.tadpole.engine.manager.TadpoleSQLManager;
import com.hangum.tadpole.engine.query.dao.DBInfoDAO;
import com.hangum.tadpole.engine.query.dao.mysql.ProcedureFunctionDAO;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;
import com.hangum.tadpole.engine.sql.util.executer.procedure.MSSQLProcedureExecuter;
import com.hangum.tadpole.engine.sql.util.executer.procedure.MySqlProcedureExecuter;
import com.hangum.tadpole.engine.sql.util.executer.procedure.OracleProcedureExecuter;
import com.hangum.tadpole.engine.sql.util.executer.procedure.PostgreSQLProcedureExecuter;
import com.hangum.tadpole.engine.sql.util.executer.procedure.ProcedureExecutor;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * RDB procedure executer manager
 * 
 * @author hangum
 *
 */
public class ProcedureExecuterManager {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger
			.getLogger(ProcedureExecuterManager.class);

	protected UserDBDAO userDB;
	protected ProcedureFunctionDAO procedureDAO;
	
	public ProcedureExecuterManager(UserDBDAO userDB, ProcedureFunctionDAO procedureDAO) {
		this.userDB = userDB;
		this.procedureDAO = procedureDAO;
	}

	/**
	 * return procedure executer
	 * 
	 * @return
	 * @throws Exception
	 */
	public ProcedureExecutor getExecuter() throws Exception {
		if(DBGroupDefine.ORACLE_GROUP == userDB.getDBGroup()) {
			return new OracleProcedureExecuter(procedureDAO, userDB);
		} else if(DBGroupDefine.MSSQL_GROUP == userDB.getDBGroup()) {
			return new MSSQLProcedureExecuter(procedureDAO, userDB);
		} else if(DBGroupDefine.MYSQL_GROUP == userDB.getDBGroup()) {
			return new MySqlProcedureExecuter(procedureDAO, userDB);
		} else if(DBGroupDefine.POSTGRE_GROUP == userDB.getDBGroup()) {
			return new PostgreSQLProcedureExecuter(procedureDAO, userDB);
		} else {
			throw new Exception(Messages.get().ProcedureExecuterManager_0);
		}
	}
	
	/**
	 * DB is that it supports?
	 * 
	 * @return
	 */
	public boolean isSupport() {
		try {
			getExecuter();
			
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	/**
	 * Is executed procedure?
	 * 
	 * @param procedureDAO
	 * @param useDB
	 * @return
	 */
	public boolean isExecuted(ProcedureFunctionDAO procedureDAO, UserDBDAO selectUseDB) {
		if(!isSupport()) {
			MessageDialog.openWarning(null, CommonMessages.get().Warning, Messages.get().ProcedureExecuterManager_0);
			return false;
		}
		if(!procedureDAO.isValid()) {
			MessageDialog.openWarning(null, CommonMessages.get().Warning, Messages.get().ProcedureExecuterManager_4);
			return false;
		}
		
		if(DBGroupDefine.MYSQL_GROUP == userDB.getDBGroup()) {
			double dbVersion = 0.0;
			try {
				SqlMapClient sqlClient = TadpoleSQLManager.getInstance(userDB);				
				DBInfoDAO dbInfo = (DBInfoDAO)sqlClient.queryForObject("findDBInfo"); //$NON-NLS-1$
				dbVersion = Double.parseDouble( StringUtils.substring(dbInfo.getProductversion(), 0, 3) );
			
				if (dbVersion < 5.5){
					MessageDialog.openInformation(null, CommonMessages.get().Information, Messages.get().ProcedureExecuterManager_6);
					return false;
				}
			} catch (Exception e) {
				logger.error("find DB info", e); //$NON-NLS-1$
				
				return false;
			}

		}
		
		try {
			ProcedureExecutor procedureExecutor = getExecuter();
			procedureExecutor.getInParameters();
		} catch(Exception e) {
			MessageDialog.openError(null,CommonMessages.get().Error, e.getMessage());
			return false;
		}
		
		return true;
	}
	
}
