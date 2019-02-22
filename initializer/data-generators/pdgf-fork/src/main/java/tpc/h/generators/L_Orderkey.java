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
package tpc.h.generators;

import pdgf.core.FieldValueDTO;
import pdgf.core.dataGenerator.GenerationContext;
import pdgf.core.dbSchema.Field;
import pdgf.core.exceptions.ConfigurationException;
import pdgf.core.exceptions.XmlException;
import pdgf.plugin.Generator;
import pdgf.plugin.AbstractPDGFRandom;
import pdgf.util.StaticHelper;
import pdgf.util.random.PdgfDefaultRandom;

/**
 * 
 * @author Michael Frank
 * @version 1.0 08.06.2010
 */
public class L_Orderkey extends Generator {

	private static int chunkSize;
	private static int[] randLookup;
	private static int[] l_lineNumber;
	private static int[] countLookup;
	static {
		// 1 + ((7-1)/2) = 4 = mean number of rows.
		chunkSize = 4 * 7;
		randLookup = new int[chunkSize];
		countLookup = new int[7];
		int pos = 0;

		/*
		 * generates:randLookup = new int []
		 * {1,2,2,3,3,3,4,4,4,4,5,5,5,5,5,6,6,6,6,6,6,7,7,7,7,7,7,7}; generates
		 * l_Linenumber
		 * {1,1,2,1,2,3,1,2,3,4,1,2,3,4,5,1,2,3,4,5,6,1,2,3,4,5,6,7}
		 */
		for (int i = 1; i <= 7; i++) {
			countLookup[i - 1] = pos + 1;
			for (int j = 1; j <= i; j++) {
				randLookup[pos] = i;

				pos++;
				// System.out.println(i + " " + j);
			}

		}
	}

	/*
	 * // a ReferenceState per thread private ThreadLocal<Integer>
	 * perThread_L_Linenumber = new ThreadLocal<Integer>() {
	 * 
	 * @Override protected Integer initialValue() {
	 * 
	 * return new Integer(0); } };
	 */
	public L_Orderkey() throws ConfigurationException, XmlException {
		super(
				"For each row in orders table, a random number of rows within [1,7] in the lineitem table");
		// TODO Auto-generated constructor stub
	}

//	// test this class
//	public static void main(String[] args) throws ConfigurationException {
//
//		System.out.println("COUNT LOOKUP TABLE:");
//		for (int i = 0; i < countLookup.length; i++) {
//			System.out.print( countLookup[i]+ " ");
//		}
//		System.out.print("\n");
//
//		
//		System.out.println("RAND LOOKUP TABLE:");
//		for (int i = 0; i < randLookup.length; i++) {
//			System.out.print(randLookup[i]+ " ");
//		}
//		System.out.print("\n");
//		
//		FieldValueDTO fwdto = new FieldValueDTO(java.sql.Types.INTEGER, null);
//		PdgfDefaultRandom rng = new PdgfDefaultRandom();
//		GenerationContext gc = new GenerationContext();
//
//		Field f = new Field();
//	    try {
//	    	
//			java.lang.reflect.Field field  = f.getClass().getDeclaredField("typeAsInt");
//			field.setAccessible(true);
//			field.setInt(f, StaticHelper.getSQLType("INTEGER"));
//		} catch (SecurityException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (NoSuchFieldException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	    
//		L_Orderkey test = new L_Orderkey();
//		test.setParent(f);
//		test.setRngName(PdgfDefaultRandom.class.getSimpleName());
//		try {
//			test.initialize(1);
//			
//		} catch (ConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (XmlException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//
//		System.out.println("RowNo\tOrderKy\tL_LineNumber");
//		for (int i = 1; i < 50; i++) {
//			gc.setCurRow(i);
//			gc.setID(i);
//			test.getNextValue(rng, gc, fwdto);
//			System.out.println(i + " :\t" + fwdto.getValue() + "\t"
//					+ L_LineNumber.calcLineNumberForRow(i));
//
//		}
//
//		System.out.println("reverse lookup + check in (brackets)");
//		System.out
//				.println("\nO_OrderKey -> associated rows in LineItems table");
//		for (int j = 1; j < 22; j++) {
//			long[] linesForOrderKey = getLineItemsForO_OrderRow(j);
//			System.out.print(j + " |\t");
//			for (int i = 0; i < linesForOrderKey.length; i++) {
//				System.out.print(linesForOrderKey[i] + "("
//						+ calcOrderRowForLineitemRow(linesForOrderKey[i])
//						+ ")\t");
//			}
//			System.out.println(" #" + linesForOrderKey.length);
//		}
//
//		System.out.println("start benchmarking...");
//		// Benchmark
//		long start = System.currentTimeMillis();
//		long iterations = 10000000;
//		for (int i = 1; i < iterations; i++) {
//			gc.setCurRow(i);
//			gc.setID(i);
//			test.getNextValue(rng, gc, fwdto);
//
//		}
//		long time = System.currentTimeMillis() - start;
//		System.out.println(iterations + " iterations in " + time + "ms "
//				+ ((iterations * 1000) / time) + " orderkeys/s");
//
//	}

	@Override
	public synchronized void initialize(int workers)
			throws ConfigurationException, XmlException {
		super.initialize(workers);
		

	}

	@Override
	public void nextValue(AbstractPDGFRandom rng,
			GenerationContext generationContext, FieldValueDTO currentFieldValue) {

		currentFieldValue
				.setValue(O_OrderKey
						.calcOOrderKeyFromRow(calcOrderRowForLineitemRow(generationContext
								.getID()))); //.getCurRow() == getID() for historical tables

	}

	// For each row in the ORDERS table, a random number of rows within [1 .. 7]
	// in the LINEITEM table with:
	public static long calcOrderRowForLineitemRow(long curRow) {
		return ((curRow - 1) / chunkSize) * 7
				+ randLookup[(int) ((curRow - 1) % chunkSize)];
	}

	public static long[] getLineItemsForO_OrderRow(long oOrkderRowNo) {
		// calc chunk

		long chunkstart = (oOrkderRowNo - 1) / 7 * chunkSize;

		// remainder in chunk
		long startInChunk = countLookup[(int) ((oOrkderRowNo - 1) % 7)];
		long LinItemRowStart = chunkstart + startInChunk;
		long[] linesForOrderKey = new long[1 + (int) ((oOrkderRowNo - 1) % 7)];
		linesForOrderKey[0] = LinItemRowStart;
		for (int i = 1; i < linesForOrderKey.length; i++) {
			linesForOrderKey[i] = linesForOrderKey[i - 1] + 1;
		}
		return linesForOrderKey;
	}

	@Override
	protected void configParsers() throws XmlException{
		super.configParsers();
		
	}

}
