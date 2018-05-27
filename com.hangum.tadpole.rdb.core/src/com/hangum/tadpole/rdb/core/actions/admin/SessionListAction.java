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
package com.hangum.tadpole.rdb.core.actions.admin;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.hangum.tadpole.commons.exception.dialog.ExceptionDetailsErrorDialog;
import com.hangum.tadpole.commons.libs.core.message.CommonMessages;
import com.hangum.tadpole.engine.define.DBGroupDefine;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;
import com.hangum.tadpole.rdb.core.Activator;
import com.hangum.tadpole.rdb.core.Messages;
import com.hangum.tadpole.rdb.core.editors.sessionlist.SessionListEditor;
import com.hangum.tadpole.rdb.core.editors.sessionlist.SessionListEditorInput;

/**
 * Session List Action
 * 
 * @since 2013.04.01
 * @author hangum
 *
 */
public class SessionListAction implements IViewActionDelegate {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(SessionListAction.class);

	public final static String ID = "com.hangum.db.browser.rap.core.actions.object.admin.sessionlist"; //$NON-NLS-1$

	private IStructuredSelection sel;

	public SessionListAction() {
		super();
	}

	@Override
	public void run(IAction action) {
		UserDBDAO userDB = (UserDBDAO)sel.getFirstElement();
		
		if(DBGroupDefine.MYSQL_GROUP == userDB.getDBGroup() ||
			DBGroupDefine.ORACLE_GROUP == userDB.getDBGroup() ||
			DBGroupDefine.MSSQL_GROUP == userDB.getDBGroup() ||
			DBGroupDefine.POSTGRE_GROUP == userDB.getDBGroup() ||
			DBGroupDefine.ALTIBASE_GROUP == userDB.getDBGroup()) 
		{
			
			try {
				SessionListEditorInput sleInput = new SessionListEditorInput(userDB);
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(sleInput, SessionListEditor.ID);
			} catch (PartInitException e) {
				logger.error("open session list", e); //$NON-NLS-1$
				
				Status errStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e); //$NON-NLS-1$
				ExceptionDetailsErrorDialog.openError(null, CommonMessages.get().Error, Messages.get().AbstractQueryAction_1, errStatus); //$NON-NLS-1$
			}
		} else {
			MessageDialog.openWarning(null, CommonMessages.get().Information, Messages.get().NotSupportDatabase);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		sel = (IStructuredSelection)selection;
	}

	@Override
	public void init(IViewPart view) {
	}
}
