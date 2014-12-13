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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

//import NewListener.basepath;

/**
* Save Error responseData and JMeter state(properties/variables)
* to a set of files called Error Response file and Property file
* when error occurs during execution.
*
* This functions both in GUI and non-GUI runs.
*
* For multiple errors with same response the plugin will create only one error response file.
* Whereas,property file will log the details of every single error that occured.
* JMeter properties/variables to be logged can be defined by the user.
*/
// 1  extends AbstractTestElement class and implements  SampleListener
public class ErrorListener extends AbstractTestElement implements Serializable, SampleListener,TestListener,UnsharedComponent {
static final Logger log = LoggingManager.getLoggerForClass();
private static final long serialVersionUID = 240L;

/** To synchronize the process of logging errors and properties to files*/
  static Object syncObject =new Object();


//2
/** Base Properties referenced by plugin to log errors and store the files*/
static public   String BASEPATH;
static String ERRORFILE;
static String PROPFILE;
static String FILEEXT;
static String VARIABLE_NAME;
static boolean ERRORS_ONLY =true;
static boolean SUCCESS_ONLY =false;
static boolean SKIP_SUFFIX =false; //$NON-NLS-1$
static String propertyFileExtension=".csv";
//public data d;
Boolean P[] = new Boolean [9];


//Not used currently
static String JPropCount = JMeterUtils.getPropDefault("JmeterPropertiesCountToLog", "5");


//
/** Holds the property file name and error file name*/
static Map<String, String> ErrorFileSampleMap = new ConcurrentHashMap<String, String>();
//Not used currently
static Map<String, String> PropertyFileSampleMap = new ConcurrentHashMap<String, String>();


/** Variables used intermediately to identify the status of execution
* and execution of specific functions
*/

static  boolean flag=false;
static  boolean warning=false;
 static boolean headersCreated=false;
 static String reportPath=null;
 static String propertyPath=null;
 static boolean directoryCreated=false;
 static boolean updateAllowed=true;
 public  String [] propertyHeaderArray=new String[20];

/**
* Constructor is initially called once for each occurrence in the test plan
* For GUI, several more instances are created Then clear is called at start
* of test Called several times during test startup The name will not
* necessarily have been set at this point.
*/
public ErrorListener() {
	
super();
//JOptionPane.showMessageDialog(null,"message","hghghghhhg", JOptionPane.OK_OPTION);
}

//2 called when test starts .. it sets the initial vals of all d flags
private static void initialize() {

	


//SaveResult.sampleno=0;
//Arrays.fill(NewListener1.propertyHeaderArray,null);//*****************
synchronized (PropertyFileSampleMap) {
PropertyFileSampleMap.clear();}
synchronized (ErrorFileSampleMap) {
ErrorFileSampleMap.clear();}

//propertyPath=   "_TimeStamp-"+GetTimeStampInHMS()+"_"+GetTimeStampInMs();//threadGroup


}

/**
* Constructor for use during startup (intended for non-GUI use) @param name
* of summariser
*/
public ErrorListener(String name) {
this();
setName(name);
}



/**
* This is called once for each occurrence in the test plan, before the
* start of the test. The super.clear() method clears the name (and all
* other properties), so it is called last.
*/
@Override
public void clear() {
super.clear();
}

public void getdata(String BPATH,Boolean p[],String [] pHArray)
{
	BASEPATH = BPATH;
	P=p;
	propertyHeaderArray=pHArray;
	//SaveResult.propertyHeaderArray=propertyHeaderArray;
//	JOptionPane.showMessageDialog(null,"message","ph(getdata)"+propertyHeaderArray[0], JOptionPane.OK_OPTION);
	//d.B=BASEPATH;
}

public String gbasepath(){
	//JOptionPane.showMessageDialog(null,"messageGB",BASEPATH, JOptionPane.OK_OPTION);

	return BASEPATH;
}
/**
* Saves the error response and properties of sample error request in files
*
* @see org.apache.jmeter.samplers.SampleListener#sampleOccurred(org.apache.jmeter.samplers.SampleEvent)
*/

// 3 called when a sample occours ..it processes d data from each sample
public void sampleOccurred(SampleEvent e) {

	
	//JOptionPane.showMessageDialog(null, "alert","test"+NewListener1.propertyHeaderArray[0]+NewListener1.propertyHeaderArray[1]+NewListener1.propertyHeaderArray[2], JOptionPane.OK_OPTION);
if(!e.getResult().isSuccessful())
{
synchronized(syncObject)
{
if(flag==false)
{
/** Fetch and initialize the base properties*/
//JOptionPane.showMessageDialog(null,"messagebpath",e.getHostname()+"||"+e.getThreadGroup() , JOptionPane.OK_OPTION);

if(null==BASEPATH || BASEPATH.isEmpty())
{
BASEPATH= JMeterUtils.getJMeterBinDir();	// save in bin
}
ERRORFILE=getPropertyAsString("ErrorFilePrefix").trim();
if(null==ERRORFILE || ERRORFILE.isEmpty())
{
ERRORFILE="ErrorLog";
}
//PROPFILE=getPropertyAsString("PropertyFilePrefix").trim();
if(null==PROPFILE || PROPFILE.isEmpty())
{
PROPFILE="Error_Report_"+"Timestamp"+GetTimeStampInHMS()+".csv";
}
FILEEXT=getPropertyAsString("FileExtensionName").trim();
VARIABLE_NAME=getPropertyAsString("VariableName").trim();
if(null==VARIABLE_NAME || VARIABLE_NAME.isEmpty())
{
VARIABLE_NAME="JVariable";
}



/** Construct the directory path to store the error and property data*/
StringBuilder sb = new StringBuilder(BASEPATH);
if(!BASEPATH.endsWith(File.separator))  // separator char is//
{
sb.append(File.separator);
}
BASEPATH=sb.append(reportPath).toString();  
PROPFILE=BASEPATH+PROPFILE;//+propertyPath+propertyFileExtension;sampleno



/** Fetch and initialize the user defined properties*/
//PropertyIterator iter = propertyIterator();
//while (iter.hasNext()) {
//JMeterProperty prop = iter.next();
//if(prop.getName().startsWith("JmeterProperty"))
//{

//PropertyCount++;
//}
//}
flag=true;
}

/** 4 Create the directory where the files will be stored*/	
File createDir = new File(BASEPATH);
if(!createDir.exists())
{
createDir.mkdirs();
directoryCreated = true;
//JOptionPane.showMessageDialog(null,BASEPATH,"dir created", JOptionPane.OK_OPTION);
}else
{
	directoryCreated = true;
}
	
//JOptionPane.showMessageDialog(null,"message","ph(Sampleocc)"+propertyHeaderArray[0], JOptionPane.OK_OPTION);
if(directoryCreated==true) 
{
warning=false;
processSample(e.getResult(), new Counter());
}
else
{
if(warning=false)
{
System.out.println("Directory could not be created.Hence,not proceeding with logging errors...");
}warning=true;
}
}
}
}





/**
* Recurse the whole (sub)result hierarchy.
*
* @param s Sample result
* @param c sample counter
*/

// 5- called by sampleOccured . it creates a saveResult object n uses it 2 call its fn SaveSampleResult
private void processSample(SampleResult s, Counter c) {

SaveResult saveResult=new SaveResult();
//JOptionPane.showMessageDialog(null, "alert","(processSample)"+propertyHeaderArray[0], JOptionPane.OK_OPTION);
//saveResult.SaveSampleResult(s,c.num++,BASEPATH,P, PROPFILE,FILEEXT, propertyHeaderArray,headersCreated);
SampleResult[] sr = s.getSubResults();
for (int i = 0; i < sr.length; i++) {
processSample(sr[i], c);
}
}

/**
* Get the current timestamp in human readable format
*
*/
private static String GetTimeStampInHMS() {

long epoch = System.currentTimeMillis()/1000;
String date = new java.text.SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new java.util.Date (epoch*1000));
return date;

}

/**
* Get the current timestamp in milliseconds
*
*/
private static long GetTimeStampInMs() {
return System.currentTimeMillis();
}

// Mutable int to keep track of sample count
private static class Counter{
int num=0;
}

/**
* {@inheritDoc}
*/
//called when a sample has started 
public void sampleStarted(SampleEvent e) {

	
}

/**
* {@inheritDoc}
*/
//called when a sample has stopped
public void sampleStopped(SampleEvent e) {
// not used
}

//called when a test has ended   //SKIP_SUFFIX
@Override
public void testEnded() {
updateAllowed=true;
}

@Override
public void testEnded(String arg0) {
updateAllowed=true;
}

@Override
public void testIterationStart(LoopIterationEvent arg0) {
// TODO Auto-generated method stub
}
//called when a test has ended
@Override
public void testStarted() {
initialize();
flag=false;
//SaveResult.sampleno=0;
directoryCreated=false;
headersCreated=false;
//trial.flag=false;
JOptionPane.showMessageDialog(null, "alert","flag is false hahahaha", JOptionPane.OK_OPTION);
updateAllowed=false;
reportPath="ErrorListener"+"_"+"TimeStamp-"+GetTimeStampInHMS()+"_"+File.separator;

}

@Override
public void testStarted(String arg0) {
initialize();
flag=false;
//SaveResult.sampleno=0;
directoryCreated=false;
headersCreated=false;
//trial.flag=false;
JOptionPane.showMessageDialog(null, "alert","flag is falsehahah", JOptionPane.OK_OPTION);
updateAllowed=false;
reportPath="ErrorListener"+"_"+"TimeStamp-"+GetTimeStampInHMS()+"_"+File.separator;

}
/*
*/
/*@Override
public void processBatch(List<SampleEvent> arg0) throws RemoteException {
// TODO Auto-generated method stub
}*/

public SampleSaveConfiguration getSaveConfig() {//not required
	// TODO Auto-generated method stub
	return null;
}
}

