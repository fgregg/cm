package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaPairResultJPA.DV_ABSTRACT;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaPairResultJPA.JPQL_PAIRRESULTINTEGER_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaPairResultJPA.JPQL_PAIRRESULTINTEGER_FIND_BY_JOBID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaPairResultJPA.QN_PAIRRESULTINTEGER_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaPairResultJPA.QN_PAIRRESULTINTEGER_FIND_BY_JOBID;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;

@NamedQueries({
		@NamedQuery(name = QN_PAIRRESULTINTEGER_FIND_ALL,
				query = JPQL_PAIRRESULTINTEGER_FIND_ALL),
		@NamedQuery(name = QN_PAIRRESULTINTEGER_FIND_BY_JOBID,
				query = JPQL_PAIRRESULTINTEGER_FIND_BY_JOBID) })
@Entity
@DiscriminatorValue(DV_ABSTRACT)
public class OabaPairResultInteger extends AbstractPairResultEntity<Integer> {

	private static final long serialVersionUID = 271L;

	public static Integer computeIdFromString(String s) {
		Integer retVal = null;
		if (s != null && !s.trim().isEmpty()) {
			retVal = Integer.valueOf(s);
		}
		return retVal;
	}

	public static String exportIdToString(Integer id) {
		String retVal = null;
		if (id != null) {
			retVal = id.toString();
		}
		return retVal;
	}

	public OabaPairResultInteger(OabaJob job, Integer record1Id,
			Integer record2Id, RECORD_SOURCE_ROLE record2Role, float p,
			Decision d, String[] notes) {
		super(job.getId(), RECORD_ID_TYPE.TYPE_INTEGER.getCharSymbol(),
				exportIdToString(record1Id), exportIdToString(record2Id),
				record2Role.getCharSymbol(), p, d.toSingleChar(), notes, null);
	}

	@Override
	protected Integer idFromString(String s) {
		return computeIdFromString(s);
	}

	@Override
	protected String idToString(Integer id) {
		return exportIdToString(id);
	}

}
