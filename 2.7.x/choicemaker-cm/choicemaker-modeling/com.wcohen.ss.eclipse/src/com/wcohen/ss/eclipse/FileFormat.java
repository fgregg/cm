/*
 * Created on Jun 15, 2007
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.wcohen.ss.eclipse;

/**
 * Persisted via Java serialization
 * @author rphall
 */
public class FileFormat {
	
	private final String name;
	
	private FileFormat(String _name) {
		this.name = _name;
	}

	public static final String NAME_BINARY = "binary";
	
	/** Persisted via Java binary serialization */
	public static final FileFormat BINARY = new FileFormat(NAME_BINARY);
	
	public static final String NAME_BETWIXT = "betwixt";
	
	/** Persisted via Betwixt XML serialization */
	public static final FileFormat BETWIXT = new FileFormat(NAME_BETWIXT);

	public static FileFormat getInstance(String name) {
		if (NAME_BINARY.equalsIgnoreCase(name)) {
			return BINARY;
		} else if (NAME_BETWIXT.equalsIgnoreCase(name)) {
			return BETWIXT;
		} else {
			throw new RuntimeException("Unknown type: '" + name + "'");
		}
	}
	
	public String getName() {
		return NAME_BINARY;
	}
	
	public boolean equals(Object o) {
		boolean retVal = (o instanceof FileFormat)
			&& ((FileFormat)o).getName().equalsIgnoreCase(this.getName());
		return retVal;
	}
	
	public int hashCode() {
		return this.name.hashCode();
	}
	
}


