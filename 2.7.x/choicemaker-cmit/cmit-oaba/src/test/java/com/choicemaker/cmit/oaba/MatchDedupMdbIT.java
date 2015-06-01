package com.choicemaker.cmit.oaba;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.runner.RunWith;

import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;

/**
 * This class extends {@link MatchDedupMdbProcessing} by adding an Arquillian
 * shrink-wrap method and an Arquillian <code>RunWith</code> directive.
 * 
 * @author rphall
 *
 */
@RunWith(Arquillian.class)
public class MatchDedupMdbIT extends MatchDedupMdbProcessing {

	public static final boolean TESTS_AS_EJB_MODULE = false;

	/**
	 * Creates an EAR deployment.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = { };
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

}
