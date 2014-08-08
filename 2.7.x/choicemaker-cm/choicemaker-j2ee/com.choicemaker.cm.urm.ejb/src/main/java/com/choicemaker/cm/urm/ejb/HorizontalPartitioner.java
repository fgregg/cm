/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.urm.ejb;

/**
 * @author emoussikaev
 * @version Revision: 2.5  Date: Oct 6, 2005 2:37:37 PM
 * @see
 */
public class HorizontalPartitioner {

	private static char EP = '\0';
	private static int POS_TYPE_NUM = 0;
	private static int POS_TYPE_ALPHAU = 1;
	private static int POS_TYPE_ALPHAL = 2;
	private static int POS_TYPE_ALPHANUM = 3;  
	int 	maxSize;
	int		width; 
	int[]	positionType;
	char[]	typeMin = {'0','A','a','0'};
	char[]	typeMax = {'9','Z','z','z'};
	
	/**
	 * 
	 */
	
	public HorizontalPartitioner() {
		super();
	}

	void addRecordSource(char[] min, char[] max, boolean excludeMax){
		String idsQuery ="";
		String idName = "mci_id";
		StringBuffer minb = new StringBuffer ();
		StringBuffer maxb = new StringBuffer ();
		int minLen = width;
		while(min[minLen-1] ==EP)
			minLen--;
		for (int i= 0; i< minLen ; i++) {
			if(min[i]!=EP )
				minb.append(min[i]);
			else
				minb.append(typeMin[positionType[i]]);		
		}
		int maxLen = width;
		while(max[maxLen-1] == EP)
				maxLen--;
		for (int i= 0; i< maxLen; i++) {
			if(max[i]!=EP )
				maxb.append(max[i]);
			else
				maxb.append(typeMin[positionType[i]]);		
		}

		System.out.println (setMinMax (idsQuery, idName, minb.toString(), maxb.toString(), excludeMax));
	}
	
	private String setMinMax (String idsQuery, String idName, String min, String max, boolean excludeMax) {
			StringBuffer sb = new StringBuffer ();
		
			int ind = idsQuery.toUpperCase().indexOf("WHERE");

			if ( ind > 0) {
				sb.append(idsQuery.substring(0, ind + 6));
			} else {
				sb.append(idsQuery);
				sb.append(" where ");
			}
			
			sb.append ("(");
			sb.append (idName);
			sb.append (" between '");
			sb.append(min);
			sb.append("' and '");
			sb.append(max);
			if(excludeMax){	//			and mci_id <> '3'
				sb.append("' and '");
				sb.append (idName);
				sb.append (" <> '");
				sb.append(max);
			}								
			sb.append ("')");
			
		if ( ind > 0) {
			sb.append (" and ");
			sb.append(idsQuery.substring(ind + 6));
		}
		
		return sb.toString();
	}
	
	
	void calculateSegment(int pos, int count, char[] min, char[] max, boolean excludeMax){
		
		float reqChunkFloat = (float)count/(float)maxSize; 
		if( reqChunkFloat <1.5f || pos >= width) {
			addRecordSource(min,max, excludeMax);
			return;
		}
//		at least 2 chunks required
		int requiredChunkNumb = (int)Math.ceil(reqChunkFloat); 
		char minChar = min[pos];
		char maxChar = max[pos];
		
		boolean equal = true; 
		for (int i = 0; i < pos; i++) {
			if(min[i]!=max[i]){
				equal = false;
				break;
			}
		}

		if(equal &&( minChar > maxChar && maxChar != EP && minChar != EP
		   ||  maxChar == typeMin[positionType[pos]] && minChar == EP) 
		   ) {
			addRecordSource(min, max, excludeMax);
			return;
		}
		
		if(minChar == maxChar && maxChar != EP )  {
			calculateSegment(pos+1, count, min, max, excludeMax);
			return;
		}
		

		
		//2 chunks available except case when  min[pos]>max[pos]				
		int availSymbolNumb = 0;	
		char[]	symbols = new char[256];
		char curChar;
		if(minChar == EP){
			availSymbolNumb = 1;
			symbols[0] = minChar;
			curChar = typeMin[positionType[pos]];
		} else
			curChar = minChar;
		
		char stopChar = (maxChar==EP?typeMax[positionType[pos]]:maxChar);
		while (curChar <= stopChar) {
			if( Character.isLetterOrDigit(curChar)){
				symbols[availSymbolNumb++] = curChar;
			}
			curChar++;	
		}
		if (maxChar==EP)
			symbols[availSymbolNumb++] = EP;
		int step = 1;
		int chunkNumb = availSymbolNumb;
		if(requiredChunkNumb < availSymbolNumb) { // 5 < 12
			step = (int)Math.ceil((float)availSymbolNumb/(float)requiredChunkNumb); //step = ]12/5[ = 3  
			chunkNumb = (int)Math.ceil((float)availSymbolNumb/(float)step); // chunkNumber = ]12/3[ = 4 - not 5
		}
		
		int newCount = count/chunkNumb;
		char[] newMin = new char[width];
		char[] newMax = new char[width];
		for (int i = 0; i < newMin.length; i++) newMin[i] = min[i];
		for (int i = 0; i < newMax.length; i++) newMax[i] = i<pos?min[i]:EP;
		
		int n=0;
		if(step == 1 && chunkNumb >2 && min[pos] == EP)
			n=1;
		
		for(; n<chunkNumb-2 ; n++){ //the last chunk is created differently
			newMax[pos] = symbols[(n+1)*step];
			calculateSegment(pos+1,newCount,newMin,newMax,true);
			newMin[pos]= newMax[pos];
			for (int i = pos+1; i < width; i++) newMin[i]=EP;
		}
				
		for (int i = 0; i < width; i++) newMax[i] = max[i];
		for (int i = pos+1; i < width; i++) newMax[i]=EP;
		//if(newMin[pos]!= EP || newMax[pos]!= typeMin[positionType[pos]])
			calculateSegment(pos+1,newCount,newMin,newMax,true);
			
		
		for (int i = 0; i < width; i++) newMin[i] = newMax[i];
		for (int i = 0; i < width; i++) newMax[i] = max[i];
		if(symbols[availSymbolNumb-1] != EP)			 
			calculateSegment(pos+1,newCount,newMin,newMax,excludeMax);
	}

	public static void main(String[] args) {

		String min = "B1";
		String max = "B9999";
		int count =18701;

//		String min = "A10";
//		String max = "A99999";
//		int count =277;

//		String min = "A45";
//		String max = "C23";
//		int count =77;

//		String min = "A45";
//		String max = "C23";
//		int count =177;


		
//		String min = "A";
//		String max = "B00";
//		int count =120;

//		String min = "A99";
//		String max = "C00";
//		int count =120;

//		String min = "C";
//		String max = "C00";
//		int count =30;


//		String min = "32H";
//		String max = "45M";
//		int count =300;


		HorizontalPartitioner t = new HorizontalPartitioner();
		t.maxSize = 1000; 
		int nar = Math.min(min.length(),max.length());
		t.width = Math.max(min.length(),max.length())+1;
		t.positionType = new int[t.width];
		for (int i = 0; i < nar; i++) {
			if( 
				Character.isDigit(min.charAt(i)) && Character.isDigit(max.charAt(i)) 
			  ){
				t.positionType[i] = POS_TYPE_NUM;
				continue;
			}
			if(
				Character.isLetter(min.charAt(i)) && Character.isLetter(max.charAt(i)) &&
			   	Character.isLowerCase(min.charAt(i)) && Character.isLowerCase(max.charAt(i)) 
			  ){
				t.positionType[i] = POS_TYPE_ALPHAL;
				continue;
			   }
			if(
				Character.isLetter(min.charAt(i)) && Character.isLetter(max.charAt(i)) &&
			   	Character.isUpperCase(min.charAt(i)) && Character.isUpperCase(max.charAt(i)) 
			   ){
				t.positionType[i] = POS_TYPE_ALPHAU;
				continue;
			   }
			t.positionType[i] = POS_TYPE_ALPHANUM;   
		}
		for (int i = nar; i < t.width; i++) {
			if( 
				(i< min.length() && Character.isDigit(min.charAt(i)) && max.length()<= i ) 
				|| (i < max.length() && min.length()<= i)&& Character.isDigit(max.charAt(i))
			  ){
				t.positionType[i] = POS_TYPE_NUM;
				continue;
			}

			t.positionType[i] = POS_TYPE_ALPHANUM;				
		} 
		char[] left = new char[t.width];
		char[] right = new char[t.width];
		for (int i = 0; i < right.length; i++) {
			if(i<max.length())
				right[i] = max.charAt(i);
			else	
				right[i] = EP;
		}
		for (int i = 0; i < left.length; i++) {
			if(i<min.length())
				left[i] = min.charAt(i);
			else	
				left[i] = EP;
		}
		t.calculateSegment(0,count,left,right,false);
	}
}
