package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import javax.ejb.Local;

import com.choicemaker.cm.args.RecordAccess;

@Local
public interface RecordAccessController {

	RecordAccess save(RecordAccess ra);

	RecordAccess find(Long id);

}