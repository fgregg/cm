package com.choicemaker.cm.args;

import java.io.Serializable;

/**
 * A type of the graph topology that can be used for identifying when a set of
 * linked records represents represents a single entity.
 * <p>
 *
 * @author emoussikaev
 * @version Revision: 2.5 Date: Nov 1, 2005 1:12:25 PM
 */
public interface IGraphProperty extends Serializable {

	String getName();

}
