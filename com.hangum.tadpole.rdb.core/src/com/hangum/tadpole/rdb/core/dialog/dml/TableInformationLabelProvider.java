/*******************************************************************************
 * Copyright (c) 2016 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.hangum.tadpole.rdb.core.dialog.dml;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.hangum.tadpole.rdb.core.viewers.object.ObjectExploreDefine;
import com.tadpole.common.define.core.define.PublicTadpoleDefine;

/**
 * generate statement dml
 * 
 * @author hangum
 *
 */
public class TableInformationLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0) {
			ExtendTableColumnDAO columnDao = (ExtendTableColumnDAO) element;
			
			Image imageBase = null;
			if(PublicTadpoleDefine.isPK(columnDao.getKey())) 		imageBase = ObjectExploreDefine.IMAGE_PRIMARY_KEY; 
			else if(PublicTadpoleDefine.isFK(columnDao.getKey())) 	imageBase = ObjectExploreDefine.IMAGE_FOREIGN_KEY; 
			else if(PublicTadpoleDefine.isMUL(columnDao.getKey())) imageBase = ObjectExploreDefine.IMAGE_MULTI_KEY;
			else 											imageBase = ObjectExploreDefine.IMAGE_COLUMN;
			
			return imageBase;
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		ExtendTableColumnDAO dao = (ExtendTableColumnDAO) element;

		switch (columnIndex) {
		case 0: return dao.getName();
		case 1: return dao.getType();
		case 2: return dao.getKey();
		case 3: return dao.getComment();
		} 

		return "*** not set column value ***"; //$NON-NLS-1$
	}

}