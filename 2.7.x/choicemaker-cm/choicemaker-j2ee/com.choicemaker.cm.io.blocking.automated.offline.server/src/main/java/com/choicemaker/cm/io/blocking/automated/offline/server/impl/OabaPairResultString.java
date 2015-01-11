package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaPairResultJPA.*;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;

@NamedQueries({
	@NamedQuery(name = QN_PAIRRESULTSTRING_FIND_ALL, query = JPQL_PAIRRESULTSTRING_FIND_ALL),
	@NamedQuery(name = QN_PAIRRESULTSTRING_FIND_BY_JOBID,
			query = JPQL_PAIRRESULTSTRING_FIND_BY_JOBID) })
@Entity
@DiscriminatorValue(DV_ABSTRACT)
public class OabaPairResultString extends AbstractPairResultEntity<String> {

	private static final long serialVersionUID = 271L;

	public OabaPairResultString(OabaJob job, String id1, String id2,
			RECORD_SOURCE_ROLE record2Role, float p, Decision d, String[] notes) {
		super(job.getId(), RECORD_ID_TYPE.TYPE_INTEGER.getCharSymbol(), id1,
				id2, record2Role.getCharSymbol(), p, d.toSingleChar(), notes,
				null);
	}

	@Override
	protected String idFromString(String s) {
		return s;
	}

	@Override
	protected String idToString(String id) {
		return id;
	}

}
