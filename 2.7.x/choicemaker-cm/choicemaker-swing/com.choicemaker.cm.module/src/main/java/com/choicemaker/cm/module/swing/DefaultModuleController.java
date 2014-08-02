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
package com.choicemaker.cm.module.swing;

import javax.swing.text.Document;

import com.choicemaker.cm.module.IMessageSupport;
import com.choicemaker.cm.module.IModuleController;
import com.choicemaker.cm.module.INamedState;
import com.choicemaker.cm.module.namedevent.NamedEventSupport;
import com.choicemaker.cm.module.namedstate.NamedState;
import com.choicemaker.cm.module.namedstate.NamedStateSupport;
import com.choicemaker.cm.module.properties.PropertyControlSupport;


/**
 * @author rphall
 */
public class DefaultModuleController implements IModuleController {
	
	public static class DefaultOperationModel implements IOperationModel {
	}

	private IEventModel iem;
	
	private IMessageSupport ims;
	
	private IOperationModel iom;
	
	private IStateModel ism;	
	
	private IUserInterface iui;

	private IStatusModel ium;

	private IConfigurationModel pcs;
	
	/**
	 * @param d a Swing Document object that will be used for output,
	 * typically the model object behind some JTextArea component that is
	 * part of the application UI
	 */
	public DefaultModuleController(Document d) {
		this.iom = new DefaultOperationModel();
		this.ium = new Accumulator(d);
		this.iem = new NamedEventSupport(this);
		this.ism = new NamedStateSupport(this) {
			public INamedState getCurrentState() {
				return new NamedState();
			}
			public void setCurrentState(INamedState ignored) {
			}
		};
		this.iui = new DefaultPanelControl();
		this.pcs = new PropertyControlSupport(this);
		this.ims = new DefaultMessageSupport();
	}
	
	public DefaultModuleController(
		IOperationModel operationModel,
		IStatusModel statusModel,
		IEventModel eventModel,
		IStateModel stateModel,
		IUserInterface userInterface,
		IConfigurationModel configurationModel,
		IMessageSupport messageSupport
	) {
		setOperationModel(operationModel);
		setStatusModel(statusModel);
		setEventModel(eventModel);
		setStateModel(stateModel);
		setUserInterface(userInterface);
		setConfigurationModel(configurationModel);
		setMessageSupport(messageSupport);
	}

	public IConfigurationModel getConfigurationModel() {
		if (pcs == null) {
			throw new IllegalStateException("null user messages");
		}
		return this.pcs;
	}

	public IEventModel getEventModel() {
		if (iem == null) {
			throw new IllegalStateException("null user messages");
		}
		return this.iem;
	}
	
	public IMessageSupport getMessageSupport() {
		if (ims == null) {
			throw new IllegalStateException("null message support");
		}
		return this.ims;
	}
	
	public IOperationModel getOperationModel() {
		if (iom == null) {
			throw new IllegalStateException("null user messages");
		}
		return this.iom;
	}

	public IStateModel getStateModel() {
		if (ism == null) {
			throw new IllegalStateException("null user messages");
		}
		return this.ism;
	}

	public IStatusModel getStatusModel() {
		if (iui == null) {
			throw new IllegalStateException("null user messages");
		}
		return this.ium;
	}

	public IUserInterface getUserInterface() {
		if (iui == null) {
			throw new IllegalStateException("null user messages");
		}
		return this.iui;
	}
	
	protected void setConfigurationModel(IConfigurationModel o) {
		if (o == null) {
			throw new IllegalArgumentException("null configuration model");
		}
		this.pcs = o;
	}
	
	protected void setEventModel(IEventModel o) {
		if (o == null) {
			throw new IllegalArgumentException("null event model");
		}
		this.iem = o;
	}
	
	protected void setMessageSupport(IMessageSupport o) {
		if (o == null) {
			throw new IllegalArgumentException("null message support");
		}
		this.ims = o;
	}
	
	protected void setOperationModel(IOperationModel o) {
		if (o == null) {
			throw new IllegalArgumentException("null operation model");
		}
		this.iom = o;
	}
	
	protected void setStateModel(IStateModel o) {
		if (o == null) {
			throw new IllegalArgumentException("null state model");
		}
		this.ism = o;
	}
	
	protected void setStatusModel(IStatusModel o) {
		if (o == null) {
			throw new IllegalArgumentException("null status model");
		}
		this.ium = o;
	}
	
	protected void setUserInterface(IUserInterface o) {
		if (o == null) {
			throw new IllegalArgumentException("null user-interface model");
		}
		this.iui = o;
	}
	
}

