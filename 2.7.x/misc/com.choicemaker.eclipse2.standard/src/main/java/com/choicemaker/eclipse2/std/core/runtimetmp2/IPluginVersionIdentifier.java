package com.choicemaker.eclipse2.std.core.runtime;

public interface IPluginVersionIdentifier {

	/**
	 * Returns the major (incompatible) component of this 
	 * version identifier.
	 *
	 * @return the major version
	 */
	int getMajorComponent();

	/**
	 * Returns the minor (compatible) component of this 
	 * version identifier.
	 *
	 * @return the minor version
	 */
	int getMinorComponent();

	/**
	 * Returns the service level component of this 
	 * version identifier.
	 *
	 * @return the service level
	 */
	int getServiceComponent();

	/**
	 * Returns the qualifier component of this 
	 * version identifier.
	 *
	 * @return the qualifier
	 */
	String getQualifierComponent();

	/**
	 * Compares two version identifiers to see if this one is
	 * greater than or equal to the argument.
	 * <p>
	 * A version identifier is considered to be greater than or equal
	 * if its major component is greater than the argument major 
	 * component, or the major components are equal and its minor component
	 * is greater than the argument minor component, or the
	 * major and minor components are equal and its service component is
	 * greater than the argument service component, or the major, minor and
	 * service components are equal and the qualifier component is
	 * greated than the argument qualifier component (using lexicographic
	 * string comparison), or all components are equal.
	 * </p>
	 *
	 * @param versionId the other version identifier
	 * @return <code>true</code> is this version identifier
	 *    is compatible with the given version identifier, and
	 *    <code>false</code> otherwise
	 * @since 2.0
	 */
	boolean isGreaterOrEqualTo(IPluginVersionIdentifier id);

	/**
	 * Compares two version identifiers for compatibility.
	 * <p>
	 * A version identifier is considered to be compatible if its major 
	 * component equals to the argument major component, and its minor component
	 * is greater than or equal to the argument minor component.
	 * If the minor components are equal, than the service level of the
	 * version identifier must be greater than or equal to the service level
	 * of the argument identifier. If the service levels are equal, the two 
	 * version identifiers are considered to be equivalent if this qualifier is 
	 * greated or equal to the qualifier of the argument (using lexicographic
	 * string comparison).
	 * </p>
	 *
	 * @param versionId the other version identifier
	 * @return <code>true</code> is this version identifier
	 *    is compatible with the given version identifier, and
	 *    <code>false</code> otherwise
	 */
	boolean isCompatibleWith(IPluginVersionIdentifier id);

	/**
	 * Compares two version identifiers for equivalency.
	 * <p>
	 * Two version identifiers are considered to be equivalent if their major 
	 * and minor component equal and are at least at the same service level 
	 * as the argument. If the service levels are equal, the two version
	 * identifiers are considered to be equivalent if this qualifier is 
	 * greated or equal to the qualifier of the argument (using lexicographic
	 * string comparison).
	 * 
	 * </p>
	 *
	 * @param versionId the other version identifier
	 * @return <code>true</code> is this version identifier
	 *    is equivalent to the given version identifier, and
	 *    <code>false</code> otherwise
	 */
	boolean isEquivalentTo(IPluginVersionIdentifier id);

	/**
	 * Compares two version identifiers for perfect equality.
	 * <p>
	 * Two version identifiers are considered to be perfectly equal if their
	 * major, minor, service and qualifier components are equal
	 * </p>
	 *
	 * @param versionId the other version identifier
	 * @return <code>true</code> is this version identifier
	 *    is perfectly equal to the given version identifier, and
	 *    <code>false</code> otherwise
	 * @since 2.0
	 */
	boolean isPerfect(IPluginVersionIdentifier id);

	/**
	 * Compares two version identifiers for order using multi-decimal
	 * comparison. 
	 *
	 * @param versionId the other version identifier
	 * @return <code>true</code> is this version identifier
	 *    is greater than the given version identifier, and
	 *    <code>false</code> otherwise
	 */
	boolean isGreaterThan(IPluginVersionIdentifier id);

}