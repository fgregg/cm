package com.choicemaker.cm.compiler.gen;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The ValidConverter class is difficult to test, because of the usual reasons
 * <ul>
 * </ul>
 * 
 * @author rphall
 *
 */
public class ValidConverterTest extends TestCase {

	public static final String TEST_XML_DATA =
		"/com/choicemaker/cm/compiler/gen/valid_converter--test_data.xml";

	public static final String ROOT_ELEMENT = "ValidConverterExamples";
	public static final String EXAMPLE_ELEMENT = "ValidConverter";
	public static final String INPUT_ELEMENT = "input";
	public static final String OUTPUT_ELEMENT = "output";

	private Map testData;

	public void setUp() {
		InputStream is = null;
		try {
			is = ValidConverterTest.class.getResourceAsStream(TEST_XML_DATA);
			assertTrue(is != null);
			XMLReader xmlReader =
				SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			TestDataParser dataParser = new TestDataParser();
			xmlReader.setContentHandler(dataParser);
			InputSource in = new InputSource(is);
			xmlReader.parse(in);
			testData = dataParser.getTestData();
			assertTrue(testData != null);
			assertTrue(!testData.isEmpty());
		} catch (Exception e1) {
			fail(e1.toString());
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					fail(e.toString());
				}
				is = null;
			}
		}
	}

	public void tearDown() {
		if (testData != null) {
			testData = null;
		}
	}

	public void testConvertValids() {
		assertTrue(testData != null);
		assertTrue(!testData.isEmpty());
		for (Iterator it = testData.entrySet().iterator(); it.hasNext();) {
			Entry e = (Entry) it.next();
			final String input = (String) e.getKey();
			assertTrue(input != null);
			final String expectedOutput = normalize((String) e.getValue());
			assertTrue(expectedOutput != null);
			String oldOutput = normalize(Perl5ValidConverter.convertValids(input));
			assertTrue(expectedOutput.equals(oldOutput));
			String newOutput = normalize(ValidConverter.convertValids(input));
			assertTrue(expectedOutput.equals(newOutput));
		}
	}
	
	private static final Pattern WHITE_SPACE = Pattern.compile("\\s+");
	
	// XML data may contain superficial differences in white space.
	// This method is a hack that simply removes all whites space.
	String normalize(String x) {
		Matcher matcher = WHITE_SPACE.matcher(x);
		String retVal = matcher.replaceAll("");
		return retVal;
	}

	private static class TestDataParser extends DefaultHandler {
		private final Map examples = new HashMap();
		private boolean isInput;
		private StringBuilder currentInput;
		private boolean isOutput;
		private StringBuilder currentOutput;

		public Map getTestData() {
			return Collections.unmodifiableMap(examples);
		}

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			qName = qName.intern();
			if (qName == ROOT_ELEMENT) {
				examples.clear();
				isInput = false;
				currentInput = null;
				isOutput = false;
				currentOutput = null;
			} else if (qName == EXAMPLE_ELEMENT) {
				isInput = false;
				currentInput = new StringBuilder();
				isOutput = false;
				currentOutput = new StringBuilder();
			} else if (qName == INPUT_ELEMENT) {
				isInput = true;
				isOutput = false;
			} else if (qName == OUTPUT_ELEMENT) {
				isInput = false;
				isOutput = true;
			}
		}

		public void characters(char[] ch, int start, int length) {
			if (isInput) {
				String s = new String(ch, start, length);
				currentInput.append(s);
			} else if (isOutput) {
				String s = new String(ch, start, length);
				currentOutput.append(s);
			}
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			qName = qName.intern();
			if (qName == EXAMPLE_ELEMENT) {
				if (currentInput != null && currentOutput != null) {

					String input = currentInput.toString();
					String output = currentOutput.toString();
					examples.put(input, output);

					isInput = false;
					currentInput = null;
					isOutput = false;
					currentOutput = null;
				}
			}
		}
	}

}
