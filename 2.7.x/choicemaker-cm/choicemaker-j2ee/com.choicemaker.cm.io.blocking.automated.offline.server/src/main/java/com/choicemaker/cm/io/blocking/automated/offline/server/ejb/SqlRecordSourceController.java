package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.util.List;

import javax.ejb.Local;
import javax.sql.DataSource;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.PersistableSqlRecordSource;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ISerializableRecordSource;

/**
 * Manages a database of SQL record-source configurations.
 * 
 * @author rphall
 *
 */
@Local
public interface SqlRecordSourceController {

	// @Override
	PersistableSqlRecordSource save(PersistableRecordSource rs);

	// @Override
	PersistableSqlRecordSource find(Long id, String type);

	// @Override
	ISerializableRecordSource getRecordSource(Long rsId, String type)
			throws Exception;

	// @Override
	List<PersistableRecordSource> findAll();

	DataSource getStageDataSource(OabaParameters params)
			throws BlockingException;

	DataSource getMasterDataSource(OabaParameters params)
			throws BlockingException;

	DataSource getDataSource(Long id) throws BlockingException;

	DataSource getDataSource(String jndiName) throws BlockingException;

}