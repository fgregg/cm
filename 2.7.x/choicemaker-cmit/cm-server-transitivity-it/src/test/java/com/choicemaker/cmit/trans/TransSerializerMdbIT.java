package com.choicemaker.cmit.trans;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.runner.RunWith;

import com.choicemaker.cmit.trans.util.TransitivityDeploymentUtils;

/**
 * This class extends {@link TransSerializerMdbProcessing} by adding an
 * Arquillian shrink-wrap method and an Arquillian <code>RunWith</code>
 * directive.
 * 
 * @author rphall
 *
 */
@RunWith(Arquillian.class)
public class TransSerializerMdbIT extends TransSerializerMdbProcessing {

	public static final boolean TESTS_AS_EJB_MODULE = false;

	/**
	 * Creates an EAR deployment.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = {};
		return TransitivityDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

}
