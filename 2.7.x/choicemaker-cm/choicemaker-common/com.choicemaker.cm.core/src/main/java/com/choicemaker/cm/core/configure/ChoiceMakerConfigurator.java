package com.choicemaker.cm.core.configure;

import com.choicemaker.cm.core.XmlConfException;

public interface ChoiceMakerConfigurator {

	/**
	 * Name of a public, static, final field referencing a non-null
	 * ChoiceMakerConfigurator instance
	 */
	public static final String INSTANCE = "instance";

	/**
	 * Creates and initializes a ChoiceMaker configuration using default values.
	 * The default values are obtained by methods that are dependent on the
	 * operating environment, and thus are specified by particular
	 * implementations of this interface.
	 *
	 * @throws XmlConfException
	 *             if a valid configuration can not be created and initialized
	 */
	ChoiceMakerConfiguration init() throws XmlConfException;

	/**
	 * Creates and initializes a ChoiceMaker configuration.
	 *
	 * @param fn
	 *            path to an XML configuration file
	 * @param reload
	 * @param initGui
	 * @return A non-null, valid ChoiceMaker configuration
	 * @throws XmlConfException
	 *             if a valid configuration can not be created and initialized
	 */
	ChoiceMakerConfiguration init(String fn, boolean reload, boolean initGui)
			throws XmlConfException;

	/**
	 * For backward compatibility with ChoiceMaker 2.3. The
	 * <code>logConfName</code> argument is ignored and should be null.
	 * Equivalent to invoking:
	 *
	 * <pre>
	 * init(fn, reload, initGui)
	 * </pre>
	 *
	 * @param fn
	 *            path to an XML configuration file
	 * @param logConfName
	 *            ignored with a warning message
	 * @param reload
	 * @param initGui
	 * @return A non-null, valid ChoiceMaker configuration
	 * @throws XmlConfException
	 *             if a valid configuration can not be created and initialized
	 */
	ChoiceMakerConfiguration init(String fn, String logConfName,
			boolean reload, boolean initGui) throws XmlConfException;

}
