package com.choicemaker.cm.args;

public interface PersistableSqlRecordSource extends PersistableRecordSource {

	String TYPE = "SQL";

	/**
	 * Returns the fully qualified class name of an implementation of DbReader
	 *
	 * @return the FQCN of an implementation of
	 *         {@link com.choicemaker.cm.io.db.base.DbReader DbReader},
	 *         {@link com.choicemaker.cm.io.db.base.DbReaderSequential
	 *         DbReaderSequential}, or
	 *         {@link com.choicemaker.cm.io.db.base.DbReaderParallel
	 *         DbReaderParallel}
	 */
	String getDatabaseReader();

	/**
	 * Returns the JNDI name of a data source
	 *
	 * @return a non-null, valid JDNI reference
	 */
	String getDataSource();

	/**
	 * Returns a SQL Select statement used to pull records from the data source.
	 * Must be valid SQL that defines an <code>ID</code> output column.
	 *
	 * <pre>
	 * SELECT &lt;some column&gt; AS ID from &lt;some table&gt;
	 *  &lt;optional WHERE clause&gt;
	 * </pre>
	 *
	 * @return a non-null, valid statement
	 */
	String getSqlSelectStatement();

	/**
	 * Returns the name of a ChoiceMaker model.
	 *
	 * @return a non-null, valid name
	 */
	String getModelId();

	/**
	 * Returns the name of a database configuration defined by the model
	 *
	 * @return non-null, valid name of some configuration defined by the model
	 */
	String getDatabaseConfiguration();

	/**
	 * Returns the plugin identifier for an implementation of the
	 * DatabaseAccessor interface
	 *
	 * @return may be null
	 */
	String getDatabaseAccessor();

}
