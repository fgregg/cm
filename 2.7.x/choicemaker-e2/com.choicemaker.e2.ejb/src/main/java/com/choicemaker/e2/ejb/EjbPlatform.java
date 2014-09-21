package com.choicemaker.e2.ejb;

import javax.ejb.Local;

import com.choicemaker.e2.CMPlatform;

/**
 * A singleton implementation that uses an embedded Eclipse2 implementation to implement
 * CMPlatform methods.
 *
 * @author rphall
 *
 */
@Local
public interface EjbPlatform extends CMPlatform {
}
