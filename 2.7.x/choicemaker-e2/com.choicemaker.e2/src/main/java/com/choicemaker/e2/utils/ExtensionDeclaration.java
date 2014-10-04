package com.choicemaker.e2.utils;

import com.choicemaker.e2.CMExtension;

/**
 * A utility class used in testing the extensions declared by a plugin. Aside
 * from this narrow purpose, this limited class isn't likely to be useful
 * elsewhere.
 * 
 * @author rphall
 */
public class ExtensionDeclaration {
	public final String extensionId;
	public final String extensionPoint;

	public ExtensionDeclaration(String id, String pt) {
		this.extensionId = id;
		this.extensionPoint = pt;
	}

	public ExtensionDeclaration(CMExtension cme) {
		this.extensionId = cme.getUniqueIdentifier();
		this.extensionPoint = cme.getExtensionPointUniqueIdentifier();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result =
			prime * result
					+ ((extensionId == null) ? 0 : extensionId.hashCode());
		result =
			prime
					* result
					+ ((extensionPoint == null) ? 0 : extensionPoint.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ExtensionDeclaration other = (ExtensionDeclaration) obj;
		if (extensionId == null) {
			if (other.extensionId != null) {
				return false;
			}
		} else if (!extensionId.equals(other.extensionId)) {
			return false;
		}
		if (extensionPoint == null) {
			if (other.extensionPoint != null) {
				return false;
			}
		} else if (!extensionPoint.equals(other.extensionPoint)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ExtensionDeclaration [extensionId=" + extensionId
				+ ", extensionPoint=" + extensionPoint + "]";
	}
}