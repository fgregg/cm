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
package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.rmi.RemoteException;
import java.util.Date;

import javax.ejb.EJBObject;

import com.choicemaker.cm.io.blocking.automated.offline.core.IControl;

/**
 * @author pcheung
 *
 */
public interface TransitivityJob extends EJBObject, IControl {

	public static String STATUS_NEW = "NEW";
	public static String STATUS_QUEUED = "QUEUED";
	public static String STATUS_STARTED = "STARTED";
	public static String STATUS_COMPLETED = "COMPLETED";
	public static String STATUS_FAILED = "FAILED";
	public static String STATUS_ABORT_REQUESTED = "ABORT_REQUESTED";
	public static String STATUS_ABORTED = "ABORTED";
	public static String CLEAR = "CLEAR";

	// CMP fields
	/** For CMP only */
	void setId(Long id) throws RemoteException;
	Long getId() throws RemoteException;

	/** For CMP only; use markAsXxx() methods instead */
	void setDescription(String description) throws RemoteException;
	String getDescription() throws RemoteException;

	/** For CMP only; use markAsXxx() methods instead */
	void setStatus(String status) throws RemoteException;
	String getStatus() throws RemoteException;

	/** For CMP only: use updateFractionCompleted(float) instead */
	void setFractionComplete(int i) throws RemoteException;
	int getFractionComplete() throws RemoteException;

	/** For CMP only */
	void setModel(String model) throws RemoteException;
	String getModel() throws RemoteException;

	/** For CMP only */
	void setDiffer(float differ) throws RemoteException;
	float getDiffer() throws RemoteException;

	/** For CMP only */
	void setMatch(float match) throws RemoteException;
	float getMatch() throws RemoteException;

	/** For CMP only */
	void setRequested(Date date) throws RemoteException;
	Date getRequested() throws RemoteException;

	/** For CMP only; use markAsQueued() method instead */
	void setQueued(Date queued) throws RemoteException;
	Date getQueued() throws RemoteException;

	/** For CMP only; use markAsStarted() method instead */
	void setStarted(Date started) throws RemoteException;
	Date getStarted() throws RemoteException;

	/** For CMP only; use markAsUpdated() method instead */
	void setUpdated(Date updated) throws RemoteException;
	Date getUpdated() throws RemoteException;

	/** For CMP only; use markAsCompleted() method instead */
	void setCompleted(Date completed) throws RemoteException;
	Date getCompleted() throws RemoteException;

	/** For CMP only; use markAsFailed() method instead */
	void setFailed(Date failed) throws RemoteException;
	Date getFailed() throws RemoteException;

	/** For CMP only; use markAsAbortRequested() method instead */
	void setAbortRequested(Date abortRequested) throws RemoteException;
	Date getAbortRequested() throws RemoteException;

	/** For CMP only; use markAsAborted() method instead */
	void setAborted(Date aborted) throws RemoteException;
	Date getAborted() throws RemoteException;

	void markAsStarted() throws RemoteException;

	void markAsCompleted() throws RemoteException;

	void markAsFailed() throws RemoteException;

	void markAsAbortRequested() throws RemoteException;

	void markAsAborted() throws RemoteException;

	void updateFractionCompleted(int fractionCompleted)
		throws RemoteException;

}