/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.reviewmaker.base;

import java.rmi.RemoteException;

import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.sort.SortCondition;

public class PagedQuery {

	private static int DEFAULT_BLOCK_SIZE = 100;

	protected ServerLiaison serverLiaison;
	protected Record r;
	protected SortCondition sc;

	protected int numTotal;

	protected int pageSize;
	protected int pageNum;
	protected Record[] page;

	protected boolean hasNext;

	public PagedQuery(ServerLiaison sl) {
		this(sl, null, null, DEFAULT_BLOCK_SIZE, 0);
	}

	public PagedQuery(ServerLiaison sl, Record r) {
		this(sl, r, null, DEFAULT_BLOCK_SIZE, 0);
	}

	public PagedQuery(ServerLiaison sl, Record r, SortCondition sc) {
		this(sl, r, sc, DEFAULT_BLOCK_SIZE, 0);
	}

	public PagedQuery(ServerLiaison sl, Record r, SortCondition sc, int pageSize, int pageNum) {
		this.serverLiaison = sl;
		this.r = r;
		this.sc = sc;
		this.pageSize = pageSize;
		this.pageNum = pageNum;

		initPage();
	}

	private void initPage() {
		int desiredLow = pageNum * pageSize;
		int desiredHigh = desiredLow + pageSize + 1; // to see if there are more pages...

		Record[] ret = null;

		try {
			ret = serverLiaison.getRecordsNeedingReview(r, sc, desiredLow, desiredHigh);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		if (ret != null) {
			hasNext = ret.length > pageSize;

			int len = Math.min(ret.length, pageSize);
			Record[] temp = new Record[len];
			System.arraycopy(ret, 0, temp, 0, len);

			page = temp;
		} else {
			page = new Record[0];
		}

		if (r == null) {
			try {
				numTotal = -1;
				numTotal = serverLiaison.getNumPendingReviews();
			} catch (Exception ex) {
				numTotal = -1;
			}
		} else if (page.length < pageSize) {
			numTotal = getHighIndex(); 
		} else {
			numTotal = -1;
		}
	}

	public Record[] getCurrentPage() {
		Record[] ret = new Record[page.length];
		System.arraycopy(page, 0, ret, 0, page.length);
		return ret;
	}

	/**
	 * Returns the index of r, but only if it's in the current page.
	 */
	public int indexOf(Record r) {
		if (r == null) {
			return -1;	
		}
		
		for (int i = 0; i < page.length; i++) {
			if (page[i].getId().equals(r.getId())) {
				return getLowIndex() + i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Returns null if not found.
	 */
	public Record getRecord(int index) {
		if (index < getLowIndex() || index >= getHighIndex()) {
			return null;
		} else {
			return page[index - pageNum*pageSize];	
		}
	}

	public Record getQueryRecord() {
		return r;
	}

	public int getNumTotalRecords() {
		return numTotal;
	}

	public int getPageSize() {
		return pageSize;	
	}

	public int getPageNum() {
		return pageNum;	
	}

	public int getLowIndex() {
		return pageNum * pageSize;
	}

	public int getHighIndex() {
		return pageNum * pageSize + page.length;
	}

	public boolean hasNextPage() {
		return hasNext;
	}

	public boolean hasPreviousPage() {
		return pageNum > 0;
	}

	public void nextPage() {
		if (!hasNextPage()) {
			throw new IllegalStateException();
		}

		pageNum++;
		initPage();
	}

	public void previousPage() {
		if (!hasPreviousPage()) {
			throw new IllegalArgumentException();
		}

		pageNum--;
		initPage();
	}
	
	public void refreshPage() {
		initPage();	
	}
	
	public ServerLiaison getServerLiaison() {
		return serverLiaison;	
	}

}
