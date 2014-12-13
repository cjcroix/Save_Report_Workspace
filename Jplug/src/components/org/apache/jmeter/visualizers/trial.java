/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.EventListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.jmeter.JMeterReport;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.functions.TestPlanName;
import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.CommentPanel;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.junit.JMeterTest;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.util.Calculator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.trial.Counter;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.gui.RendererUtils;
import org.apache.jorphan.gui.RightAlignRenderer;
import org.apache.jorphan.gui.layout.VerticalLayout;
import org.apache.jorphan.reflect.Functor;

/**
 * This class implements a statistical analyser that calculates both the average
 * and the standard deviation of the sampling process. The samples are displayed
 * in a JTable, and the statistics are displayed at the bottom of the table.
 *
 * created March 10, 2002
 *
 */
public class trial extends AbstractVisualizer implements Clearable ,ActionListener{

	
    private static final long serialVersionUID = 240L;

    // Note: the resource string won't respond to locale-changes,
    // however this does not matter as it is only used when pasting to the clipboard
    private static final ImageIcon imageSuccess = JMeterUtils.getImage(
            JMeterUtils.getPropDefault("viewResultsTree.success",  //$NON-NLS-1$
                                       "icon_success_sml.gif"),    //$NON-NLS-1$
            JMeterUtils.getResString("table_visualizer_success")); //$NON-NLS-1$

    private static final ImageIcon imageFailure = JMeterUtils.getImage(
            JMeterUtils.getPropDefault("viewResultsTree.failure",  //$NON-NLS-1$
                                       "icon_warning_sml.gif"),    //$NON-NLS-1$
            JMeterUtils.getResString("table_visualizer_warning")); //$NON-NLS-1$

    private static final String[] COLUMNS = new String[] {
            "table_visualizer_sample_num",  // $NON-NLS-1$
            "table_visualizer_start_time",  // $NON-NLS-1$
            "table_visualizer_thread_name", // $NON-NLS-1$
            "sampler_label",                // $NON-NLS-1$
            "table_visualizer_sample_time", // $NON-NLS-1$
            "table_visualizer_status",      // $NON-NLS-1$
            "table_visualizer_bytes",       // $NON-NLS-1$
            "table_visualizer_latency"};    // $NON-NLS-1$

    private ObjectTableModel model = null;

    private JTable table = null;
    boolean SKIP_SUFFIX =false;
    Boolean Flag1 =false;
    int count =0;

    private JTextField dataField = null;

    private JTextField averageField = null;

    private JTextField deviationField = null;

    private JTextField noSamplesField = null;

    private JScrollPane tableScrollPanel = null;

    private JCheckBox autoscroll = null;

    private JCheckBox childSamples = null;

    private transient Calculator calc = new Calculator();

    private Format format = new SimpleDateFormat("HH:mm:ss.SSS"); //$NON-NLS-1$

    // Column renderers
    private static final TableCellRenderer[] RENDERERS =
        new TableCellRenderer[]{
            new RightAlignRenderer(), // Sample number (string)
            new RightAlignRenderer(), // Start Time
            null, // Thread Name
            null, // Label
            null, // Sample Time
            null, // Status
            null, // Bytes
        };

	protected  String BASEPATH = null;
//***********dcl for el
    protected   JTextField basepath;
    protected   JTextField errorReportFileName;
    protected   JTextField logFileName;
    
    static String ERRORFILE = null;
     String PROPFILE = null;
    String FILEEXT = ".txt";
    String errorFileName = null;
    boolean headersCreated=false;
    String reportPath=null;
    String propertyPath=null;
    boolean directoryCreated=false;
    static boolean updateAllowed=true;
    public  boolean flag=false;
    static boolean warning=false;
   
    static String propertyFileExtension=".csv";
   
    private JPanel buttonPanel = new JPanel();
    TableColumn th1 = new TableColumn();
    TableColumn th2 = new TableColumn();
    TableColumn th3 = new TableColumn();
    TableColumn th4 = new TableColumn();
    TableColumn th5 = new TableColumn();
    TableColumn th6 = new TableColumn();
    TableColumn th7 = new TableColumn();
    TableColumn th8 = new TableColumn();


    private JButton browse = new JButton("Browse");
    private JButton createErrorReport = new JButton("Create new Error Report and Error Log");
    private JButton createErrorLog = new JButton("Create new Error Log");
    protected  String[]propertyHeaderArray=new String[20];

    JMeterProperty BasePath=null;
     JMeterProperty JPropertyName=null;

    private  final String BRO = "bro";
    private final String CER = "cer";
    private final String CEL = "cel";
    //TestElement ite;

    JTextField JPropertyNameField;
    CommentPanel commentPanel = new CommentPanel();

    private JLabel errorProperties = new JLabel("Preview of table headers of Error report");

    // private JTable table;
    // private DefaultTableModel model = new DefaultTableModel(); 

    private JCheckBox selectAll = new JCheckBox("Select all"); 
    private JCheckBox sampleNo  = new JCheckBox("Sample #");
    private JCheckBox startTime   = new JCheckBox("Start Time");
    private JCheckBox threadName   = new JCheckBox("Thread Name");
    private JCheckBox label  = new JCheckBox("Label");
    private JCheckBox sampleTime = new JCheckBox("Sample Time(ms)");
    private JCheckBox status = new JCheckBox("Status");
    private JCheckBox bytes  = new JCheckBox("Bytes");
    private JCheckBox latency  = new JCheckBox("Latency");
  
    

    private final String SELECTALL = "selectAll"; //p[0]
    private final String SAMPLENO = "sampleNo";  //p[1]
    private final String STARTTIME = "startTime"; //p[2] 
    private final String THREADNAME = "threadName"; //p[3]
    private final String LABEL = "label"; //p[4]
    private final String SAMPLETIME = "sampleTime"; //p[5]
    private final String STATUS = "status"; //p[6]
    private final String BYTES = "bytes"; //p[7]
    private final String LATENCY = "latency";  //p[8]

   

    protected   Boolean[] p = new Boolean[9];

    //protected int PropertyCount;

    /** Holds the count of properties defined by the user*/
     protected  int PropertyCount=0;
    
    
    
    //************ decl for el fin
    /**
     * Constructor for the TableVisualizer object.
     */
     
     public String getStaticLabel() {
    	 return "Error Listener";
    	 }
    public trial() {
        super();
        model = new ObjectTableModel(COLUMNS,
                TableSample.class,         // The object used for each row
                new Functor[] {
                new Functor("getSampleNumberString"), // $NON-NLS-1$
                new Functor("getStartTimeFormatted",  // $NON-NLS-1$
                        new Object[]{format}),
                new Functor("getThreadName"), // $NON-NLS-1$
                new Functor("getLabel"), // $NON-NLS-1$
                new Functor("getElapsed"), // $NON-NLS-1$
                new SampleSuccessFunctor("isSuccess"), // $NON-NLS-1$
                new Functor("getBytes"), // $NON-NLS-1$
                new Functor("getLatency") }, // $NON-NLS-1$
                new Functor[] { null, null, null, null, null, null, null, null },
                new Class[] {
                String.class, String.class, String.class, String.class, Long.class, ImageIcon.class, Long.class, Long.class });
        init();
        
    }
  
    public static boolean testFunctors(){
        trial instance = new trial();
        
        return instance.model.checkFunctors(null,instance.getClass());
        
    }
    /*
    public TestElement createTestElement() {
    	 
    	//modifyTestElement(errorMon);
    	//errorMon.getThreadName()
    	//JOptionPane.showMessageDialog(null, "alert",TestPlan.NAME, JOptionPane.OK_OPTION);



    	return errorMon;
    	}
    
*/
	public void sampleOcc(SampleResult e) {
		// TODO Auto-generated method stub
	
	
		
		if(!e.isSuccessful())
		{
			JOptionPane.showMessageDialog(null, "alert",Boolean.toString(flag)+e.getThreadName(), JOptionPane.OK_OPTION);
		if(flag==false)
		{
		/** Fetch and initialize the base properties*/
			reportPath="ErrorListener"+"_"+ getName()+"TimeStamp-"+GetTimeStampInHMS()+File.separator;
			count = 0;
			BASEPATH=basepath.getText().trim();
		if(null==BASEPATH || BASEPATH.isEmpty())
		{
		BASEPATH= JMeterUtils.getJMeterBinDir();	// save in bin
		}
		ERRORFILE="ErrorLog";//getPropertyAsString("ErrorFilePrefix").trim();
		if(null==ERRORFILE || ERRORFILE.isEmpty())
		{
		ERRORFILE="ErrorLog";
		}
		//PROPFILE=getPropertyAsString("PropertyFilePrefix").trim();
		
		PROPFILE=errorReportFileName.getText()+".csv";//"Error_Report_"+e.getParent()+"Timestamp"+GetTimeStampInHMS()+".csv";
		
		//getPropertyAsString("FileExtensionName").trim();
	



		/** Construct the directory path to store the error and property data*/
		StringBuilder sb = new StringBuilder(BASEPATH);
		if(!BASEPATH.endsWith(File.separator))  // separator char is//
		{
		sb.append(File.separator);
		}
		BASEPATH=sb.append(reportPath).toString();  
		PROPFILE=BASEPATH+PROPFILE;//+propertyPath+propertyFileExtension;sampleno
		
		errorFileName = makeErrorFileName(e.getContentType(),e);
		
		JOptionPane.showMessageDialog(null,PROPFILE,"PROP FILE", JOptionPane.OK_OPTION);
		flag=true;
		}
       
		/** 4 Create the directory where the files will be stored*/	
		
		File createDir = new File(BASEPATH);
		if(!createDir.exists())
		{
		createDir.mkdirs();
		directoryCreated = true;
		JOptionPane.showMessageDialog(null,BASEPATH,"dir created", JOptionPane.OK_OPTION);
		}else
		{
			directoryCreated = true;
		}
		

		if(directoryCreated==true) 
		{
		warning=false;
		processSample(e, new Counter());
		
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
	

	
    @Override
    public String getLabelResource() {
        return "view_results_in_table"; // $NON-NLS-1$
    }

    protected synchronized void updateTextFields(SampleResult res) {
        noSamplesField.setText(Long.toString(calc.getCount()));
        dataField.setText(Long.toString(res.getTime()/res.getSampleCount()));
        averageField.setText(Long.toString((long) calc.getMean()));
        deviationField.setText(Long.toString((long) calc.getStandardDeviation()));
    }

    @Override
    public void add(final SampleResult res) {  //***altered
    
    
    //	JOptionPane.showMessageDialog(null,res.getParent().getThreadName()+" and ","parent", JOptionPane.OK_OPTION);
    	if(Flag1==false)
    		headersCreated=false;
    	else
    		headersCreated=true;
    	Flag1=true;
    	
    
    	JOptionPane.showMessageDialog(null,"message",	this.collector.NAME, JOptionPane.OK_OPTION);
    	
    	sampleOcc(res);
/*
        JMeterUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                if (childSamples.isSelected()) {
                    SampleResult[] subResults = res.getSubResults();
                    if (subResults.length > 0) {
                        for (SampleResult sr : subResults) {
                            add(sr);
                        }
                        return;
                    }
                }
                synchronized (calc) {
                    calc.addSample(res);
                    int count = calc.getCount();
                    TableSample newS = new TableSample(
                            count, 
                            res.getSampleCount(), 
                            res.getStartTime(), 
                            res.getThreadName(), 
                            res.getSampleLabel(),
                            res.getTime(),
                            res.isSuccessful(),
                            res.getBytes(),
                            res.getLatency());
                    JOptionPane.showMessageDialog(null,"message", "2", JOptionPane.OK_OPTION);
                    model.addRow(newS);
                }
                updateTextFields(res);
                if (autoscroll.isSelected()) {
                    table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, 0, true));
                }
            }
        });*/
                //
               
            }
       
    public static class Counter{
    	int num;
    	}
    
    private synchronized void processSample(SampleResult s, Counter c) {
    	SaveResult saveResult=new SaveResult();
    	//String [] propertyHeaderArray = null;
    	 saveResult.SaveSampleResult(s,c.num++,BASEPATH,p,PROPFILE,FILEEXT,propertyHeaderArray,headersCreated,errorFileName,++count); // not bieng called
    	headersCreated=true;
    	
    	SampleResult[] sr = s.getSubResults();
    	for (int i = 0; i < sr.length; i++) {
    	processSample(sr[i], c);
    	}
    	}

    private String makeErrorFileName(String contentType,SampleResult s)
    {
    StringBuilder sb = new StringBuilder(BASEPATH);
    sb.append(logFileName.getText());
    JOptionPane.showMessageDialog(null,sb,"inside mk error filename", JOptionPane.OK_OPTION);
    if(null!=FILEEXT && !FILEEXT.isEmpty())
    {
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
    
    @Override
    public synchronized void clearData() {
        model.clearData();
        calc.clear();
        noSamplesField.setText("0"); // $NON-NLS-1$
        dataField.setText("0"); // $NON-NLS-1$
        averageField.setText("0"); // $NON-NLS-1$
        deviationField.setText("0"); // $NON-NLS-1$
        flag=false;
        JOptionPane.showMessageDialog(null,"message", "flag is f", JOptionPane.OK_OPTION);
        
        repaint();
    }

    @Override
    public String toString() {
        return "Show the samples in a table";
    }

    private void init() {
    	this.setLayout(new BorderLayout());

    	this.setBorder(makeBorder());
    	Box box = Box.createVerticalBox();	
    	box.add(this.makeTitlePanel());   	
    	box.add(createBasePathPanel());
    	box.add(errorProperties);
    	this.add(createTablePanel(), BorderLayout.CENTER);
    	this.add(Box.createVerticalStrut(70), BorderLayout.EAST);add(createCheckBoxPanel(),BorderLayout.WEST);
    	this.add(box, BorderLayout.NORTH);
    }
    // el functions
   
   
    
    @Override
    protected Container makeTitlePanel() {
        VerticalPanel titlePanel = new VerticalPanel();
        titlePanel.add(createTitleLabel());
        VerticalPanel contentPanel = new VerticalPanel();
        contentPanel.setBorder(BorderFactory.createEtchedBorder());
        contentPanel.add(getNamePanel());       
        contentPanel.add(commentPanel);
        titlePanel.add(contentPanel);
        return titlePanel;
    }
    
    private JPanel createCheckBoxPanel() {
    	GridLayout checkBoxGrid = new GridLayout(9,1);
    	checkBoxGrid.setHgap(0);
    	buttonPanel.setLayout(checkBoxGrid);
    	buttonPanel.add(selectAll);

    	selectAll.setActionCommand(SELECTALL);
    	selectAll.addActionListener(this);
    	selectAll.doClick();

    	buttonPanel.add(sampleNo);
    	sampleNo.setActionCommand(SAMPLENO);
    	sampleNo.addActionListener(this);

    	buttonPanel.add(startTime);
    	startTime.setActionCommand(STARTTIME);
    	startTime.addActionListener(this);

    	buttonPanel.add(threadName);
    	threadName.setActionCommand(THREADNAME);
    	threadName.addActionListener(this);

    	buttonPanel.add(label);
    	label.setActionCommand(LABEL);
    	label.addActionListener(this);

    	buttonPanel.add(sampleTime);
    	sampleTime.setActionCommand(SAMPLETIME);
    	sampleTime.addActionListener(this);

    	buttonPanel.add(status);
    	status.setActionCommand(STATUS);
    	status.addActionListener(this);

    	buttonPanel.add(bytes);
    	bytes.setActionCommand(BYTES);
    	bytes.addActionListener(this);

    	buttonPanel.add(latency);
    	latency.setActionCommand(LATENCY);
    	latency.addActionListener(this);

    	return buttonPanel;
    	}
    
    private Component createTablePanel() 
    {	
    	table = new JTable();
    	return makeScrollPane(table);
    	}
    public JPanel createBasePathPanel()
    {
    JLabel label = new JLabel("Base Directory :"); 
    JLabel label2 =new JLabel("Error Report name :  ");
    JLabel label3 =new JLabel("Error Log name       :  ");
    basepath = new JTextField(10);
    errorReportFileName =new JTextField(10);
    logFileName =new JTextField(10);
   errorReportFileName.setText("ErrorReport_"+GetTimeStampInHMS()+"_"+this.getName());
   logFileName.setText("ErrorLog_"+GetTimeStampInHMS());
   
    //basepath.setName(errorMon.BASEPATH);
   
    label.setLabelFor(basepath);
    label2.setLabelFor(createErrorReport);
    label3.setLabelFor(createErrorLog);
   
    browse.setActionCommand("bro");
    browse.setActionCommand(BRO);
   
    createErrorReport.setActionCommand(CER);
   // createErrorLog.setActionCommand(CEL);
    JPanel basepathPanel = new JPanel();
    JPanel panel1 = new JPanel(new BorderLayout());
    JPanel panel2 = new JPanel(new BorderLayout());
    JPanel panel3 = new JPanel();
    JPanel panel4 = new JPanel();
    JPanel panel5 = new JPanel(new BorderLayout());

    GridLayout basePathGrid = new GridLayout(2,1);
  //     BoxLayout basePathGrid = new BoxLayout(basepathPanel,BoxLayout.Y_AXIS);  
       
    basepathPanel.setLayout(basePathGrid);
    panel1.add(label,BorderLayout.WEST);
    panel1.add(basepath,BorderLayout.CENTER);
    panel1.add(browse,BorderLayout.EAST);
    
    panel2.add(label2, BorderLayout.WEST);
    panel2.add(errorReportFileName, BorderLayout.CENTER);
    
    panel5.add(label3, BorderLayout.WEST);
    panel5.add(logFileName, BorderLayout.CENTER);
    
    panel3.setLayout(new BorderLayout());
    panel4.setLayout(new GridLayout(2,1));
    panel3.add( createErrorReport,BorderLayout.EAST);
    panel3.add(panel4,BorderLayout.CENTER);
    panel4.add(panel2);
    panel4.add(panel5);
  
    basepathPanel.add(panel1);
    basepathPanel.add(panel3);
    //basepathPanel.add();
   /*
    panel1.add(label, BorderLayout.WEST);
    panel1.add(basepath, BorderLayout.CENTER);
    panel1.add(browse,BorderLayout.EAST);
    
   
    
    panel4.add(createErrorReport,BorderLayout.EAST);
    
    panel3.add(label3, BorderLayout.WEST);
    panel3.add(logFileName, BorderLayout.CENTER);
   // panel3.add(createErrorLog,BorderLayout.EAST);
   
    basepathPanel.add(panel1);
    basepathPanel.add(panel2);
    basepathPanel.add(panel4);
    basepathPanel.add(panel3);
    */
	browse.addActionListener(this);
	browse.setEnabled(true);
	
	createErrorReport.addActionListener(this);
	createErrorReport.setEnabled(true);
	 
//	createErrorLog.addActionListener(this);
	//createErrorLog.setEnabled(true);
	 
    return basepathPanel;
    }
    
    private static String GetTimeStampInHMS() {

    	long epoch = System.currentTimeMillis()/1000;
    	String date = new java.text.SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new java.util.Date (epoch*1000));
    	return date;

    	}
    public void actionPerformed(ActionEvent e) {
    	String action = e.getActionCommand();
    	JFileChooser j = new JFileChooser();
    	j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);


    	     if (action.equals(BRO)) {
    		//JOptionPane.showMessageDialog(null, "alert",propertyHeaderArray[0]+propertyHeaderArray[1]+propertyHeaderArray[2], JOptionPane.OK_OPTION);
    	    int returnVal = j.showOpenDialog(this);
    	    j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    	    if (returnVal == JFileChooser.APPROVE_OPTION) {
    	    	basepath.setText(j.getSelectedFile().getAbsolutePath());
    	    	BASEPATH=basepath.getText().trim();
    	    	JOptionPane.showMessageDialog(null, "alert",basepath.getText(), JOptionPane.OK_OPTION);
    	    flag=false;
    	    	
    	    	
    	    }
    	    }
    	     else if (action.equals(CER))
    	     {
    	    	 errorReportFileName.setText("ErrorReport_"+GetTimeStampInHMS());
    	    	 logFileName.setText("ErrorLog_"+GetTimeStampInHMS()); 
    	    	 flag=false;
    	     }
    	    /* else if (action.equals(CEL))
    	     {
    	    	 logFileName.setText("ErrorLog_"+GetTimeStampInHMS()); 
    	    	 flag=false;
    	     }
    	     */
    	else if(action.equals(SELECTALL))
    	{
    		
    		if(selectAll.isSelected())
    		
    			{
    			
    			for(int i=0;i<9;i++)
    			{
    				p[i]=true;
    			}

    			}
    		else
    		{
    			//p[0]=false;
    			for(int i=0;i<9;i++)
    			{
    				p[i]=false;
    			}

    		}
    			sampleNo.setSelected(p[0]);
    	     	startTime.setSelected(p[0]);
    	     	threadName.setSelected(p[0]);
    	        label.setSelected(p[0]);
    	     	sampleTime.setSelected(p[0]);
    	     	status.setSelected(p[0]);
    	     	bytes.setSelected(p[0]);
    	     	latency.setSelected(p[0]);
    	     	
    	     	
    	     	sampleNo.setEnabled(!p[0]);
    	     	startTime.setEnabled(!p[0]);
    	     	threadName.setEnabled(!p[0]);
    	     	label.setEnabled(!p[0]);
    	     	sampleTime.setEnabled(!p[0]);
    	     	status.setEnabled(!p[0]);
    	     	bytes.setEnabled(!p[0]);
    	     	latency.setEnabled(!p[0]);
    	     	
    	    
    	     	
    	     	/*properties 
    	     	sampleNo
    	     	startTime
    	     	threadName
    	     	label
    	     	sampleTime
    	     	status
    	     	bytes
    	     	latency
    	     	*/
    	     	
    		}
    		

    	else if(action.equals(SAMPLENO))
    	{
    		if(sampleNo.isSelected())
    			p[1] = true;
    		else
    			p[1]=false;	
    	}
    	else if(action.equals(STARTTIME))
    	{
    		if(startTime.isSelected()==true)
    			p[2] = true;
    		else
    			p[2]=false;
    	}
    	else if(action.equals(THREADNAME))
    	{
    		if(threadName.isSelected()==true)
    			p[3] = true;
    		else
    			p[3]=false;
    	}
    	else if(action.equals(LABEL))
    	{
    		if(label.isSelected()==true)
    			p[4] = true;
    		else
    			p[4]=false;
    	}
    	else if(action.equals(SAMPLETIME))
    	{
    		if(sampleTime.isSelected()==true)
    			p[5] = true;
    		else
    			p[5]=false;
    	}
    	else if(action.equals(STATUS))
    	{
    		if(status.isSelected()==true)
    			p[6] = true;
    		else
    			p[6]=false;
    	}
    	else if(action.equals(BYTES))
    	{
    		if(bytes.isSelected()==true)
    			p[7]= true;
    		else
    			p[7]=false;
    	}
    	else if(action.equals(LATENCY))
    	{
    		if(latency.isSelected()==true)
    			p[8] = true;
    		else
    			p[8]=false;
    	}
    	createTable(); 
    	headersCreated=false;
    	
    	
    	//errorMon.getdata(basepath.getText().trim(),p);
    	}

    protected void createTable()
    {  	
    	table.removeColumn(th1);
    	table.removeColumn(th2);
    	table.removeColumn(th3);
    	table.removeColumn(th4);
    	table.removeColumn(th5);
    	table.removeColumn(th6);
    	table.removeColumn(th7);
    	table.removeColumn(th8);
    	
    	PropertyCount=0;
    	for(int i=0;i<10;i++)
    	{
    		propertyHeaderArray[i]=null;
    	}

    	if(p[1])
    	{
    		th1.setHeaderValue("Sample #");
    		table.addColumn(th1);
    		propertyHeaderArray[PropertyCount]="Sample #";
    		++PropertyCount;
    		}
    	else
    		table.removeColumn(th1);
    	if(p[2])
    	{	
    		th2.setHeaderValue("Start Time");
    		table.addColumn(th2);
    		propertyHeaderArray[PropertyCount]="Start Time";
    		++PropertyCount;
    		}
    	else
    		table.removeColumn(th2);
    	if(p[3])
    	{
    		th3.setHeaderValue("Thread Name");
    		table.addColumn(th3);
    	
    		propertyHeaderArray[PropertyCount]="Thread Name";
    		++PropertyCount;
    		}
    	else
    		table.removeColumn(th3); 
    	if(p[4])
    	{
    		th4.setHeaderValue("Label");
    		table.addColumn(th4);
    		propertyHeaderArray[PropertyCount]="Label";
    		++PropertyCount;
    		}
    	else
    		table.removeColumn(th4); 
    	if(p[5])
    	{
    		th5.setHeaderValue("Sample Time(ms)");
    		table.addColumn(th5);
    		propertyHeaderArray[PropertyCount]="Sample Time(ms)";
    		++PropertyCount;
    		}
    	else
    		table.removeColumn(th5);
    	if(p[6])
    	{
    		th6.setHeaderValue("Status");
    		table.addColumn(th6);
    		propertyHeaderArray[PropertyCount]="Status";
    		++PropertyCount;
    		}
    	else
    		table.removeColumn(th6);	
    	if(p[7])
    	{
    		th7.setHeaderValue("Bytes");
    		table.addColumn(th7);
    		propertyHeaderArray[PropertyCount]="Bytes";
    		++PropertyCount;
    		}
    	else
    		table.removeColumn(th7);
    	if(p[8])
    	{
    		th8.setHeaderValue("Latency");
    		table.addColumn(th8);
    		propertyHeaderArray[PropertyCount]="Latency";
    		++PropertyCount;
    		}
    	else
    		table.removeColumn(th8);	
    	}
    
    
    // el functions fin
    public static class SampleSuccessFunctor extends Functor {
        public SampleSuccessFunctor(String methodName) {
            super(methodName);
        }

        @Override
        public Object invoke(Object p_invokee) {
            Boolean success = (Boolean)super.invoke(p_invokee);

            if(success != null) {
                if(success.booleanValue()) {
                    return imageSuccess;
                }
                else {
                    return imageFailure;
                }
            }
            else {
                return null;
            }
        }
    }

	
	
	
		
	}

	

	
	

	

