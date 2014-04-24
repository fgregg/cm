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
package com.choicemaker.cm.modelmaker.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.EventListener;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.eclipse.core.boot.IPlatformRunnable;

import com.choicemaker.cm.analyzer.filter.BooleanFilterCondition;
import com.choicemaker.cm.analyzer.filter.FilterCondition;
import com.choicemaker.cm.analyzer.filter.RuleFilterCondition;
import com.choicemaker.cm.compiler.gen.ant.CallAnt;
import com.choicemaker.cm.compiler.impl.CompilerFactory;
import com.choicemaker.cm.core.ClueDesc;
import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableMarkedRecordPair;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MarkedRecordPairBinder;
import com.choicemaker.cm.core.MarkedRecordPairSink;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.Repository;
import com.choicemaker.cm.core.RepositoryChangeEvent;
import com.choicemaker.cm.core.RepositoryChangeListener;
import com.choicemaker.cm.core.Thresholds;
import com.choicemaker.cm.core.compiler.CompilerException;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.ml.MachineLearner;
import com.choicemaker.cm.core.ml.none.None;
import com.choicemaker.cm.core.train.Trainer;
import com.choicemaker.cm.core.util.Arguments;
import com.choicemaker.cm.core.util.ArrayHelper;
import com.choicemaker.cm.core.util.IntArrayList;
import com.choicemaker.cm.core.util.LoggingObject;
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.core.util.OperationFailedException;
import com.choicemaker.cm.core.xmlconf.ProbabilityModelsXmlConf;
import com.choicemaker.cm.core.xmlconf.XmlConfException;
import com.choicemaker.cm.core.xmlconf.XmlConfigurator;
import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.gui.utils.dialogs.AboutDialog;
import com.choicemaker.cm.gui.utils.plaf.ChoiceMakerMetalTheme;
import com.choicemaker.cm.modelmaker.ModelMakerEventNames;
import com.choicemaker.cm.modelmaker.filter.ListeningMarkedRecordPairFilter;
import com.choicemaker.cm.modelmaker.filter.ModelMakerMRPFilter;
import com.choicemaker.cm.modelmaker.gui.abstraction.PreferenceDefaults;
import com.choicemaker.cm.modelmaker.gui.abstraction.PreferenceKeys;
import com.choicemaker.cm.modelmaker.gui.dialogs.RecordPairFilterDialog;
import com.choicemaker.cm.modelmaker.gui.dialogs.StartDialog;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationEvent;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationListener;
import com.choicemaker.cm.modelmaker.gui.listeners.EventMultiplexer;
import com.choicemaker.cm.modelmaker.gui.menus.ModelMenu;
import com.choicemaker.cm.modelmaker.gui.menus.SourceMenu;
import com.choicemaker.cm.modelmaker.gui.menus.ToolBar;
import com.choicemaker.cm.modelmaker.gui.menus.ToolsMenu;
import com.choicemaker.cm.modelmaker.gui.menus.ViewMenu;
import com.choicemaker.cm.modelmaker.gui.ml.MlGuiFactories;
import com.choicemaker.cm.modelmaker.gui.panels.HumanReviewPanel;
import com.choicemaker.cm.modelmaker.gui.panels.MessagePanel;
import com.choicemaker.cm.modelmaker.gui.panels.RecordPairList;
import com.choicemaker.cm.modelmaker.gui.panels.TestingControlPanel;
import com.choicemaker.cm.modelmaker.gui.panels.TrainingControlPanel;
import com.choicemaker.cm.modelmaker.gui.tables.ClueTableModel;
import com.choicemaker.cm.modelmaker.gui.utils.Log4jAppender;
import com.choicemaker.cm.modelmaker.gui.utils.ThreadWatcher;
import com.choicemaker.cm.modelmaker.stats.Statistics;
import com.choicemaker.cm.module.IUserMessages;
import com.choicemaker.cm.module.swing.DefaultManagedPanel;
import com.choicemaker.cm.module.swing.DefaultModuleMenu;

/**
 *
 * @author Martin Buechi
 * @author S. Yoakum-Stover
 * @version $Revision: 1.3 $ $Date: 2010/03/29 12:38:18 $
 */
public class ModelMaker extends JFrame implements IPlatformRunnable {
	public static Logger logger = Logger.getLogger(ModelMaker.class);

	public static final int CLUES = 1;
	public static final int RULES = 2;

	//XmlConf file name
	private static String xmlConfFileName;

	// Delegates
	final private IUserMessages userMessages =
	new IUserMessages() {
			public Writer getWriter() {
				return ModelMaker.this.getMessagePanel().getWriter();
			}
			public OutputStream getOutputStream() {
				return ModelMaker.this.getMessagePanel().getOutputStream();
			}
			public PrintStream getPrintStream() {
				return ModelMaker.this.getMessagePanel().getPrintStream();
			}
			public void postMessage(final String s) {
				ModelMaker.this.getMessagePanel().getPrintStream();
			}
			public void clearMessages() {
				ModelMaker.this.getMessagePanel().clearMessages();
			}
			public void postInfo(String s) {
				String displayString =
					MessageUtil.m.formatMessage("train.gui.modelmaker.message.info", s) + Constants.LINE_SEPARATOR;
				ModelMaker.this.getMessagePanel().postMessage(displayString);
			}
	};
	public IUserMessages getUserMessages() {
		return userMessages;
	}

	//Frame icon
	private ImageIcon cmIcon;

	//Main content panel of this JFrame
	private JPanel myContentPanel;

	//RecordPairFilterDialog
	private ListeningMarkedRecordPairFilter filter;

	//Menus
	private JMenuBar myMenuBar;
	private SourceMenu sourceMenu;
	private ModelMenu modelMenu;
	private ViewMenu viewMenu;
	private JMenuItem exitItem;
	private JMenuItem aboutItem;

	// Toolbar
	private ToolBar toolbar;
	private RecordPairList recordPairList;

	//message panel
	private MessagePanel messagePanel;

	//tabbed panes
	private JTabbedPane tabbedPane;
	private JSplitPane splitPane;
	private TrainingControlPanel trainingPanel;
	private TestingControlPanel testingPanel;
	private HumanReviewPanel reviewPanel;
	private DefaultManagedPanel modulePanel;

	//training objects
	private IProbabilityModel probabilityModel;
	private MarkedRecordPairSource markedRecordPairSource;
	private static final int NUM_SOURCES = 2;
	private MarkedRecordPairSource[] multiSources = new MarkedRecordPairSource[NUM_SOURCES];
	private boolean keepAllSourcesInMemory;
	private int usedMultiSource = 0;
	private java.util.List[] multiSourceLists = new java.util.List[NUM_SOURCES];
	private boolean[] multiIncludeHolds = new boolean[NUM_SOURCES];
	private boolean includeHolds; // in source
	private Trainer trainer;
	private Statistics statistics;
	private boolean evaluated;
	private java.util.List sourceList;
	private IntArrayList checkedList = new IntArrayList();
	private int[] selection;
	private Repository repository = new Repository(null, null);

	//thresholds
	private Thresholds thresholds;

	private int markedRecordPair;
	private boolean sourceDataModified;

	private static Preferences preferences = Preferences.userNodeForPackage(ModelMaker.class);
	private Log4jAppender log4jAppender;

	// listeners
	private EventMultiplexer probabilityModelEventMultiplexer = new EventMultiplexer();
	private EventListenerList listenerList = new EventListenerList();

	private static final String FRAME_TITLE = MessageUtil.m.formatMessage("train.gui.modelmaker.title");

	public EventMultiplexer getProbabilityModelEventMultiplexer() {
		return probabilityModelEventMultiplexer;
	}

	public void addMarkedRecordPairDataChangeListener(RepositoryChangeListener l) {
		listenerList.add(RepositoryChangeListener.class, l);
	}

	public void removeMarkedRecordPairDataChangeListener(RepositoryChangeListener l) {
		listenerList.remove(RepositoryChangeListener.class, l);
	}

	public void fireMarkedRecordPairDataChange(RepositoryChangeEvent evt) {
		setSourceDataModified(true);
		EventListener[] listeners = listenerList.getListeners(RepositoryChangeListener.class);
		for (int i = listeners.length - 1; i >= 0; --i) {
			switch (evt.getID()) {
				case RepositoryChangeEvent.SET_CHANGED :
					 ((RepositoryChangeListener) listeners[i]).setChanged(evt);
					break;
				case RepositoryChangeEvent.RECORD_DATA_CHANGED :
					 ((RepositoryChangeListener) listeners[i]).recordDataChanged(evt);
					break;
				case RepositoryChangeEvent.MARKUP_DATA_CHANGED :
					 ((RepositoryChangeListener) listeners[i]).markupDataChanged(evt);
					break;
			}
		}
	}

	public void addEvaluationListener(EvaluationListener l) {
		listenerList.add(EvaluationListener.class, l);
	}

	public void removeEvaluationListener(EvaluationListener l) {
		listenerList.remove(EvaluationListener.class, l);
	}

	private void fireEvaluated(EvaluationEvent evt) {
		EventListener[] listeners = listenerList.getListeners(EvaluationListener.class);
		for (int i = listeners.length - 1; i >= 0; --i) {
			((EvaluationListener) listeners[i]).evaluated(evt);
		}
	}

	public static Preferences getPreferences() {
		return preferences;
	}

	public ModelMaker() {
	}

	/**
	 * Instantiates the one instance of Trainer that is used
	 * throughout.  Sets the look and feel.  Gives this JFrame
	 * its title and title icon.  Creates the content panel.
	 */
	private void init() {
		//Set the frame title
		setTitle(FRAME_TITLE);
		cmIcon = new ImageIcon(ModelMaker.class.getResource("images/cmIcon.gif"));
		setIconImage(cmIcon.getImage());
		//Create the content panel
		myContentPanel = new JPanel();
		getContentPane().add(myContentPanel, BorderLayout.CENTER);
		filter = new ModelMakerMRPFilter(this);

		float _dt = getPreferences().getFloat(PreferenceKeys.DIFFER_THRESHOLD,PreferenceDefaults.DIFFER_THRESHOLD);
		float _mt = getPreferences().getFloat(PreferenceKeys.MATCH_THRESHOLD,PreferenceDefaults.MATCH_THRESHOLD);
		setThresholds(new Thresholds(_dt, _mt));
	}

	/**
	 * Instantiates the main panels - Message, Training, Testing,
	 * and Review. Loads the latter three into a tabbed pane.
	 */
	private void buildComponents() {
		JavaHelpUtils.init(ModelMaker.class.getClassLoader());

		// Toolbar
		toolbar = new ToolBar(this, MessageUtil.m.formatMessage("train.gui.modelmaker.modelmaker"));
		getContentPane().add(toolbar, BorderLayout.NORTH);

		// Menus
		myMenuBar = new JMenuBar();
		setJMenuBar(myMenuBar);

		JMenu fileMenu = new JMenu(MessageUtil.m.formatMessage("train.gui.modelmaker.menu.file"));
		fileMenu.setMnemonic(KeyEvent.VK_F);
		myMenuBar.add(fileMenu);
		exitItem = new JMenuItem(MessageUtil.m.formatMessage("train.gui.modelmaker.menu.file.exit"));
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		fileMenu.add(exitItem);

		modelMenu = new ModelMenu(this);
		myMenuBar.add(modelMenu);

		sourceMenu = new SourceMenu(this);
		myMenuBar.add(sourceMenu);

		viewMenu = new ViewMenu(this);
		myMenuBar.add(viewMenu);

		//MessagePanel
		messagePanel = new MessagePanel(this);
		log4jAppender = new Log4jAppender(this, messagePanel.getWriter());
		log4jAppender.addTo("com");

		//TrainingPanel
		trainingPanel = new TrainingControlPanel(this);

		//TestingPanel
		testingPanel = new TestingControlPanel(this);

		//ClusterPanel
		modulePanel = new DefaultManagedPanel(messagePanel.getDocument(),this);

		//ReviewPanel
		reviewPanel = new HumanReviewPanel(this);

		//TabbedPane
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.training.tabtext"), trainingPanel);
		tabbedPane.addTab(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.test.tabtext"), testingPanel);
		tabbedPane.addTab(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.cluster.tabtext"), modulePanel);
		tabbedPane.addTab(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.humanreview.tabtext"), reviewPanel);

		//SplitPane to hold everything.
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerSize(2);
		splitPane.setContinuousLayout(true);
		splitPane.setDividerLocation(0.7f);
		splitPane.setResizeWeight(0.7f);
		splitPane.setOneTouchExpandable(true);
		splitPane.setTopComponent(tabbedPane);
		splitPane.setBottomComponent(messagePanel);
		myContentPanel.setLayout(new BorderLayout());
		myContentPanel.add(splitPane, BorderLayout.CENTER);

		//RecordPairFilterDialog
		recordPairList = new RecordPairList(this);
		myContentPanel.add(recordPairList, BorderLayout.EAST);
		toolbar.addThresholds();

		// Other modules, Tools and help menus
		DefaultModuleMenu dmm = new DefaultModuleMenu(modulePanel.getModule(),this);
		dmm.setEnabled(false);
		myMenuBar.add(dmm);
		myMenuBar.add(new ToolsMenu(this));

		JMenu helpMenu = new JMenu(MessageUtil.m.formatMessage("train.gui.modelmaker.menu.help"));
		myMenuBar.add(helpMenu);
		JMenuItem contentsItem = new JMenuItem("Contents");
		JavaHelpUtils.enableHelp(contentsItem, "train.gui.modelmaker");
		helpMenu.add(contentsItem);
		aboutItem = new JMenuItem(MessageUtil.m.formatMessage("train.gui.modelmaker.menu.help.about"));
		helpMenu.add(aboutItem);

		JavaHelpUtils.enableHelpKey(this, "train.gui.modelmaker");
	}

	private void programExit() {
		this.dispose();
		System.exit(0);
	}

	private void addListeners() {

		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				String title = MessageUtil.m.formatMessage("train.gui.modelmaker.title");

				Date d = null;
				try {
					d = new Date(new File(ModelMaker.class.getProtectionDomain().getCodeSource().getLocation().getFile().replaceAll("%20", " ")).lastModified());
				} catch(Exception ex) {
					// DO NOTHING
				}
				String text = MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.about.message", d);

				new AboutDialog(ModelMaker.this, title, text).show();
			}
		});

		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				programExit();
			}
		});
	}

	/**
	 * The usual stuff that a JFrame needs in order to be
	 * displayed.
	 */
	private void display() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		Dimension dim = getToolkit().getScreenSize();
		setSize(dim.width, dim.height - 22);
		//setSize(800, 600-22);
		setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2 - 11);
		setVisible(true);
	}

	public JToolBar getToolBar() {
		return toolbar;
	}

	public void showToolbar(boolean b) {
		toolbar.setVisible(b);
	}

	public void showPairIndices(boolean b) {
		if (b) {
			myContentPanel.add(recordPairList, BorderLayout.EAST);
		} else {
			myContentPanel.remove(recordPairList);
		}
		myContentPanel.validate();
	}

	public void showStatusMessages(boolean b) {
		if (b) {
			splitPane.setBottomComponent(messagePanel);
		} else {
			splitPane.setBottomComponent(null);
		}
	}

	public boolean isSourceDataModified() {
		return sourceDataModified;
	}

	private void setSourceDataModified(boolean v) {
		sourceDataModified = v;
	}

	/**
	 * Accessor used by the HumanReviewPanel in order to call
	 * methods on the testingPanel.
	 *
	 * @return reference to the TestingControlPanel.
	 */
	public TestingControlPanel getTestingControlPanel() {
		return testingPanel;
	}

	public TrainingControlPanel getTrainingControlPanel() {
		return trainingPanel;
	}

	public HumanReviewPanel getHumanReviewPanel() {
		return reviewPanel;
	}

	public void showHumanReviewPanel() {
		tabbedPane.setSelectedComponent(reviewPanel);
	}

	public void showTestingPanel() {
		tabbedPane.setSelectedComponent(testingPanel);
	}

	public DefaultManagedPanel getModulePanel() {
		return modulePanel;
	}

	public void showClusterPanel() {
		tabbedPane.setSelectedComponent(modulePanel);
	}

	public Thresholds getThresholds() {
		return thresholds;
	}

	private void setEvaluated(boolean b) {
		evaluated = b;
		if (evaluated) {
			statistics =
				new Statistics(
					probabilityModel,
					sourceList,
					thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold());
		} else {
			if (!keepAllSourcesInMemory) {
				multiSourceLists[0] = null;
				multiSourceLists[1] = null;
			}
			sourceList = null;
			trainer = null;
			repository.setTrainer(trainer);
			selection = null;
			statistics = null;
		}
		fireEvaluated(new EvaluationEvent(this, evaluated));
		if (evaluated && markedRecordPair < sourceList.size() && sourceList.size() > 0) {
			setMarkedRecordPair(0);
		}
		Runtime.getRuntime().gc();
	}

	public boolean isEvaluated() {
		return evaluated;
	}

	public void setDifferThreshold(float d) {
		setThresholds(new Thresholds(d, getThresholds().getMatchThreshold()));
	}

	public void setMatchThreshold(float m) {
		setThresholds(new Thresholds(getThresholds().getDifferThreshold(), m));
	}

	public void setThresholds(Thresholds t) {
		Thresholds oldValue = thresholds;
		thresholds = t;
		getPreferences().putFloat(PreferenceKeys.DIFFER_THRESHOLD, thresholds.getDifferThreshold());
		getPreferences().putFloat(PreferenceKeys.MATCH_THRESHOLD, thresholds.getMatchThreshold());
		if ((oldValue == null || !oldValue.equals(thresholds)) && trainer != null) {
			trainer.computeDecisions(thresholds.getDifferThreshold(), thresholds.getMatchThreshold());
			statistics.setThresholds(thresholds);
		}
		if(trainer != null) {
			trainer.setLowerThreshold(thresholds.getDifferThreshold());
			trainer.setUpperThreshold(thresholds.getMatchThreshold());
		}
		firePropertyChange(ModelMakerEventNames.THRESHOLDS, oldValue, thresholds);
	}

	//************************************************************************************************
	//**********Probability Model Handling************************************************************
	//************************************************************************************************

	/**
	* Returns true if we have a non-null probability model.  *
	*/
	public boolean haveProbabilityModel() {
		return (probabilityModel != null);
	}

	public void reloadProbabilityModel() {
		try {
			setProbabilityModel(probabilityModel.getFileName(), true);
		} catch (OperationFailedException ex) {
			logger.error(new LoggingObject("CM-100501", probabilityModel.getFileName()), ex);
		}
	}

	/**
	 * Gets the model using the model name, then sets the model.
	 * If a model by the passed name can not be retrieved, an
	 * error is posted and the previously set model is kept as
	 * the active model.
	 *
	 * @param modelName
	 */
	public void setProbabilityModel(String modelName, boolean reload) throws OperationFailedException {
		Writer statusOutput = new StringWriter();
		Cursor cursor = null;
		try {
			cursor = getCursor();
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			File f = new File(modelName);
			InputStream is = new FileInputStream(f);

			CompilerFactory factory = CompilerFactory.getInstance ();
			ICompiler compiler = factory.getDefaultCompiler();

			IProbabilityModel pm = ProbabilityModelsXmlConf.readModel(modelName,is,compiler,statusOutput);
			setProbabilityModel(pm);
		} catch (Exception ex) {
			// AJW 2004-04-26: removed to avoid duplicate error messages.
			//logger.error(new LoggingObject("CM-100502", modelName, ex));
			throw new OperationFailedException(
				MessageUtil.m.formatMessage("train.gui.modelmaker.model.retrieve.error", modelName),
				ex);
		} finally {
			setCursor(cursor);
			messagePanel.postMessage(statusOutput.toString());
		}
	}

	/**
	 * Sets the probability model.  Nulls out the source list and calls
	 * resetEvaluationStatistics on the trainingPanel so that the proper clue
	 * set is displayed.  Sends a modelChanged message to any listeners.
	 *
	 * @param pm     A reference to a PMManager.
	 */
	public void setProbabilityModel(IProbabilityModel pm) {
		Cursor cursor = getCursor();
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		setSourceDataModified(false);
		IProbabilityModel oldModel = probabilityModel;
		if (oldModel != null)
			oldModel.removePropertyChangeListener(probabilityModelEventMultiplexer);
		probabilityModel = pm;
		if (probabilityModel != null)
			probabilityModel.addPropertyChangeListener(probabilityModelEventMultiplexer);
		setTitleMessage();
		postProbabilityModelInfo();
		log4jAppender.addTo(probabilityModel == null ? null : probabilityModel.getAccessor());
		multiSourceLists[0] = null;
		multiSourceLists[1] = null;
		setEvaluated(false);
		if (markedRecordPairSource != null)
			markedRecordPairSource.setModel(probabilityModel);
		firePropertyChange(ModelMakerEventNames.PROBABILITY_MODEL, oldModel, probabilityModel);
		setCursor(cursor);
	}

	/**
	 *
	 * @return A reference to the active PMManager.
	 */
	public IProbabilityModel getProbabilityModel() {
		return probabilityModel;
	}

	public void postProbabilityModelInfo() {
		if (probabilityModel != null) {
			postInfo(MessageUtil.m.formatMessage("train.gui.modelmaker.model.name", probabilityModel.getName()));
			postInfo(
				MessageUtil.m.formatMessage(
					"train.gui.modelmaker.model.clue.file.name",
					probabilityModel.getClueFileName()));
			postInfo(
				MessageUtil.m.formatMessage(
					"train.gui.modelmaker.model.schema.file.name",
					probabilityModel.getAccessor().getSchemaFileName()));
			postInfo(
				MessageUtil.m.formatMessage(
					"train.gui.modelmaker.model.last.train.user",
					probabilityModel.getUserName()));
			postInfo(
				MessageUtil.m.formatMessage(
					"train.gui.modelmaker.model.last.train.date",
					probabilityModel.getLastTrainingDate()));
			postInfo(
				MessageUtil.m.formatMessage(
					"train.gui.modelmaker.model.last.train.source",
					probabilityModel.getTrainingSource()));
			postInfo(probabilityModel.isTrainedWithHolds() ? "Trained with holds" : "Trained without holds");
			postInfo(
				MessageUtil.m.formatMessage(
					"train.gui.modelmaker.model.last.train.firing.threshold",
					new Integer(probabilityModel.getFiringThreshold())));
			MachineLearner ml = probabilityModel.getMachineLearner();
			if(ml != null && !(ml instanceof None)) {
				postInfo(
					MessageUtil.m.formatMessage(
						"train.gui.modelmaker.model.last.train.ml",
						MlGuiFactories.getGui(probabilityModel.getMachineLearner())));
				String modelInfo = ml.getModelInfo();
				if(modelInfo != null) {
					postInfo(modelInfo);
				}
			}
		}
	}

	public ImmutableProbabilityModel getProbabilityModel(String modelName) throws OperationFailedException {
		ImmutableProbabilityModel pm = null;
		Writer statusOutput = new StringWriter();
		try {
			File f = new File(modelName);
			InputStream is = new FileInputStream(f);

			CompilerFactory factory = CompilerFactory.getInstance ();
			ICompiler compiler = factory.getDefaultCompiler();

			pm = ProbabilityModelsXmlConf.readModel(modelName,is,compiler,statusOutput);
		} catch (XmlConfException ex) {
			throw new OperationFailedException(
				MessageUtil.m.formatMessage("train.gui.modelmaker.model.retrieve.error", modelName));
		} catch(FileNotFoundException ex) {
			String msg = "Unable to find '" + modelName + "'";
			throw new OperationFailedException(msg);
		} finally {
			messagePanel.postMessage(statusOutput.toString());
		}
		return pm;
	}

	/**
	 * Saves the probability model to disk.
	 *
	 * @param pm
	 */
	public void saveProbabilityModel(IProbabilityModel pm) throws OperationFailedException {
		try {
			ProbabilityModelsXmlConf.saveModel(pm);
			//logger.info("Saved probability model to disk: " + pm.getName());
			postInfo(MessageUtil.m.formatMessage("train.gui.modelmaker.model.saved", pm.getName()));
		} catch (XmlConfException ex) {
			throw new OperationFailedException(
				MessageUtil.m.formatMessage("train.gui.modelmaker.model.save.error", pm.getName()),
				ex);
		}
	}

	/**
	 * Saves the active probability model to disk.
	 */
	public void saveActiveModel() {
		try {
			saveProbabilityModel(probabilityModel);
		} catch (OperationFailedException ex) {
			logger.error(new LoggingObject("CM-100503", probabilityModel.getFileName()), ex);
		}
	}

	public boolean buildProbabilityModel(IProbabilityModel pm) {
		Cursor cursor = getCursor();
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		multiSourceLists[0] = null;
		multiSourceLists[1] = null;
		setEvaluated(false);
		boolean success = true;
		if (pm.isUseAnt()) {
			success = CallAnt.callAnt(pm.getAntCommand(), messagePanel.getPrintStream());
		}
		if (success) {
			try {
				CompilerFactory factory = CompilerFactory.getInstance ();
				ICompiler compiler = factory.getDefaultCompiler();
				success = compiler.compile(pm, messagePanel.getWriter());
			} catch (CompilerException ex) {
				// TODO FIXME error message for compiler exception
				logger.error(new LoggingObject("TODO FIXME", probabilityModel.getFileName()), ex);
			}
		}
		setCursor(cursor);
		return success;
	}

	//************************************************************************************************
	//**********Marked Record-Pair Source Handling****************************************************
	//************************************************************************************************

	/**
	* Returns true if we have a non-null MRP source.  *
	*/
	public boolean haveMarkedRecordPairSource() {
		return (markedRecordPairSource != null);
	}

	public boolean haveMarkedRecordPairSource(int i) {
		return multiSources[i] != null;
	}

	public void setMultiIncludeHolds(int i, boolean b) {
		if (multiIncludeHolds[i] != b) {
			multiSourceLists[i] = null;
			multiIncludeHolds[i] = b;
			if (usedMultiSource == i) {
				setIncludeHolds(b);
			}
			firePropertyChange(ModelMakerEventNames.MULTI_INCLUDE_HOLDS, null, null);
		}
		setTitleMessage();
	}

	public boolean getMultiIncludeHolds(int i) {
		return multiIncludeHolds[i];
	}

	private void setIncludeHolds(boolean v) {
		if (v != includeHolds) {
			includeHolds = v;
			setEvaluated(false);
			fireMarkedRecordPairDataChange(new RepositoryChangeEvent(this));
		}
	}

	public boolean isIncludeHolds() {
		return includeHolds;
	}

	public MarkedRecordPairSource getMultiSource(int i) {
		return multiSources[i];
	}

	public void setMultiSource(int i, MarkedRecordPairSource s) {
		multiSourceLists[i] = null;
		multiSources[i] = s;
		if (usedMultiSource == i) {
			if(s == null && multiSources[1 - i] != null) {
				usedMultiSource = 1 - i;
				setIncludeHolds(multiIncludeHolds[usedMultiSource]);
				setMarkedRecordPairSource(multiSources[usedMultiSource]);
			} else {
				setMarkedRecordPairSource(s);
			}
		} else if (multiSources[usedMultiSource] == null && s != null) {
			usedMultiSource = i;
			setIncludeHolds(multiIncludeHolds[i]);
			setMarkedRecordPairSource(s);
		}
		setTitleMessage();
	}

	/**
	 * Returns the keepBothSourcesInMemory.
	 * @return boolean
	 */
	public boolean isKeepAllSourcesInMemory() {
		return keepAllSourcesInMemory;
	}

	/**
	 * Sets the keepBothSourcesInMemory.
	 * @param keepBothSourcesInMemory The keepBothSourcesInMemory to set
	 */
	public void setKeepAllSourcesInMemory(boolean keepAllSourcesInMemory) {
		this.keepAllSourcesInMemory = keepAllSourcesInMemory;
		if (!keepAllSourcesInMemory) {
			multiSourceLists[0] = null;
			multiSourceLists[1] = null;
		}
	}

	/**
	 * Sets the trainSourceUsed.
	 * @param trainSourceUsed The trainSourceUsed to set
	 */
	public void setUsedMultiSource(int i) {
		usedMultiSource = i;
	}

	public int getUsedMultiSource() {
		return usedMultiSource;
	}

	public void swapSources() {
		usedMultiSource = 1 - usedMultiSource;
		MarkedRecordPairSource tmp = multiSources[0];
		multiSources[0] = multiSources[1];
		multiSources[1] = tmp;
		if (multiIncludeHolds[0] != multiIncludeHolds[1]) {
			multiSourceLists[0] = null;
			multiSourceLists[1] = null;
			setMultiIncludeHolds(usedMultiSource, multiIncludeHolds[usedMultiSource]);
		} else {
			java.util.List ll = multiSourceLists[0];
			multiSourceLists[0] = multiSourceLists[1];
			multiSourceLists[1] = ll;
		}
		setTitleMessage();
		// AJW 2004-04-26: fire an event so that the MultiSourceMenu items update themselves.
		// passing "null" as the oldValue is a hack so that PropertyChangeSupport actually
		// fires events to listeners (it doesn't if the two are reference equal).
		firePropertyChange(ModelMakerEventNames.MARKED_RECORD_PAIR_SOURCE, null, markedRecordPairSource);
	}

	public MarkedRecordPairSource getMarkedRecordPairSource() {
		return markedRecordPairSource;
	}

	/**
	 * Sets the MRP source.  Nulls out the source list and
	 * resets the panels that contain data that needs to be
	 * recomputed whenever the source changes.
	 *
	 * @param s      A reference to a MarkedRecordPairSource.
	 */
	private void setMarkedRecordPairSource(MarkedRecordPairSource s) {
		MarkedRecordPairSource old = markedRecordPairSource;
		markedRecordPairSource = s;
		setSourceDataModified(false);
		setEvaluated(false);
		if (markedRecordPairSource != null)
			markedRecordPairSource.setModel(probabilityModel);
		setTitleMessage();
		firePropertyChange(ModelMakerEventNames.MARKED_RECORD_PAIR_SOURCE, old, markedRecordPairSource);
	}

	private void setTitleMessage() {
		String message = FRAME_TITLE + " - ";
		if (probabilityModel == null) {
			message += "<no model>";
		} else {
			message += probabilityModel.getName() + ".model";
		}
		message += " - ";
		message += getSourceMessage(0);
		message += " : ";
		message += getSourceMessage(1);
		setTitle(message);
	}

	private String getSourceMessage(int i) {
		String message = "";
		if (i != usedMultiSource) {
			message += "(";
		}
		if (multiSources[i] != null) {
			String fn = multiSources[i].getFileName();
			if (fn != null) {
				message += new File(fn).getName();
			} else {
				message += multiSources[i].getName();
			}
			if (multiIncludeHolds[i]) {
				message += " [with holds]";
			} else {
				message += " [without holds]";
			}
		} else {
			message += "<no source>";
		}
		if (i != usedMultiSource) {
			message += ")";
		}
		return message;
	}

	/**
	 * Uses the MarkedRecordPairBinder to load the source into an
	 * in-memory collection.  This method is called prior to
	 * training, or prior to saving a source to disk.
	 */
	private void loadSourceList() throws OperationFailedException {
		if (sourceList == null) {
			if (multiSourceLists[usedMultiSource] == null) {
				try {
					sourceList = MarkedRecordPairBinder.getList(markedRecordPairSource, includeHolds, repository);
					if (sourceList.size() == 0) {
						sourceList = null;
					}
					multiSourceLists[usedMultiSource] = sourceList;
				} catch (IOException ex) {
					throw new OperationFailedException(
						MessageUtil.m.formatMessage("train.gui.modelmaker.source.list.retrieve.error"),
						ex);
				}
			} else {
				sourceList = multiSourceLists[usedMultiSource];
			}
		}
		checkedList = new IntArrayList();
	}

	public java.util.List getSourceList() {
		return sourceList;
	}

	/**
	 * Returns true if we have a non-null source list.
	 *
	 * @return
	 */
	public boolean haveSourceList() {
		return (sourceList != null);
	}

	public boolean isChecked(int mrpIndex) {
		return checkedList != null && checkedList.contains(mrpIndex);
	}

	public void setChecked(int mrpIndex, boolean checked) {
		if (checkedList != null) {
			if (checked) {
				checkedList.add(mrpIndex);
			} else {
				checkedList.remove(checkedList.indexOf(mrpIndex));
			}
		}
	}

	public void uncheckAll() {
		IntArrayList old = checkedList;
		checkedList = new IntArrayList();
		firePropertyChange(ModelMakerEventNames.CHECKED_INDICES, old, checkedList);
	}

	public void checkAll() {
		IntArrayList old = checkedList;

		int size = sourceList.size();
		IntArrayList newChecked = new IntArrayList(size);
		for (int i = 0; i < size; i++) {
			newChecked.add(i);
		}

		checkedList = newChecked;
		firePropertyChange(ModelMakerEventNames.CHECKED_INDICES, old, checkedList);
	}

	public int[] getCheckedIndices() {
		if (checkedList == null) {
			return new int[0];
		} else {
			return checkedList.toArray();
		}
	}

	public void sortChecked() {
		checkedList.sort();
	}

	public IntArrayList getChecked() {
		return checkedList;
	}

	/**
	 * Saves the active MRPSource to disk.  This method is called
	 * by the HumanReviewPanel to save a source that has been
	 * modified.
	 */
	public void saveMarkedRecordPairSource() {
		try {
			MarkedRecordPairSink snk = (MarkedRecordPairSink) markedRecordPairSource.getSink();
			MarkedRecordPairBinder.store(sourceList, snk);
			logger.info("Re-saved MarkedRecordPairSource: " + markedRecordPairSource.getName());
			setSourceDataModified(false);
		} catch (IOException ex) {
			logger.error(new LoggingObject("CM-100602", markedRecordPairSource.getName()), ex);
		}
	}

	//************************************************************************************************
	//******Clues methods*****************************************************************************
	//************************************************************************************************

	/**
	 * Sets the cluesToEvaluate elements all to true in
	 * probabilityModel.
	 */
	public void setAllCluesOrRules(int what, boolean value) {
		boolean[] cluesEnabled = probabilityModel.getCluesToEvaluate();
		ClueDesc[] cds = probabilityModel.getClueSet().getClueDesc();
		for (int i = 0; i < cluesEnabled.length; i++) {
			if (((what & CLUES) != 0 && !cds[i].rule) || ((what & RULES) != 0 && cds[i].rule)) {
				cluesEnabled[i] = value;
			}
		}
		probabilityModel.changedCluesToEvaluate();
	}

	/**
	 * Resets the weights in the probability model all to 1.
	 */
	public void resetWeights() {
		MachineLearner ml = probabilityModel.getMachineLearner();
//		if (ml instanceof MaximumEntropy) {
//			((MaximumEntropy) ml).resetWeights();
//		}
	}

	/**
	 * Evaluates the clues on the source to get and display
	 * the counts.
	 */
	public void evaluateClues() {
		if (usedMultiSource == 0 && multiSources[1] != null) {
			usedMultiSource = 1;
			setIncludeHolds(multiIncludeHolds[1]);
			setMarkedRecordPairSource(multiSources[1]);
			setTitleMessage();
		}
		long t0 = System.currentTimeMillis();
		final Thread t = new Thread() {
			public void run() {
				try {
					if (!currentThread().isInterrupted()) {
						loadSourceList();
						if (haveSourceList() && !currentThread().isInterrupted()) {
							initializeTrainer();
							if (!currentThread().isInterrupted()) {
								trainer.test();
								if (!currentThread().isInterrupted()) {
									trainer.computeProbabilitiesAndDecisions(
										thresholds.getDifferThreshold(),
										thresholds.getMatchThreshold());
								}
							}
						}
					}
				} catch (OperationFailedException ex) {
					logger.error(new LoggingObject("CM-100801"), ex);
				}
			}
		};
		boolean interrupted =
			ThreadWatcher.watchThread(
				t,
				this,
				MessageUtil.m.formatMessage("train.gui.modelmaker.wait"),
				MessageUtil.m.formatMessage("train.gui.modelmaker.evaluation.evaulate"));
		if (interrupted) {
			postInfo("Clue evaluation cancelled.");
			multiSourceLists[usedMultiSource] = null;
			setEvaluated(false);
		} else {
			long deltaT = System.currentTimeMillis() - t0;
			postInfo(MessageUtil.m.formatMessage("train.gui.modelmaker.evaluation.complete", new Long(deltaT)));
			setEvaluated(haveSourceList());
		}
	}

	public void postClueText(int clueId) {
		try {
			String text = probabilityModel.getClueText(clueId);
			postClue(text);
		} catch (IOException ex) {
			logger.error(new LoggingObject("CM-100802", new Integer(clueId)), ex);
		}
	}

	//************************************************************************************************
	//****Trainer methods ****************************************************************************
	//************************************************************************************************

	private void initializeTrainer() {
		markedRecordPairSource.setModel(getProbabilityModel());
		trainer = new Trainer(thresholds.getDifferThreshold(), thresholds.getMatchThreshold());
		repository.setTrainer(trainer);
		trainer.setModel(probabilityModel);
		trainer.setSource(sourceList);
	}

	/**
	 * Starts the training.  When training is done, updates
	 * the ProbabilityModelChangeListeners and TrainerChangeListeners
	 * so that they can update their data.
	 */
	public boolean train(
		boolean recompile,
		boolean enableAllClues,
		boolean enableAllRules,
		int firingThreshold,
		boolean andTest) {
		//logger.debug("train called.");
		long t0 = System.currentTimeMillis();
		if (usedMultiSource == 1 && multiSources[0] != null) {
			usedMultiSource = 0;
			setIncludeHolds(multiIncludeHolds[0]);
			setMarkedRecordPairSource(multiSources[0]);
			setTitleMessage();
		}
		if (recompile && probabilityModel.needsRecompilation()) {
			boolean success = buildProbabilityModel(probabilityModel);
			if (!success) {
				return false;
			}
		}
		if (firingThreshold < 2) {
			//postError(MessageUtil.m.formatMessage("train.gui.modelmaker.train.firing.threshold.warning"));
			return false;
		}
		probabilityModel.beginMultiPropertyChange();
		probabilityModel.setEnableAllCluesBeforeTraining(enableAllClues);
		probabilityModel.setEnableAllRulesBeforeTraining(enableAllRules);
		setAllCluesOrRules((enableAllClues ? ModelMaker.CLUES : 0) + (enableAllRules ? ModelMaker.RULES : 0), true);
		probabilityModel.setFiringThreshold(firingThreshold);

		final Thread t = new Thread() {
			public void run() {
				try {
					if (!currentThread().isInterrupted()) {
						loadSourceList();
						if (haveSourceList() && !currentThread().isInterrupted()) {
							initializeTrainer();
							// NOTE: to undo this, change "true" to "false".
							Object ret = trainer.train();
							if (true && ret != null) {
								postInfo(ret.toString());
							}
						}
					}
				} catch (OperationFailedException ex) {
					logger.error(new LoggingObject("CM-100801"), ex);
				}
			}
		};
		boolean interrupted =
			ThreadWatcher.watchThread(
				t,
				this,
				MessageUtil.m.formatMessage("train.gui.modelmaker.wait"),
				MessageUtil.m.formatMessage("train.gui.modelmaker.train.train"));
		if (interrupted) {
			postInfo("Training cancelled.");
			probabilityModel.endMultiPropertyChange();
			multiSourceLists[usedMultiSource] = null;
			setEvaluated(false);
			return false;
		} else {
			if (haveSourceList()) {
				probabilityModel.setLastTrainingDate(new java.util.Date());
				probabilityModel.setTrainingSource(markedRecordPairSource.getName());
				probabilityModel.setTrainedWithHolds(isIncludeHolds());
				probabilityModel.setUserName(System.getProperty("user.name"));
				long deltaT = System.currentTimeMillis() - t0;
				postInfo(MessageUtil.m.formatMessage("train.gui.modelmaker.train.complete", new Long(deltaT)));
				probabilityModel.endMultiPropertyChange();
				if (andTest && usedMultiSource == 0 && multiSources[1] != null) {
					evaluateClues();
				} else {
					trainer.computeProbabilitiesAndDecisions(
						thresholds.getDifferThreshold(),
						thresholds.getMatchThreshold());
					setEvaluated(true);
				}
			} else {
				setEvaluated(false);
			}
			return true;
		}
	}

	public Trainer getTrainer() {
		return trainer;
	}

	public Statistics getStatistics() {
		return statistics;
	}

	public Repository getRepository() {
		return repository;
	}

	//************************************************************************************************
	//******Human review methods**********************************************************************
	//************************************************************************************************

	/*  Method called by the testingPanel or the reviewPanel when a particular
	 *  MRP is selected for reviewing.
	 */
	public void setMarkedRecordPair(int p) {
		markedRecordPair = p;
		firePropertyChange(ModelMakerEventNames.MARKED_RECORD_PAIR, null /* new Integer(oldValue) */
		, new Integer(markedRecordPair));
	}

	public int getMarkedRecordPair() {
		return markedRecordPair;
	}

	/**
	 * This method is called by the ClueTableCellListener which is
	 * attached to the clue table in the TestingPanel.  It allows
	 * one to step through the MRPs associated with a given
	 * clue.
	 *
	 * @param clueID
	 * @param fireType
	 */
	public void updateRecordPairList(int clueID, int fireType) {
		ClueDesc cd = probabilityModel.getAccessor().getClueSet().getClueDesc()[clueID];
		if (cd.rule) {
			if (fireType == ClueTableModel.COL_TOTAL_FIRES) {
				filter.reset();
				filter.setConditions(
					new FilterCondition[] { new RuleFilterCondition(clueID, BooleanFilterCondition.ACTIVE)});
				filterMarkedRecordPairList();
			}
		} else if (cd.decision.toInt() < Decision.NUM_DECISIONS) {
			filter.reset();
			//			filter.setActiveClues(new int[] { clueID });
			filter.setConditions(
				new BooleanFilterCondition[] { new BooleanFilterCondition(clueID, BooleanFilterCondition.ACTIVE)});
			//                                            this argument represents an ActiveClue -----^
			if (fireType != ClueTableModel.COL_TOTAL_FIRES) {
				Decision d = cd.decision;
				boolean[] b;
				if (fireType == ClueTableModel.COL_CORRECT_FIRES) {
					b = new boolean[Decision.NUM_DECISIONS];
					b[d.toInt()] = true;
				} else {
					b = ArrayHelper.getTrueArray(Decision.NUM_DECISIONS);
					b[d.toInt()] = false;
				}
				filter.setHumanDecision(b);
			}
			filterMarkedRecordPairList();
		}
	}

	public void displayRecordPairFilterDialog() {
		new RecordPairFilterDialog(this).show();
	}

	public void filterMarkedRecordPairList() {
		selection = filter.filterSource(sourceList);
		reviewPanel.setSelectionSize(selection.length);
		recordPairList.updateRecordPairList(selection);
	}

	public int[] getSelection() {
		return selection;
	}

	public ListeningMarkedRecordPairFilter getFilter() {
		return filter;
	}

	public void setFilter(ListeningMarkedRecordPairFilter filter) {
		this.filter = filter;
		filterMarkedRecordPairList();
	}

	//************************ Data Methods

	/**
	 * Method called from the HumanReviewPanel when a record's
	 * data have been modified by hand.  This method recomputes
	 * the probability on the modified record pair.
	 *
	 * @param index Index of the MRP in the source list.
	 */
	public void dataModified() {
		int index = getMarkedRecordPair();
		ImmutableMarkedRecordPair r = (ImmutableMarkedRecordPair) sourceList.get(index);
	//	trainer.computeProbability(r, thresholds.getDifferThreshold(), thresholds.getMatchThreshold());
		setMarkedRecordPair(index);
		fireMarkedRecordPairDataChange(new RepositoryChangeEvent(this, null));
	}

	//*************************

	/**
	 * Since the HumanReviewPanel and the TestingControlPanel do
	 * not communicate directly, this method allows one to click
	 * a button on the review Panel to select the next MRP to
	 * be displayed from the list shown on the testing panel.
	 */
	public void reviewNextMarkedRecordPair() {
		recordPairList.reviewNextMarkedRecordPair();
	}

	/**
	 * Since the HumanReviewPanel and the TestingControlPanel do
	 * not communicate directly, this method allows one to click
	 * a button on the review Panel to select the previous MRP to
	 * be displayed from the list shown on the testing panel.
	 */
	public void reviewPreviousMarkedRecordPair() {
		recordPairList.reviewPreviousMarkedRecordPair();
	}

	public RecordPairList getRecordPairList() {
		return recordPairList;
	}

	//******************************************************************************
	//******************************************************************************
	//******************************************************************************

	/**
	 * Posts Clue text to the MessagePanel.
	 */
	public void postClue(String s) {
		messagePanel.postMessage(Constants.LINE_SEPARATOR + s + Constants.LINE_SEPARATOR);
	}

	/**
	 * Posts Info message to the MessagePanel.
	 */
	private void postInfo(String s) {
//		String displayString =
//			MessageUtil.m.formatMessage("train.gui.modelmaker.message.info", s) + Constants.LINE_SEPARATOR;
//		messagePanel.postMessage(displayString);
		this.getUserMessages().postInfo(s);
	}

	/**
	 * Posts Warning message to the MessagePanel.
	 */
	//public void postWarning(String s) {
	//	String displayString =
	//		MessageUtil.m.formatMessage("train.gui.modelmaker.message.warning", s) + Constants.LINE_SEPARATOR;
	//	messagePanel.postMessage(displayString);
	//}

	/**
	 * Posts Error message to the MessagePanel.
	 */
	//public void postError(String s) {
	//	String displayString =
	//		MessageUtil.m.formatMessage("train.gui.modelmaker.message.error", s) + Constants.LINE_SEPARATOR;
	//	messagePanel.postMessage(displayString);
	//}

	/**
	 * Posts FatalError message to the MessagePanel.
	 */
	//public void postFatalError(String s) {
	//	String displayString =
	//		MessageUtil.m.formatMessage("train.gui.modelmaker.message.fatal.error", s) + Constants.LINE_SEPARATOR;
	//	messagePanel.clearMessages();
	//	messagePanel.postMessage(displayString);
	//}

	public MessagePanel getMessagePanel() {
		return messagePanel;
	}

	static {
		ChoiceMakerMetalTheme.init();
	}

	public static void processLicenseFile() {
		// 2009-07-06 rphall
		// Removed license validation for open-source release
	} // processLicenseFile()

	private static String CONF_ARG = "-conf";
	private static String LOG_ARG = "-log";
	private static String DIALOG_OPT = "-dialog";

	public Object run(Object args2) throws Exception {
		String[] args = null;
		if(args2 instanceof String[]) {
			args = (String[]) args2;
		}
		// defineFrameLookAndFeel();
		processLicenseFile();

		// Must skip unknown arguments, since this method will be passed
		// (unknown) Eclipse arguments as well as ModelMaker arguments
		Arguments ca = new Arguments(Arguments.SKIP_UNKNOWN);

		ca.addArgument(CONF_ARG, "");
		ca.addArgument(LOG_ARG, Arguments.DEFAULT);
		ca.addOption(DIALOG_OPT);
		ca.enter(args);
		String conf = ca.argumentVal(CONF_ARG);
		String log = ca.argumentVal(LOG_ARG);
		if (ca.optionSet(DIALOG_OPT) || !checkValidity(conf)) {
			if (conf == "") {
				conf = getPreferences().get(PreferenceKeys.CONFIGURATION_FILE, PreferenceDefaults.CONFIGURATION_FILE);
			}
			StartDialog sd = new StartDialog(conf);
			sd.setVisible(true);
			try {
				synchronized (ModelMaker.class) {
					ModelMaker.class.wait();
				}
			} catch (InterruptedException ex) {
				// do nothing
			}
			if (!sd.isRes()) {
				System.exit(0);
			}
			conf = sd.getConfigurationFileName();
			getPreferences().put("configurationfile", conf);
		}
		try {
			XmlConfigurator.init(conf, log, true, true);
			// create
			init();
			buildComponents();
			addListeners();
			display();
			setTitleMessage();
		} catch (XmlConfException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(
				null,
				MessageUtil.m.formatMessage("train.gui.modelmaker.configurationfile.invalid.error", conf),
				MessageUtil.m.formatMessage("error"),
				JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		Object o = new Object();
		while(true) {
			synchronized(o) {
				o.wait();
			}
		}
		//return new Integer(0);
	}

	public static void main(String[] args) throws IOException {
		//defineFrameLookAndFeel();
		processLicenseFile();
		Arguments ca = new Arguments();
		ca.addArgument(CONF_ARG, "");
		ca.addArgument(LOG_ARG, Arguments.DEFAULT);
		ca.addOption(DIALOG_OPT);
		ca.enter(args);
		String conf = ca.argumentVal(CONF_ARG);
		String log = ca.argumentVal(LOG_ARG);
		if (ca.optionSet(DIALOG_OPT) || !checkValidity(conf)) {
			if (conf == "") {
				conf = getPreferences().get(PreferenceKeys.CONFIGURATION_FILE, PreferenceDefaults.CONFIGURATION_FILE);
			}
			StartDialog sd = new StartDialog(conf);
			sd.setVisible(true);
			try {
				synchronized (ModelMaker.class) {
					ModelMaker.class.wait();
				}
			} catch (InterruptedException ex) {
				// do nothing
			}
			if (!sd.isRes()) {
				System.exit(0);
			}
			conf = sd.getConfigurationFileName();
			getPreferences().put("configurationfile", conf);
		}
		try {
			XmlConfigurator.init(conf, log, true, true);
			// create
			new ModelMaker();
		} catch (XmlConfException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(
				null,
				MessageUtil.m.formatMessage("train.gui.modelmaker.configurationfile.invalid.error", conf),
				MessageUtil.m.formatMessage("error"),
				JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	public static boolean checkValidity(String conf) {
		return new File(conf).getAbsoluteFile().isFile();
	}

}
