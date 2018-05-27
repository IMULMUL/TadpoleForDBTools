/*******************************************************************************
 * Copyright (c) 2013 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     nilriri - initial API and implementation
 ******************************************************************************/
package com.hangum.tadpole.rdb.core.editors.dbinfos.composites;

import java.sql.PreparedStatement;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;

import com.hangum.tadpole.engine.define.DBDefine;
import com.hangum.tadpole.engine.define.DBGroupDefine;
import com.hangum.tadpole.engine.manager.TadpoleSQLManager;
import com.hangum.tadpole.engine.query.dao.rdb.RDBInfomationforColumnDAO;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;
import com.hangum.tadpole.rdb.core.viewers.object.sub.rdb.table.CommentCellEditor;

/**
 * column comment editor
 * 
 * @author nilriri
 * 
 */
public class DBInfoCommentEditorSupport extends EditingSupport {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(DBInfoCommentEditorSupport.class);

	private final TableViewer viewer;
	private UserDBDAO userDB;
	private int column;

	/**
	 * 
	 * @param viewer
	 * @param explorer
	 */
	public DBInfoCommentEditorSupport(TableViewer viewer, UserDBDAO userDB, int column) {
		super(viewer);

		this.viewer = viewer;
		this.userDB = userDB;
		this.column = column;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		if (column == 1 || column == 3)
			return new CommentCellEditor(column, viewer);
		else
			return null;
	}

	@Override
	protected boolean canEdit(Object element) {
		if (column == 1 || column == 3) {
			if (logger.isDebugEnabled()) logger.debug("DBMS Type is " + userDB.getDBDefine());

			if(DBGroupDefine.ORACLE_GROUP == userDB.getDBGroup()  
				|| DBGroupDefine.POSTGRE_GROUP == userDB.getDBGroup()
				|| DBGroupDefine.MSSQL_GROUP == userDB.getDBGroup()
			) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	protected Object getValue(Object element) {
		try {
			RDBInfomationforColumnDAO dao = (RDBInfomationforColumnDAO) element;
			String comment = "";
			switch (column) {
			case 1:
				comment = dao.getTable_comment();
				break;
			case 3:
				comment = dao.getColumn_comment();
				break;
			}
			return comment == null ? "" : comment;
		} catch (Exception e) {
			logger.error("getValue error ", e);
			return "";
		}
	}

	@Override
	protected void setValue(Object element, Object value) {
		String comment = "";
		try {
			if (logger.isDebugEnabled())
				logger.debug("element.getClass().toString() is " + element.getClass().toString());

			RDBInfomationforColumnDAO dao = (RDBInfomationforColumnDAO) element;

			comment = (String) (value == null ? "" : value);

			if (logger.isDebugEnabled())
				logger.debug("comment update target is " + dao.getTable_name() + "." + dao.getColumn_name());

			switch (column) {
			case 1:
				// 기존 코멘트와 다를때만 db에 반영한다.
				if (!(comment.equals(dao.getTable_comment()))) {
					dao.setTable_comment(comment);
					ApplyTableComment(dao);
				}
				break;
			case 3: // 기존 코멘트와 다를때만 db에 반영한다.
				if (!(comment.equals(dao.getColumn_comment()))) {
					dao.setColumn_comment(comment);
					ApplyColumnComment(dao);
				}
				break;

			}

			viewer.update(dao, null);
		} catch (Exception e) {
			logger.error("setValue error ", e);
		}
		viewer.update(element, null);
	}

	private void ApplyColumnComment(RDBInfomationforColumnDAO dao) {
		// TODO : DBMS별 처리를 위해 별도의 Class로 분리해야 하지 않을까?

		java.sql.Connection javaConn = null;
		PreparedStatement stmt = null;
		try {

			if (logger.isDebugEnabled())
				logger.debug("userDB is " + userDB.toString());

			javaConn = TadpoleSQLManager.getConnection(userDB);

			// IStructuredSelection is = (IStructuredSelection)
			// viewer.getSelection();

			// TableCreateDAO tableDAO = (TableCreateDAO) is.getFirstElement();

			StringBuffer query = new StringBuffer();

			if (DBGroupDefine.ORACLE_GROUP == userDB.getDBGroup() || DBGroupDefine.POSTGRE_GROUP == userDB.getDBGroup()) {

				query.append(" COMMENT ON COLUMN ").append(dao.getTable_name() + ".").append(dao.getColumn_name()).append(" IS '").append(dao.getColumn_comment()).append("'");

				if (logger.isDebugEnabled())
					logger.debug("query is " + query.toString());

				stmt = javaConn.prepareStatement(query.toString());
				stmt.execute();

			} else if (DBDefine.MSSQL_8_LE_DEFAULT == userDB.getDBDefine()) {
				query.append(" exec sp_dropextendedproperty 'Caption' ").append(", 'user' ,").append(userDB.getUsers());
				query.append(",'table' , '").append(dao.getTable_name()).append("'");
				query.append(",'column' , '").append(dao.getColumn_name()).append("'");
				stmt = javaConn.prepareStatement(query.toString());
				try {
					stmt.execute();
				} catch (Exception e) {
					if (logger.isDebugEnabled())
						logger.debug("query is " + query.toString());
					logger.error("Comment drop error ", e);
				} finally {
					try { if(stmt != null) stmt.close(); } catch (Exception e) { }
				}

				try {
					query = new StringBuffer();
					query.append(" exec sp_addextendedproperty 'Caption', '").append(dao.getColumn_comment()).append("' ,'user' ,").append(userDB.getUsers());
					query.append(",'table' , '").append(dao.getTable_name()).append("'");
					query.append(",'column', '").append(dao.getColumn_name()).append("'");
					stmt = javaConn.prepareStatement(query.toString());
					stmt.execute();
				} catch (Exception e) {
					if(logger.isDebugEnabled()) logger.debug("query is " + query.toString());
					logger.error("Comment add error ", e);
				} finally {
					try { if(stmt != null) stmt.close(); } catch (Exception e) { }
				}
			} else if (DBDefine.MSSQL_DEFAULT == userDB.getDBDefine()) {
				query.append(" exec sp_dropextendedproperty 'Caption' ").append(", 'user' , dbo ");
				query.append(",'table' , '").append(dao.getTable_name()).append("'");
				query.append(",'column' , '").append(dao.getColumn_name()).append("'");
				stmt = javaConn.prepareStatement(query.toString());
				try {
					stmt.execute();
				} catch (Exception e) {
					if(logger.isDebugEnabled()) logger.debug("query is " + query.toString());
					logger.error("Comment drop error ", e);
				} finally {
					try { if(stmt != null) stmt.close(); } catch (Exception e) { }
				}

				try {
					query = new StringBuffer();
					query.append(" exec sp_addextendedproperty 'Caption', '").append(dao.getColumn_comment()).append("' ,'user' , dbo ");
					query.append(",'table' , '").append(dao.getTable_name()).append("'");
					query.append(",'column', '").append(dao.getColumn_name()).append("'");
					stmt = javaConn.prepareStatement(query.toString());
					stmt.execute();
				} catch (Exception e) {
					if(logger.isDebugEnabled()) logger.debug("query is " + query.toString());
					logger.error("Comment add error ", e);
				} finally {
					try { if(stmt != null) stmt.close(); } catch (Exception e) { }
				}
			}

		} catch (Exception e) {
			logger.error("Comment change error ", e);
		} finally {
			try { if(stmt != null) stmt.close(); } catch (Exception e) { }
			try { if(javaConn != null) javaConn.close(); } catch (Exception e) { }
		}
	}

	private void ApplyTableComment(RDBInfomationforColumnDAO dao) {
		// TODO : DBMS별 처리를 위해 별도의 Class로 분리해야 하지 않을까?

		java.sql.Connection javaConn = null;
		PreparedStatement stmt = null;
		try {

			if(logger.isDebugEnabled()) logger.debug("userDB is " + userDB.toString());
			javaConn = TadpoleSQLManager.getConnection(userDB);

			StringBuffer query = new StringBuffer();

			if (DBGroupDefine.ORACLE_GROUP == userDB.getDBGroup() || DBGroupDefine.POSTGRE_GROUP == userDB.getDBGroup()) {
				query.append(" COMMENT ON TABLE ").append(dao.getTable_name()).append(" IS '").append(dao.getTable_comment()).append("'");

				stmt = javaConn.prepareStatement(query.toString());
				stmt.execute();

			} else if (DBDefine.MSSQL_8_LE_DEFAULT == userDB.getDBDefine()) {
				query.append(" exec sp_dropextendedproperty 'Caption' ").append(", 'user' ,").append(userDB.getUsers()).append(",'table' ").append(" , '").append(dao.getTable_name()).append("'");
				stmt = javaConn.prepareStatement(query.toString());
				try {
					stmt.execute();
				} catch (Exception e) {
					if(logger.isDebugEnabled()) logger.debug("query is " + query.toString());
					logger.error("Comment drop error ", e);
				} finally {
					try { if(stmt != null) stmt.close(); } catch (Exception e) { }
				}

				try {
					query = new StringBuffer();
					query.append(" exec sp_addextendedproperty 'Caption', '").append(dao.getTable_comment()).append("' ,'user' ,").append(userDB.getUsers()).append(",'table' ").append(" , '").append(dao.getTable_name()).append("'");
					stmt = javaConn.prepareStatement(query.toString());
					stmt.execute();
				} catch (Exception e) {
					if(logger.isDebugEnabled()) logger.debug("query is " + query.toString());
					logger.error("Comment add error ", e);
				} finally {
					try { if(stmt != null) stmt.close(); } catch (Exception e) { }
				}
			} else if (DBDefine.MSSQL_DEFAULT == userDB.getDBDefine()) {
				query.append(" exec sp_dropextendedproperty 'Caption' ").append(", 'user' , dbo,'table' ").append(" , '").append(dao.getTable_name()).append("'");
				stmt = javaConn.prepareStatement(query.toString());
				try {
					stmt.execute();
				} catch (Exception e) {
					if(logger.isDebugEnabled()) logger.debug("query is " + query.toString());
					logger.error("Comment drop error ", e);
				} finally {
					try { if(stmt != null) stmt.close(); } catch (Exception e) { }
				}

				try {
					query = new StringBuffer();
					query.append(" exec sp_addextendedproperty 'Caption', '").append(dao.getTable_comment()).append("' ,'user' , dbo ,'table' ").append(" , '").append(dao.getTable_name()).append("'");
					stmt = javaConn.prepareStatement(query.toString());
					stmt.execute();
				} catch (Exception e) {
					if(logger.isDebugEnabled()) logger.debug("query is " + query.toString());
					logger.error("Comment add error ", e);
				} finally {
					try { if(stmt != null) stmt.close(); } catch (Exception e) { }
				}
			}

		} catch (Exception e) {
			logger.error("Comment change error ", e);
		} finally {
			try { if(stmt != null) stmt.close(); } catch (Exception e) {}
			try { if(javaConn != null) javaConn.close(); } catch (Exception e) { }
		}
	}

}
