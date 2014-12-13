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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.jmeter.samplers.SampleResult;

public class ErrorListenerLogger {

FileWriter fstream = null; //create filewriter object
BufferedWriter out = null; //create Bufferered object

FileWriter fstream2 = null; //create filewriter object
BufferedWriter out2 = null; //create Bufferered object
String pHA= null;
Boolean hFlag = false;
//FileOutputStream outbytes = null;
File bytewriter=null;
public ErrorListenerLogger()
{

}
public ErrorListenerLogger(String filePath)
{
	JOptionPane.showMessageDialog(null, "alert","BP1", JOptionPane.OK_OPTION);
try
{
//bytewriter = new File(filePath);
//bytewriter.createNewFile();
}
catch(Exception ex)
{
System.out.println("Error: " + ex.getMessage());
}
}

public ErrorListenerLogger(String filePath,boolean status)
{
	JOptionPane.showMessageDialog(null, "alert","BP2", JOptionPane.OK_OPTION);
try
{	
/*
	bytewriter = new File(filePath);
if(bytewriter.getFreeSpace()==bytewriter.getTotalSpace())
	JOptionPane.showMessageDialog(null,"file is new error log " ,"file path", JOptionPane.OK_OPTION);
*/
JOptionPane.showMessageDialog(null,filePath,"file path", JOptionPane.OK_OPTION);
//fstream = new FileWriter(filePath,status);

//out = new BufferedWriter(fstream);

fstream2 = new FileWriter(filePath,status);
out2 = new BufferedWriter(fstream2);

}
catch(Exception ex)
{
System.out.println("Error: " + ex.getMessage());
}

}

public  ErrorListenerLogger(String filePath,boolean status, StringBuilder propertyHeaderArray)
{
	JOptionPane.showMessageDialog(null, "alert","BP2", JOptionPane.OK_OPTION);
	synchronized (ErrorListenerLogger.class){
	
	pHA=propertyHeaderArray.toString();
try
{	
bytewriter = new File(filePath);

if(bytewriter.getFreeSpace()==bytewriter.getTotalSpace())
{
JOptionPane.showMessageDialog(null,"file is new prop file" +propertyHeaderArray.toString() ,"FILE FILE FILE", JOptionPane.OK_OPTION);
hFlag=true;
//logtocsv(propertyHeaderArray.toString());
}
else
	hFlag=false;

JOptionPane.showMessageDialog(null,filePath,"file path", JOptionPane.OK_OPTION);
fstream = new FileWriter(filePath,status);

out = new BufferedWriter(fstream);


//fstream2 = new FileWriter(filePath,status);
//out2 = new BufferedWriter(fstream2);

}
catch(Exception ex)
{
System.out.println("Error: " + ex.getMessage());
}
	}
}

/** Write error response to file
* @param s SampleErrorResult to save
* @param bytestream, the error response data to write
*/
public void logBytes(String str) throws IOException{

	
	try
	{
	out2.write(str);
	out2.flush();
	}
	catch(Exception ex)
	{
	System.out.println("Error while logging sample  error response data" + ex.getMessage());
	}
	finally
	{
	fstream2.flush();
	out2.close();
	fstream2.close();
	}
	
	/*
	try
{
outbytes = new FileOutputStream(bytewriter);
outbytes.write(bytestream);
outbytes.flush();

}
catch (FileNotFoundException e1)
{
System.out.println("Error creating sample file for " + s.getSampleLabel()+ e1);
ErrorListener.log.error("Error creating sample file for " + s.getSampleLabel(), e1);
} catch (IOException e1) {
System.out.println("Error saving sample " + s.getSampleLabel()+ e1);
ErrorListener.log.error("Error saving sample " + s.getSampleLabel(), e1);
}
catch(Exception ex)
{
System.out.println("Error while saving sample"+ s.getSampleLabel() + ex.getMessage());
}
finally {
outbytes.close();
}
*/
}
/** Write property data to the file
* @param str String,the property data to write
 * @return 
*/
public  synchronized void logtocsv(String str) throws IOException{

	try
{
	if(hFlag==true)
	{
		out.write(pHA);
		JOptionPane.showMessageDialog(null,"PHA","file PHA", JOptionPane.OK_OPTION);
	}
	hFlag=false;
		
out.write(str);
out.flush();
}
catch(Exception ex)
{
System.out.println("Error while logging sample properties" + ex.getMessage());
}
finally
{
fstream.flush();
out.close();
fstream.close();
}
}

}

