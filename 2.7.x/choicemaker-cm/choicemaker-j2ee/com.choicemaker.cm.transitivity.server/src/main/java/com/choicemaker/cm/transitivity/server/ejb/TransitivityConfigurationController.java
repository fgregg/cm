package com.choicemaker.cm.transitivity.server.ejb;

import javax.ejb.Local;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;

/**
 * Manages a database of server configurations.
 * 
 * @author rphall
 *
 */
@Local
public interface TransitivityConfigurationController extends ServerConfigurationController {

	ServerConfiguration findConfigurationByTransitivityJobId(long jobId);

}
