package com.choicemaker.eclipse2.ejb;

import javax.ejb.Local;

import com.choicemaker.eclipse2.core.runtime.CMPlatform;

/**
 * A singleton implementation that uses an embedded Eclipse2 implementation to implement
 * CMPlatform methods.
 *
 * @author rphall
 *
 */
@Local
public interface Eclipse2Service extends CMPlatform {
}
