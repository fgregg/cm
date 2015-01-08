package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.util.List;

import javax.ejb.Local;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.core.ISerializableRecordSource;

@Local
public interface RecordSourceController {

	PersistableRecordSource save(PersistableRecordSource rs);

	PersistableRecordSource find(Long id, String type);

	ISerializableRecordSource getRecordSource(Long id, String type)
			throws Exception;
	
	ISerializableRecordSource getStageRs(OabaParameters params)
			throws Exception;

	ISerializableRecordSource getMasterRs(OabaParameters params)
			throws Exception;

	List<PersistableRecordSource> findAll();

}