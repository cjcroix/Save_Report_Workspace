/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

package org.apache.jmeter.visualizers;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.JOptionPane;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;

/**
* Save the SampleErrorResponse and Jmeter State in files
*
*/
public class SaveResult  {

/**Holds the property name and value */
 StringBuilder propertyHeader = new StringBuilder(1000);
 StringBuilder propertyValue = new StringBuilder(1000);
 
String BPATH;

/**Holds the error and property filename*/
private String propFileName=null;
private String errorFileName=null;
protected  int sampleno;
static String FILEEXT;
protected Boolean[] p =new Boolean[9];
 boolean SKIP_SUFFIX =false;
boolean headersCreated =false;
public   String [] propertyHeaderArray=new String[20];

// public static boolean headersCreated ;    //initialized in Error Listener


/**Holds the string to be logged into property file*/
private String JPropertiesString=null;

/**Holds the hash value of error response*/
private static String errorResponseHashValue=null;

/**Holds the variable name which contains the Error file name*/
String variable =null;

/**
* @param s SampleErrorResult to save
* @param num number to append to variable (if >0)
 * @param BASEPATH 
 * @param errorFileName2 
 * @param count 
*/
public synchronized void SaveSampleResult(SampleResult s, int num, String BASEPATH,Boolean []P,String pFN,String FE ,String [] propertyHeaderArr,Boolean hCreated, String errorFileName2, int count)
{
	sampleno =count;
	errorFileName=errorFileName2;
	JOptionPane.showMessageDialog(null, s.getResultFileName(),"ssr result fn", JOptionPane.OK_OPTION);
	headersCreated=hCreated;
	BPATH=BASEPATH;
	p=P;
	FILEEXT=FE;
	

	//sampleno =num;
// Should we save the sample?

	/*if (s.isSuccessful()){
if (ErrorListener.ERRORS_ONLY){
return;
}
} 
else {
if (ErrorListener.SUCCESS_ONLY){
return;
}
}
*/

/**Log Error Response*/	
//if(ErrorListener.ERRORSANDPROPS)
//{
LogErrorResponse(s,num);
//}

//1
/**Log Properties to csv file*/	
          
// Format the string to log into the property log file in csv format
//JOptionPane.showMessageDialog(null, "alert","before format string", JOptionPane.OK_OPTION);
//JOptionPane.showMessageDialog(null, "alert",s.toString(), JOptionPane.OK_OPTION);
JPropertiesString=FormatString(s,propertyHeaderArr);//**********
//JOptionPane.showMessageDialog(null,propertyHeaderArr ,JPropertiesString, JOptionPane.OK_OPTION);

// Get the Property Log file name
propFileName=pFN;
//propFileName=trial.PROPFILE;


// Write the Jmeter Properties data to file
//JOptionPane.showMessageDialog(null,propFileName,"inside sve mple res", JOptionPane.OK_OPTION);
ErrorListenerLogger logToCsv=new ErrorListenerLogger(propFileName,true,propertyHeader);
JOptionPane.showMessageDialog(null,JPropertiesString," j prop", JOptionPane.OK_OPTION);

try {
logToCsv.logtocsv(JPropertiesString);
} catch (IOException e) {
System.out.println("Error: " + e.getMessage());
}
}

/**
* @return fileName composed of fixed prefix, thread number,timestamp in human readable format
* and milliseconds.A suffix derived from the contentType
* e.g. Content-Type:text/html;charset=ISO-8859-1
*/
/*
private String makeErrorFileName(String contentType,SampleResult s)
{
StringBuilder sb = new StringBuilder(BPATH);
sb.append("ErrorLog"+"_"+"TimeStamp-"+ConvertTimeStampToHMS(s.getTimeStamp()));
JOptionPane.showMessageDialog(null,sb,"inside mk error filename", JOptionPane.OK_OPTION);
if(null!=FILEEXT && !FILEEXT.isEmpty())
{
sb.append('.');
sb.append(FILEEXT);
}
else
{
if (!SKIP_SUFFIX){
sb.append('.');
if (contentType != null) {
int i = contentType.indexOf("/"); // $NON-NLS-1$
if (i != -1) {
int j = contentType.indexOf(";"); // $NON-NLS-1$
if (j != -1) {
sb.append(contentType.substring(i + 1, j));
} else {
sb.append(contentType.substring(i + 1));
}
} else {
sb.append("unknown");
//sb.append(ErrorListener.FILEEXT);s.getThreadName()
}
} else {
sb.append("unknown");
}
}
}
return sb.toString();
}
*/
/**
* Get the current timestamp in huma n readable format
*
*/
public static String ConvertTimeStampToHMS(long currenttimestamp)
{
long epoch = currenttimestamp/1000;
String date = new java.text.SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new java.util.Date (epoch*1000));
return date;
}

/**
* Format the string to be logged in the property csv file
*
*/
public String FormatString(SampleResult s,String[] propertyHeaderArray )//********************
{	
	StringBuilder sb = new StringBuilder(100); // output line buffe
//propertyHeader =new StringBuilder(50);
propertyValue=new StringBuilder(50);
//JOptionPane.showMessageDialog(null, "alert",propertyHeaderArray[0], JOptionPane.OK_OPTION);


	// Construct the headers
JOptionPane.showMessageDialog(null, Boolean.toString(headersCreated)+ s.getThreadName(),"headers created flag", JOptionPane.OK_OPTION);

//if(headersCreated==false)
	//{
		int count=0;
		
		while(propertyHeaderArray[count]!=null)
		{
			if(count==0)			
			propertyHeader.append(propertyHeaderArray[count]);
		else
		{
			propertyHeader.append(","+propertyHeaderArray[count]);
		}
		
	count++;


		}
		propertyHeader.append("\n");
//	}

	
	// Construct the values to be written to csv file
	String str[]= new String[10]; 
	int valCount=0;
	if(p[0]||p[1])
	{
		str[valCount]=Integer.toString(sampleno);
		valCount++;
	}
	if(p[0]||p[2])
	{
		str[valCount]=ConvertTimeStampToHMS( s.getStartTime());
		valCount++;
	}
	if(p[0]||p[3])
	{
		str[valCount]=s.getThreadName();
		valCount++;
	}
	if(p[0]||p[4])
	{
		str[valCount]=s.getSampleLabel();
		valCount++;
	}
	if(p[0]||p[5])
	{
		str[valCount]=Long.toString((s.getTime()));
		valCount++;
	}
	if(p[0]||p[6])
	{
		str[valCount]="Failed";
		valCount++;
	}
	if(p[0]||p[7])
	{
		str[valCount]=Integer.toString(s.getBytes());
		valCount++;
	}
	if(p[0]||p[8])
	{
		str[valCount]= Long.toString(s.getLatency());
		valCount++;
	}
	
	for(int i=0;i<valCount;i++)
	{
		if(i==0)
		sb.append(str[i]);
		else
			sb.append(","+str[i]);
	}
	sb.append("\n");




	
	

return sb.toString();
		



}
	

/** Log error response to the file
 * @param  */                      //if not working restore
private void LogErrorResponse(SampleResult s,int num)
{
/**Create hash value of the response*/
try {
errorResponseHashValue = MD5Hash(s.getResponseData());
}
catch (NoSuchAlgorithmException e) {	
System.out.println("Error: " + e.getMessage());
}

/**Make error file name*/
/*
if(!ErrorListener.ErrorFileSampleMap.containsKey(errorResponseHashValue)) 
{
errorFileName = makeErrorFileName(s.getContentType(),s);
s.setResultFileName(errorFileName);// Associate sample with file name
ErrorListener.ErrorFileSampleMap.put(errorResponseHashValue,s.getResultFileName());
variable = ErrorListener.VARIABLE_NAME;
if (variable.length()>0){
if (num > 0) {
StringBuilder sb = new StringBuilder(variable);
sb.append(num);
variable=sb.toString();
}
JMeterContextService.getContext().getVariables().put(variable, errorFileName);
}
*/
//errorFileName = makeErrorFileName(s.getContentType(),s);

/**Write the error data to file*/      //***********************
JOptionPane.showMessageDialog(null, "alert",errorFileName, JOptionPane.OK_OPTION);
ErrorListenerLogger logger;
	logger =new ErrorListenerLogger(errorFileName,true);	

try {
logger.logBytes(s.getResponseDataAsString());

} catch (IOException e) {
// TODO Auto-generated catch block
e.printStackTrace();
}
}


	/*
}
errorFileName=ErrorListener.ErrorFileSampleMap.get(errorResponseHashValue);
JMeterContextService.getContext().getVariables().remove(ErrorListener.VARIABLE_NAME);
variable = ErrorListener.VARIABLE_NAME;
JMeterContextService.getContext().getVariables().put(variable, errorFileName);
s.setResultFileName(errorFileName);*/



/**
* MD5 implementation as Hash value
*
* @param a_sDataBytes - a original data as byte[] from String
* @return String as Hex value
* @throws NoSuchAlgorithmException
*/

public static String MD5Hash(byte[] dataBytes) throws NoSuchAlgorithmException {
if( dataBytes == null) return "";

MessageDigest md = MessageDigest.getInstance("MD5");
md.update(dataBytes);
byte[] digest = md.digest();

// convert it to the hexadecimal
BigInteger bi = new BigInteger(digest);
String hash = bi.toString(16);
if( hash.length() %2 != 0)
{
hash = "0"+hash;
}
return hash;
}
}	