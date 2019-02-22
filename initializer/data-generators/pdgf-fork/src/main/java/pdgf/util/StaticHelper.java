/*******************************************************************************
 * Copyright (c) 2011, Chair of Distributed Information Systems, University of Passau. 
 * All rights reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, 
 *     this list of conditions and the following disclaimer. 
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *     notice, this list of conditions and the following disclaimer in the 
 *     documentation and/or other materials provided with the distribution. 
 * 
 * 3. Neither the name of the University of Passau nor the names of its 
 *     contributors may be used to endorse or promote products derived 
 *     from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH 
 * DAMAGE.
 ******************************************************************************/
package pdgf.util;

import java.io.File;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pdgf.Controller;
import pdgf.core.Element;
import pdgf.core.exceptions.XmlException;

/**
 * 
 * Static helper functions of this project
 * 
 * @author Michael Frank
 * @version 1.0 10.12.2009
 */
public class StaticHelper {
	private static final long MAX_INT_PRIME = Integer.MAX_VALUE; // 2147483647;
	private static final BigInteger BL_PRIME = new BigInteger(
			"9223372036854775783"/* "922337203657" */);

	long foo = Long.MAX_VALUE;
	public static final String VERSION = Controller.class.getPackage()
			.getImplementationVersion() != null ? Controller.class.getPackage()
			.getImplementationVersion() : Constants.NON_BUILD_VERSION;
	private static String types = "java.sql.Types.";
	private static long PERMUTATION_LONG_MAX = (long) Math.floor(Math
			.sqrt(Long.MAX_VALUE / 2));

	/**
	 * Returns the SQL TYPE for a string for example if text is "VARCHAR"
	 * java.sql.Types.VARCHAR is returned
	 * 
	 * @param text
	 * @return a static constant of java.sql.Types or NULL if this constant does
	 *         not exist
	 */
	public static Integer getSQLType(String text) {

		// if full path is given: reduce to type name
		if (text.contains(".")
				&& text.substring(0, types.length()).equalsIgnoreCase(types)) {
			text = text.substring(types.length(), text.length());
		}

		// check needed for case: path was not "java.sql.types"
		if (!text.contains(".")) {
			try {
				java.lang.reflect.Field f = java.sql.Types.class.getField(text);
				return f.getInt(f);
			} catch (IllegalArgumentException e) {
				// only return null;
			} catch (IllegalAccessException e) {
				// only return null;
			} catch (SecurityException e) {
				// only return null;
			} catch (NoSuchFieldException e) {
				// only return null;
			}
		}
		return null;
	}

	public static boolean needSingleQuote(String type) {
		return needSingleQuote(getSQLType(type));
	}

	public static boolean needSingleQuote(int type) {
		switch (type) {
		case java.sql.Types.ARRAY:// 2003
			return true;
		case java.sql.Types.BIGINT:// -5
			return false;
		case java.sql.Types.BINARY:// -2
			return true;
		case java.sql.Types.BIT:// -7
			return false;
		case java.sql.Types.BLOB:// 2004
			return true; // Depends on database
		case java.sql.Types.BOOLEAN:// 16
			return true; // 't' is ok but TRUE without ' is also accepted
		case java.sql.Types.CHAR:// 1
			return true;
		case java.sql.Types.CLOB:// 2005
			return true; // Depends on database
		case java.sql.Types.DATALINK:// 70
			return true; // url linke, so -> True
		case java.sql.Types.DATE:// 91
			return true;
		case java.sql.Types.DECIMAL:// 3
			return false;
		case java.sql.Types.DISTINCT:// 2001
			return true; // user datatype, implementation specific
		case java.sql.Types.DOUBLE:// 8
			return false;
		case java.sql.Types.FLOAT:// 6
			return false;
		case java.sql.Types.INTEGER:// 4
			return false;
		case java.sql.Types.JAVA_OBJECT:// 2000
			return true; // user datatype, should be a binary stream
		case java.sql.Types.LONGNVARCHAR:// -16
			return true;
		case java.sql.Types.LONGVARBINARY:// -4
			return true;
		case java.sql.Types.LONGVARCHAR:// -1
			return true;
		case java.sql.Types.NCHAR:// -15
			return true;
		case java.sql.Types.NCLOB:// 2011
			return true; // Depends on database
		case java.sql.Types.NULL:// 0
			return false;
		case java.sql.Types.NUMERIC:// 2
			return false;
		case java.sql.Types.NVARCHAR:// -9
			return true;
		case java.sql.Types.OTHER:// 1111
			return true;
		case java.sql.Types.REAL:// 7
			return false;
		case java.sql.Types.REF:// 2006
			// ??
			return true;
		case java.sql.Types.ROWID:// -8
			return false;
		case java.sql.Types.SMALLINT:// 5
			return false;
		case java.sql.Types.SQLXML:// 2009
			return true;
		case java.sql.Types.STRUCT:// 2002
			// similar to java map
			return true;
		case java.sql.Types.TIME:// 92
			return true;
		case java.sql.Types.TIMESTAMP:// 93
			return true;
		case java.sql.Types.TINYINT:// -6
			return false;
		case java.sql.Types.VARBINARY:// -3
			return true;
		case java.sql.Types.VARCHAR:// 12
			return true;
		}

		// if not known (wich should never ever happen) quote it
		return true;
	}

	/*
	 * protected String getJavaType(int type) {
	 * 
	 * switch (type) { case java.sql.Types.CHAR:
	 * 
	 * case java.sql.Types.VARCHAR:
	 * 
	 * case java.sql.Types.LONGVARCHAR:
	 * 
	 * return "String";
	 * 
	 * case java.sql.Types.BINARY:
	 * 
	 * case java.sql.Types.VARBINARY:
	 * 
	 * case java.sql.Types.LONGVARBINARY:
	 * 
	 * return "byte[]";
	 * 
	 * case java.sql.Types.NUMERIC:
	 * 
	 * case java.sql.Types.DECIMAL:
	 * 
	 * return "java.math.BigDecimal";
	 * 
	 * case java.sql.Types.BIT:
	 * 
	 * return "boolean";
	 * 
	 * case java.sql.Types.TINYINT:
	 * 
	 * return "byte";
	 * 
	 * case java.sql.Types.SMALLINT:
	 * 
	 * return "short";
	 * 
	 * case java.sql.Types.INTEGER:
	 * 
	 * return "int";
	 * 
	 * case java.sql.Types.BIGINT:
	 * 
	 * return "long";
	 * 
	 * case java.sql.Types.REAL:
	 * 
	 * return "float";
	 * 
	 * case java.sql.Types.FLOAT:
	 * 
	 * case java.sql.Types.DOUBLE:
	 * 
	 * return "double";
	 * 
	 * case java.sql.Types.DATE:
	 * 
	 * return "java.sql.Date";
	 * 
	 * case java.sql.Types.TIME:
	 * 
	 * return "java.sql.Time";
	 * 
	 * case java.sql.Types.TIMESTAMP:
	 * 
	 * return "java.sql.Timestamp";
	 * 
	 * case java.sql.Types.OTHER:
	 * 
	 * return "Object"; }
	 * 
	 * return "** UNKNOWN **"; }
	 */

	/**
	 * Parse a node which textContent contains a integer number. Throws
	 * execption if integer is not between [1,max]
	 * 
	 * @param node
	 *            the node
	 * @param max
	 *            , the upper boundary for the parsed integer
	 * @throws XmlException
	 */
	public static int parseIntTextContent(String nodeInfo, Node node, int max)
			throws XmlException {
		return parseIntTextContent(nodeInfo, node, 1, max);
	}

	/**
	 * Parse a node which textContent contains a integer number. Throws
	 * execption if integer is not between [min,max]
	 * 
	 * @param node
	 *            the node
	 * @param max
	 *            , the upper boundary for the parsed integer
	 * @param min
	 *            ,the bottom boundary for the parsed integer
	 * @throws XmlException
	 */
	public static int parseIntTextContent(String nodeInfo, Node node, int min,
			int max) throws XmlException {
		String intString = node.getTextContent();
		String nodeName = node.getNodeName();
		int value;
		if (intString != null && !intString.isEmpty()) {

			try {
				value = Integer.parseInt(intString);
				if (value < min || value > max) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				StringBuilder errMsg = new StringBuilder();
				errMsg.append(nodeInfo);
				errMsg.append('<');
				errMsg.append(nodeName);
				errMsg.append(">  must be a number between: ");
				errMsg.append(min);
				errMsg.append(" and ");
				errMsg.append(max);
				errMsg.append("\n Value was: ");
				errMsg.append(node.getNodeValue());
				throw new XmlException(errMsg.toString());
			}
		} else {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append(nodeInfo);
			errMsg.append('<');
			errMsg.append(nodeName);
			errMsg.append("> must not be empty. Example: <");
			errMsg.append(nodeName);
			errMsg.append(">10</");
			errMsg.append(nodeName);
			errMsg.append('>');
			throw new XmlException(errMsg.toString());
		}
		return value;
	}

	/**
	 * Parse a node which textContent contains a long number.
	 * 
	 * @param node
	 *            the node
	 * @throws XmlException
	 */
	public static long parseLongTextContent(String nodeInfo, Node node)
			throws XmlException {
		return parseLongTextContent(nodeInfo, node, Long.MIN_VALUE,
				Long.MAX_VALUE);
	}

	/**
	 * Parse a node which textContent contains a long number. Throws execption
	 * if long is not between [min,max]
	 * 
	 * @param node
	 *            the node
	 * @param max
	 *            , the upper boundary for the parsed long
	 * @param min
	 *            ,the bottom boundary for the parsed long
	 * @throws XmlException
	 */
	public static long parseLongTextContent(String nodeInfo, Node node,
			long min, long max) throws XmlException {
		String intString = node.getTextContent();
		String nodeName = node.getNodeName();
		long value;
		if (intString != null && !intString.isEmpty()) {

			try {
				value = Long.parseLong(intString);
				if (value < min || value > max) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				StringBuilder errMsg = new StringBuilder();
				errMsg.append(nodeInfo);
				errMsg.append('<');
				errMsg.append(nodeName);
				errMsg.append(">  must be a number between: ");
				errMsg.append(min);
				errMsg.append(" and ");
				errMsg.append(max);
				errMsg.append("\n Value was: ");
				errMsg.append(node.getNodeValue());
				throw new XmlException(errMsg.toString());
			}
		} else {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append(nodeInfo);
			errMsg.append('<');
			errMsg.append(nodeName);
			errMsg.append("> must not be empty. Example: <");
			errMsg.append(nodeName);
			errMsg.append(">10</");
			errMsg.append(nodeName);
			errMsg.append('>');
			throw new XmlException(errMsg.toString());
		}
		return value;
	}

	/**
	 * Parse a node which textContent contains a long number.
	 * 
	 * @param node
	 *            the node
	 * @throws XmlException
	 */
	public static double parseDoubleTextContent(String nodeInfo, Node node)
			throws XmlException {
		return parseDoubleTextContent(nodeInfo, node, Double.MIN_VALUE,
				Double.MAX_VALUE);
	}

	/**
	 * Parse a node which textContent contains a long number. Throws execption
	 * if long is not between [min,max]
	 * 
	 * @param node
	 *            the node
	 * @param max
	 *            , the upper boundary for the parsed long
	 * @param min
	 *            ,the bottom boundary for the parsed long
	 * @throws XmlException
	 */
	public static double parseDoubleTextContent(String nodeInfo, Node node,
			double min, double max) throws XmlException {
		String intString = node.getTextContent();
		String nodeName = node.getNodeName();
		double value;
		if (intString != null && !intString.isEmpty()) {

			try {
				value = Double.parseDouble(intString);
				if (value < min || value > max) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				StringBuilder errMsg = new StringBuilder();
				errMsg.append(nodeInfo);
				errMsg.append('<');
				errMsg.append(nodeName);
				errMsg.append(">  must be a number between: ");
				errMsg.append(min);
				errMsg.append(" and ");
				errMsg.append(max);
				errMsg.append("\n Value was: ");
				errMsg.append(node.getNodeValue());
				throw new XmlException(errMsg.toString());
			}
		} else {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append(nodeInfo);
			errMsg.append('<');
			errMsg.append(nodeName);
			errMsg.append("> must not be empty. Example: <");
			errMsg.append(nodeName);
			errMsg.append(">10</");
			errMsg.append(nodeName);
			errMsg.append('>');
			throw new XmlException(errMsg.toString());
		}
		return value;
	}

	public static String getNodeNameAttr(Node node, Element parent)
			throws XmlException {

		String name = null;
		boolean found = false;

		NamedNodeMap fieldAttrs = node.getAttributes();

		for (int i = 0; i < fieldAttrs.getLength(); i++) {
			Node attr = fieldAttrs.item(i);
			if (attr.getNodeName().equalsIgnoreCase("name")) {
				name = attr.getNodeValue();
				found = true;
				break;
			}
		}

		// check required attributes
		if (!found) {
			throw new XmlException(parent != null ? parent.getNodeInfo() : ""
					+ "<" + node.getNodeName()
					+ "> is missing the \"name\" attribut.");
		}
		return name;
	}

	/**
	 * Calculates Start of a Partition.<br/>
	 * Assume we have the set {a,b,c,d,e,f,g,h,i,j,k}, so count is 11, and
	 * partCount=3, PartNo=3. <br/>
	 * this means we must divide the set into 3 Partitions and you want the
	 * Index where the third partition starts.<br/>
	 * In this example this would be the 7. element = {g} so this functions
	 * returns 7! <br/>
	 * <br/>
	 * 
	 * Pay attention when operating with arrays! if the set is a array[11] and
	 * array[0]='a' then your starting point is array[getPartitionStart(11,3,3)
	 * - 1]; <br/>
	 * <br/>
	 * 
	 * You may be wondering why the last partition has more elements then the
	 * others. This happens when count modulo partCount != 0; <br/>
	 * More formally the last partition has the length count/partCount + count
	 * modulo partCount. <br/>
	 * In this example this means that the length of the last partition is 11/3
	 * + 11%3 = 3 + 2 = 5<br/>
	 * <br/>
	 * 
	 * @param count
	 *            Number of items in the Set
	 * @param partitions
	 *            Number of partitions (min 1)
	 * @param partitionNumber
	 *            Number of the partition you want the starting index (starting
	 *            at 1)
	 * @return starting index from 1 to count;
	 */
	public static long getPartitionStart(long count, long partitions,
			long partitionNumber) {
		if (count < partitions) {
			return 1;
		}
		return (count / partitions * (partitionNumber - 1)) + 1;
	}

	/**
	 * Calculates End of a Partition.<br/>
	 * Assume we have the set {a,b,c,d,e,f,g,h,i,j,k}, so count is 11, and
	 * partCount=3, PartNo=2. <br/>
	 * this means we must divide the set into 3 Partitions and you want the
	 * Index where the second partition ends.<br/>
	 * In this example this would be the 6. element = {f} so this functions
	 * returns 6! <br/>
	 * <br/>
	 * Pay attention when operating with arrays! if the set is a array[11] and
	 * array[0]='a' then your end point is array[getPartitionStop(11,3,2) - 1]; <br/>
	 * <br/>
	 * 
	 * You may be wondering why the last partition has more elements then the
	 * others. This happens when count modulo partCount != 0; <br/>
	 * More formally the last partition has the length count/partCount + count
	 * modulo partCount. <br/>
	 * In this example this means that the length of the last partition is 11/3
	 * + 11%3 = 3 + 2 = 5<br/>
	 * <br/>
	 * 
	 * @param count
	 *            Number of items in the Set
	 * @param partitions
	 *            Number of partitions
	 * @param partitionNumber
	 *            Number of the partition you want the starting index
	 * @return starting index from 1 to count; if partCount ==partNo then return
	 *         stop for last partition = count
	 */
	public static long getPartitionStop(long count, long partitions,
			long partitionNumber) {
		// last node -> stop must be last item in Partition
		if (partitions == partitionNumber
				|| (count < partitions && partitions == partitionNumber)) {
			return count;
		} else if (count < partitions) {
			return 0;
		} else {
			return count / partitions * partitionNumber;
		}
	}

	public static void main(String[] args) {

		// test simple permutation
		int max = 100;
		int seed = 12216;
		int result[] = new int[max];
		int result2[] = new int[max];
		for (int i = 0; i < max; i++) {

			result[i] = (int) getSimplePermutation(i, max, seed);
			result2[i] = result[i];
			// System.out.println(result[i]);
		}
		java.util.Arrays.sort(result);
		int sum1 = 0;
		int sum2 = 0;
		for (int i = 0; i < result.length; i++) {
			System.out.println(result2[i] + "\t" + result[i]);
			sum1 += i;
			sum2 += result[i];
		}
		System.out.println(sum1 + "\t" + sum2);

	}

	/**
	 * Calulates a substitution of a value in a given range. this is a simple
	 * shuffle algorithm. Uses a seed to generate different shuffels
	 * (substitutions) (or the same again if the same seed is used).
	 * 
	 * @param value
	 * @param maxValue
	 * @param seed
	 * @return
	 */
	public static long getSimplePermutation(long value, long maxValue, long seed) {
		// calculation should not overflow

		if (maxValue < PERMUTATION_LONG_MAX) {
			return (((seed /* % maxValue */) + value) * MAX_INT_PRIME)
					% maxValue;

			// calc in big integer because (seed % maxValue) * value may be
			// greater than Long.maxValue()
		} else {
			BigInteger biValue = BigInteger.valueOf(value);
			BigInteger bimaxValue = BigInteger.valueOf(maxValue);
			BigInteger biSeed = BigInteger.valueOf(seed);
			BigInteger solution = (BL_PRIME.multiply(biValue.add(biSeed)))
					.remainder(bimaxValue);
			return solution.longValue();
		}
	}

	/**
	 * For debug purpose. Returns a graphical representation of a given DOM Tree
	 * starting at node.
	 * 
	 * @param node
	 *            starting Node
	 * @param out
	 *            where to write the result, for example System.out
	 */
	public static void printDOMTree(Node node, PrintStream out)

	{
		int type = node.getNodeType();
		switch (type) {
		// print the document element
		case Node.DOCUMENT_NODE: {
			printDOMTree(((Document) node).getDocumentElement(), out);
			break;
		}

		// print element with attributes
		case Node.ELEMENT_NODE: {
			out.print("<");
			out.print(node.getNodeName());
			NamedNodeMap attrs = node.getAttributes();
			for (int i = 0; i < attrs.getLength(); i++) {
				Node attr = attrs.item(i);
				out.print(" " + attr.getNodeName() + "=\""
						+ attr.getNodeValue() + "\"");
			}
			out.print(">");

			NodeList children = node.getChildNodes();
			if (children != null) {
				int len = children.getLength();
				for (int i = 0; i < len; i++)
					printDOMTree(children.item(i), out);
			}

			break;
		}

		// handle entity reference nodes
		case Node.ENTITY_REFERENCE_NODE: {
			out.print("&");
			out.print(node.getNodeName());
			out.print(";");
			break;
		}

		// print cdata sections
		case Node.CDATA_SECTION_NODE: {
			out.print("<![CDATA[");
			out.print(node.getNodeValue());
			out.print("]]>");
			break;
		}

		// print text
		case Node.TEXT_NODE: {
			out.print(node.getNodeValue());
			break;
		}

		// print processing instruction
		case Node.PROCESSING_INSTRUCTION_NODE: {
			out.print("<?");
			out.print(node.getNodeName());
			String data = node.getNodeValue();
			{
				out.print(" ");
				out.print(data);
			}
			out.print("?>");
			break;
		}
		}

		if (type == Node.ELEMENT_NODE) {
			out.print("</");
			out.print(node.getNodeName());
			out.print('>');
		}
	} // printDOMTree(Node, PrintWriter)

	/**
	 * max deep 100
	 * 
	 * @param subClass
	 * @param superclass
	 * @return
	 */
	public static boolean isSubClassOf(Class subClass, Class superclass) {
		Class tempSubclass = subClass;
		Class tempSuperClass = subClass.getSuperclass();
		int i = 0;
		while (tempSuperClass != null && (i++) < 100) {

			if (tempSuperClass == superclass) {
				return true;
			}
			tempSubclass = tempSuperClass;
			tempSuperClass = tempSubclass.getSuperclass();
		}
		return false;
	}

	//
	public static double roundDouble(double num, int precision) {
		return ((double) Math.round(num * pow(10, precision)))
				/ (pow(10, precision));

	}

	/**
	 * converts a long into representation like double.<br/>
	 * If longValue 12345 and decimalPlaces = 2 output is 123.45<br/>
	 * if long value = 12 and decimalPlaces =3 output is 0.012<br/>
	 * 
	 * @param longValue
	 * @param decimalPlaces
	 * @return
	 */
	public static char[] longToNumberWithDecimalPlaces(long longValue,
			int decimalPlaces) {
		String value = Long.toString(longValue);
		char[] charVal;
		int pointPos = value.length() - decimalPlaces;

		int copyStart = 0;
		if (value.charAt(0) == '-') {
			copyStart = 1;
		}

		// we need padding because final |value| < 1
		if (pointPos < 1 || (pointPos < 2 && copyStart == 1)) {
			int pos = 0;

			// this makes: a aditional leading 0 + a point + padding
			charVal = new char[value.length() - (pointPos - copyStart) + 2];
			// ..and a heading -
			if (copyStart == 1) {
				charVal[pos++] = '-';
			}

			charVal[pos++] = '0';
			charVal[pos++] = '.';

			// do padding behind 0.
			for (int i = 0; i < -(pointPos - copyStart); i++) {
				charVal[pos++] = '0';
			}
			// copy value
			value.getChars(copyStart, value.length(), charVal, pos);

		} else {

			// plus one additional point
			charVal = new char[value.length() + 1];

			// copy value & insert point
			value.getChars(0, pointPos, charVal, 0);
			charVal[pointPos] = '.';
			value.getChars(pointPos, value.length(), charVal, pointPos + 1);

		}
		return charVal;
	}

	private static double sq(double x) {
		return x * x;
	}

	public static double pow(double base, int exp) {
		return exp == 0 ? 1 : sq(pow(base, exp / 2))
				* (exp % 2 == 1 ? base : 1);
	}

	private static long sq(long x) {
		return x * x;
	}

	public static long pow(long base, int exp) {
		return exp == 0 ? 1 : sq(pow(base, exp / 2))
				* (exp % 2 == 1 ? base : 1);
	}

	private static int sq(int x) {
		return x * x;
	}

	public static int pow(int base, int exp) {
		return exp == 0 ? 1 : sq(pow(base, exp / 2))
				* (exp % 2 == 1 ? base : 1);
	}
	
	
	/**
	 * code from:
	 * http://blogs.sphinx.at/java/erzeugen-von-javaiofile-aus-javaneturl/
	 * 
	 * @param url
	 * @return
	 */
	public static File urlToFile(URL url) {
		URI uri;
		try {
			uri = url.toURI();
		} catch (URISyntaxException e) {
			// obviously the URL did
			// not comply with RFC 2396. This can only
			// happen if we have illegal unescaped characters.

			try {
				uri = new URI(url.getProtocol(), url.getUserInfo(),
						url.getHost(), url.getPort(), url.getPath(),
						url.getQuery(), url.getRef());
			} catch (URISyntaxException e1) {
				// The URL is broken beyond automatic repair
				throw new IllegalArgumentException("broken URL: " + url);
			}
		}
		return new File(uri);
	}
	
	public static String getExecutionRootFolder(Class clazz) {
		File f = getJarFolder(clazz);
		String result;
		if (f.isDirectory()) {
			result = f.getAbsolutePath();
		} else {
			result = f.getParent();
		}

		// fallback
		if (result == null) {
			ClassLoader cl = clazz.getClassLoader();
			URL u = cl.getResource(".");
			result = urlToFile(u).getAbsolutePath();
		}
		return result;
	}

	public static File getJarFolder(Class clazz) {
		File currentJar = urlToFile(clazz.getProtectionDomain().getCodeSource()
				.getLocation());

		return currentJar;

		// Old broken way (java.net.URL is really a total mess)

		// String name = StaticHelper.class.getName().replace('.', '/');
		// URL url = StaticHelper.class.getResource("/" + name + ".class");
		// String s="";
		//
		// // we have to do the following hack because the java.net.url class is
		// // broken and there is no new File(URI uri) constructor.
		// // So we have to account for broken urls and whitespace chars being
		// // converted to %20 and stuff like that. A file cannot be created
		// // out of a url path containing whitespace chars in encoded form.
		// // even url.toUri() cannot repair this mess.
		//
		// try {
		// s = URLDecoder.decode(url.toExternalForm(), "UTF-8");
		// } catch (UnsupportedEncodingException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		//
		// // Class is not in Jar
		// if (s.lastIndexOf(".jar") == -1) {
		// return null;
		// }
		// //
		// jar:file:/home/frank/PDGFEnvironment/pdgf.jar!/pdgf/util/StaticHelper.class
		//
		// s = s.replace('/', File.separatorChar);
		// s = s.substring(0, s.indexOf(".jar") + 4);
		// int idx = s.lastIndexOf("jar:file:");
		// if (idx > -1) {
		// s = s.substring(idx + "jar:file:".length());
		// }
		// idx = s.lastIndexOf("jar:");
		// if (idx > -1) {
		// s = s.substring(idx + "jar".length());
		// }
		//
		// // s = s.substring(s.lastIndexOf(':') - 1);
		// return s.substring(0, s.lastIndexOf(File.separatorChar) + 1);
	}

}
