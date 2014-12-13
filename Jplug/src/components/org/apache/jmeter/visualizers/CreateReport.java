/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.net.ntp.TimeStamp;
import org.apache.jmeter.gui.CommentPanel;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.util.Calculator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.NumberRenderer;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.gui.RateRenderer;
import org.apache.jorphan.gui.RendererUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.Functor;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.Base64.OutputStream;
import com.itextpdf.text.Image;
/**
 * Simpler (lower memory) version of Aggregate Report (StatVisualizer).
 * Excludes the Median and 90% columns, which are expensive in memory terms
 */
public class CreateReport extends NewAbstractVisualizer implements Clearable, ActionListener {

    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String USE_GROUP_NAME = "useGroupName"; //$NON-NLS-1$

    private static final String SAVE_HEADERS   = "saveHeaders"; //$NON-NLS-1$
    protected String [] imgFiles = {"average.jpg","samplesProcessed.jpg","errorPercentage.jpg","throughput.jpg","ninetyPline.jpg"};
  
    private static final String[] COLUMNS = {
    	 "sampler_label",               //$NON-NLS-1$
         "aggregate_report_count",      //$NON-NLS-1$
         "average",                     //$NON-NLS-1$
         "aggregate_report_min",        //$NON-NLS-1$
         "aggregate_report_max",        //$NON-NLS-1$
         "aggregate_report_stddev",     //$NON-NLS-1$
         "aggregate_report_error%",     //$NON-NLS-1$
         "aggregate_report_rate",       //$NON-NLS-1$
         "aggregate_report_bandwidth",  //$NON-NLS-1$
         "average_bytes",               //$NON-NLS-1$      //$NON-NLS-1$
            };
    private String [] cols = {
    		"Label",               //$NON-NLS-1$
            "#Samples",      //$NON-NLS-1$
            "Average",                     //$NON-NLS-1$
            "Min",        //$NON-NLS-1$
            "Max",        //$NON-NLS-1$
            "Std. Dev.",     //$NON-NLS-1$
            "Error %",     //$NON-NLS-1$
            "Throughput",       //$NON-NLS-1$
            "KB/sec",  //$NON-NLS-1$
            "Avg.Bytes"
    };
    
            
    private static Boolean testRunning= false;

    private final String TOTAL_ROW_LABEL
        = JMeterUtils.getResString("aggregate_report_total_label");  //$NON-NLS-1$

    private JTable myJTable;    
    private JScrollPane myScrollPane;

    private final JButton saveTable =
        new JButton(JMeterUtils.getResString("aggregate_graph_save_table"));            //$NON-NLS-1$

    private final JCheckBox saveHeaders = // should header be saved with the data?
        new JCheckBox(JMeterUtils.getResString("aggregate_graph_save_table_header"),true);    //$NON-NLS-1$

    private final JCheckBox useGroupName =
        new JCheckBox(JMeterUtils.getResString("aggregate_graph_use_group_name"));            //$NON-NLS-1$

    private final String BROWSE = "browse";
    private final String VIEWTESTHISTORY = "viewTestHistory";
    private final String COMPARETESTREPORTS = "compareTestReports";
    private final JButton browse = new JButton("Browse");
    private JLabel selectDir = new JLabel("Select directory to store Reports");
    private final JButton viewTestHistory = new JButton("View Test History");                        
    private final JButton compareTestReports = new JButton("Compare Test Summary Reports");
    private final JTextField basepath = new JTextField();
    private String BASEPATH = new String();
    private String summaryCsvFile = new String();
    private String errorCsvFile = new String();
  
    private String reportPdfFile = new String();
    
    //decl for pdf
    private  Font headerFont = new Font(Font.FontFamily.COURIER, 12,
    	      Font.BOLD);
    	  private  Font rowFont = new Font(Font.FontFamily.COURIER, 11,
    	      Font.NORMAL); 
    	  static BaseColor clr= new BaseColor(0, 44, 94);
    	  private static Font subFont = new Font(Font.FontFamily.HELVETICA, 16,
    	      Font.BOLD,clr);
    	 
    	  private static Font small = new Font(Font.FontFamily.COURIER, 12);
    //ends
    
    JCheckBox saveAsCsv = new JCheckBox("Save Summary as CSV");
    JCheckBox saveAsPdf = new JCheckBox("Save Summary as PDF");
    
    //decl for saving the errors messsages
     protected DefaultTableModel model2 = new DefaultTableModel();
     protected DefaultTableModel model3 = new DefaultTableModel();
     private int errorNo;
     private int numSamples;
    //
    
    protected ObjectTableModel model;

    /**
     * Lock used to protect tableRows update + model update
     */
    private final transient Object lock = new Object();
    CommentPanel commentPanel = new CommentPanel();

    private final Map<String, Calculator> tableRows =
        new ConcurrentHashMap<String, Calculator>();

    // Column renderers
    private static final TableCellRenderer[] RENDERERS =
        new TableCellRenderer[]{
            null, // Label
            null, // count
            null, // Mean
            null, // Min
            null, // Max
            new NumberRenderer("#0.00"), // Std Dev.
            new NumberRenderer("#0.00%"), // Error %age
            new RateRenderer("#.0"),      // Throughput
            new NumberRenderer("#0.00"),  // kB/sec
            new NumberRenderer("#.0"),    // avg. pageSize
        };

    public String getStaticLabel() {
   	 return "Create Report";
   	 }
    public CreateReport() {
    	
        super();
        
        initialize();
        createErrorTable();
        clearData();
        init();
        
       
    }
    public void initialize()
    {
    	model = new ObjectTableModel(COLUMNS,
                Calculator.class,// All rows have this class
                new Functor[] {
                    new Functor("getLabel"),              //$NON-NLS-1$
                    new Functor("getCount"),              //$NON-NLS-1$
                    new Functor("getMeanAsNumber"),       //$NON-NLS-1$
                    new Functor("getMin"),                //$NON-NLS-1$
                    new Functor("getMax"),                //$NON-NLS-1$
                    new Functor("getStandardDeviation"),  //$NON-NLS-1$
                    new Functor("getErrorPercentage"),    //$NON-NLS-1$
                    new Functor("getRate"),               //$NON-NLS-1$
                    new Functor("getKBPerSecond"),        //$NON-NLS-1$
                    new Functor("getAvgPageBytes"),       //$NON-NLS-1$
                },
                
                new Functor[] { null, null, null, null, null, null, null, null , null, null },
                new Class[] { String.class, Long.class, Long.class, Long.class, Long.class,
                              String.class, String.class, String.class, String.class, String.class });
    	basepath.setText(JMeterUtils.getJMeterBinDir());
        BASEPATH=basepath.getText().trim();
    	
    }

    /** @deprecated - only for use in testing */
    @Deprecated
    public static boolean testFunctors(){
        SummaryReport instance = new SummaryReport();
        return instance.model.checkFunctors(null,instance.getClass());
    }
    public static void teststarted()
    {
    	JOptionPane.showMessageDialog(null, "test started","test started", JOptionPane.OK_OPTION);
    	
       
    }
    public static void teststopped()
    {
    	testRunning = false;
    	
    }
/*
 * 
    public TestElement createTestElement() {
         TestNotifier tn = new TestNotifier();    	
    	JOptionPane.showMessageDialog(null, "alert",TestPlan.NAME, JOptionPane.OK_OPTION);



    	return tn;
    	}
    	*/
    @Override
    public String getLabelResource() {
        return "save_report";  //$NON-NLS-1$
    }
    private  String GetTimeStampInHMS() {

    	long epoch = System.currentTimeMillis()/1000;
    	String date = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date (epoch*1000));
    	return date;

    	}
    @Override
    public void add(final SampleResult res) {
    	if(testRunning==false)
    	{
    		
    		numSamples=1;
    		errorNo=0;
    		BASEPATH=BASEPATH+"\\"+this.getName()+"_"+GetTimeStampInHMS();
    		try {
				File createDir = new File(BASEPATH);
  	
				createDir.mkdirs();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		 summaryCsvFile=BASEPATH+"\\"+"Summary_Report"+"_"+GetTimeStampInHMS() +".csv";
    		 errorCsvFile=BASEPATH+"\\"+"Error_Report"+"_"+GetTimeStampInHMS() +".csv";
    		 reportPdfFile=BASEPATH+"\\"+"TestReport"+"_"+GetTimeStampInHMS() +".pdf";
    		 JOptionPane.showMessageDialog(null, summaryCsvFile,"test started", JOptionPane.OK_OPTION);
    	    
    		testRunning = true;
    	}
    	
         final String sampleLabel = res.getSampleLabel(useGroupName.isSelected());
        JMeterUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                Calculator row = null;
                synchronized (lock) {
                    row = tableRows.get(sampleLabel);
                    if (row == null) {
                        row = new Calculator(sampleLabel);
                        tableRows.put(row.getLabel(), row);
                        model.insertRow(row, model.getRowCount() - 1);                        
                    }
                    if(!res.isSuccessful())
                    {             synchronized(lock){
                    		addErrorToTable(res);
                    }
                    }
                    saveTable();
                }
                /*
                 * Synch is needed because multiple threads can update the counts.
                 */
                synchronized(row) {
                    row.addSample(res);
                }
                Calculator tot = tableRows.get(TOTAL_ROW_LABEL);
                synchronized(tot) {
                    tot.addSample(res);
                }
                model.fireTableDataChanged();                
            }
        });
    }

    /**
     * Clears this visualizer and its model, and forces a repaint of the table.
     */
    @Override
    public void clearData() {
        //Synch is needed because a clear can occur while add occurs
        synchronized (lock) {
            model.clearData();
            tableRows.clear();
            tableRows.put(TOTAL_ROW_LABEL, new Calculator(TOTAL_ROW_LABEL));
            model.addRow(tableRows.get(TOTAL_ROW_LABEL));
        }
    }

    /**
     * Main visualizer setup.
     */
    private void init() {
        this.setLayout(new BorderLayout());

        // MAIN PANEL
        JPanel mainPanel = new JPanel();
      
        Border margin = new EmptyBorder(10, 10, 5, 10);

        mainPanel.setBorder(margin);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        mainPanel.add(makeTitlePanel());
        mainPanel.add(createSavePanel());
        myJTable = new JTable(model);
        myJTable.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        myJTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        RendererUtils.applyRenderers(myJTable, RENDERERS);
        myScrollPane = new JScrollPane(myJTable);
        this.add(mainPanel, BorderLayout.NORTH);
        this.add(myScrollPane, BorderLayout.CENTER);
        saveTable.addActionListener(this);
        JPanel opts = new JPanel();
        opts.add(useGroupName, BorderLayout.WEST);
        opts.add(saveTable, BorderLayout.CENTER);
        opts.add(saveHeaders, BorderLayout.EAST);
        this.add(opts,BorderLayout.SOUTH);
    }
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

    @Override
    public void modifyTestElement(TestElement c) {
        super.modifyTestElement(c);
        c.setProperty(USE_GROUP_NAME, useGroupName.isSelected(), false);
        c.setProperty(SAVE_HEADERS, saveHeaders.isSelected(), true);
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        useGroupName.setSelected(el.getPropertyAsBoolean(USE_GROUP_NAME, false));
        saveHeaders.setSelected(el.getPropertyAsBoolean(SAVE_HEADERS, true));
    }
/*
    private Component createTablePanel() 
    {	
    	table = new JTable();
    	return makeScrollPane(table);
    	}
    	*/
    
    public void createErrorTable()
    {
    	model2.addColumn("Error #");    	
    	model2.addColumn("Label");
    	model2.addColumn("# Samples");
    	model2.addColumn("Error Response Message");
    
    }
    
    
    public  synchronized void addErrorToTable(SampleResult res)
    {
    	Vector e =new Vector (4); 
    	//JOptionPane.showMessageDialog(null, Integer.toString(errorExists(res.getSampleLabel(),res.getResponseMessage())) +res.getResponseMessage(),"test started", JOptionPane.OK_OPTION);
    	if(errorExists(res.getSampleLabel(),res.getResponseMessage())==-1)
    	{
    		//String [] row ={Integer.toString(errorNo+1),Integer.toString(numSamples),res.getSampleLabel(),res.getResponseMessage().toString()};
    		e.add(errorNo+1);
    		e.add(res.getSampleLabel());
    		e.add(1);    		
    		e.add(res.getResponseMessage());
    		
    	//	JOptionPane.showMessageDialog(null, e.toString(),"test started", JOptionPane.OK_OPTION);
        	
    		model2.addRow(e);
    	    ++errorNo;
    	}
    	else
    	{
    		int x=errorExists(res.getSampleLabel(),res.getResponseMessage());  
    	
    		model2.setValueAt(1+(int)model2.getValueAt(x,2),x, 2);
    		
    	}
             	
    }
    public synchronized int errorExists( String label,String message)
    {
    	
    	if(model2.getRowCount()==0)
    		return -1;
    	for(int i=0;i<model2.getRowCount();i++)
    	{
    	//	JOptionPane.showMessageDialog(null, label+model2.getValueAt(i,2).toString()+message+model2.getValueAt(i,3).toString(),"test started", JOptionPane.OK_OPTION);
    		if(model2.getValueAt(i,1).toString().compareToIgnoreCase(label)==0 && model2.getValueAt(i,3).toString().compareToIgnoreCase(message)==0)
    			return i;
    	}
    	return -1;
    }
    
            	
    
   
    public void  saveTable() 
    {
    	myJTable.getRowCount();
    	myJTable.getColumnCount();
    //	JOptionPane.showMessageDialog(null,JMeterUtils.getResString("test_fragment_title")+JMeterUtils.getResString("thread_group_title")+"row Count -"+Integer.toString(myJTable.getRowCount())+"  colm count -"+Integer.toString(myJTable.getColumnCount()),"srtysrt", JOptionPane.OK_OPTION);
    	if(saveAsCsv.isSelected()) 
    	{
    	/*
    		JFileChooser chooser = FileDialoger.promptToSaveFile("summary.csv");//$NON-NLS-1$
         if (chooser == null) {
             return;
         }
         */
        
    		FileWriter writer = null;
         try {
             writer = new FileWriter(summaryCsvFile);
             CSVSaveService.saveCSVStats(model,writer, saveHeaders.isSelected());
         } catch (FileNotFoundException e) {
             log.warn(e.getMessage());
         } catch (IOException e) {
             log.warn(e.getMessage());
         } finally {
             JOrphanUtils.closeQuietly(writer);
         }
    	}
        if(saveAsPdf.isSelected())
        {
        /*	JFileChooser chooser = FileDialoger.promptToSaveFile("summary.pdf");//$NON-NLS-1$
            if (chooser == null) {
                return;
            }*/
            FileWriter writer = null;
            try {
            	
               // writer = new FileWriter(reportPdfFile);
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(reportPdfFile));
                createPdf(reportPdfFile,document);
                /*document.open();
                addMetaData(document);
                addTitlePage(document);
                addContent(document);
                document.close();
                */
                
                
              } catch (Exception e) {
                e.printStackTrace();
              }
            
            finally
            {
            	
            }
            
        }
       /* JFileChooser chooser = FileDialoger.promptToSaveFile("Errors.csv");//$NON-NLS-1$
        if (chooser == null) {
            return;
        }*/
        FileWriter writer = null;
        try {
            writer = new FileWriter(errorCsvFile);       
            CSVSaveService.saveCSVStats(model2,writer, saveHeaders.isSelected());
        } catch (FileNotFoundException e) {
            log.warn(e.getMessage());
        } catch (IOException e) {
            log.warn(e.getMessage());
        } finally {
            JOrphanUtils.closeQuietly(writer);
        }
    	                            
    }
    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
          paragraph.add(new Paragraph(" "));
        }
      }
//writing to pdf fns
    public void createPdf(String filename, Document document2)
            throws IOException, DocumentException {
        	// step 1
            Document document = new Document();
            // step 2
          //  OutputStream outputStream = new OutputStream(new FileOutputStream(filename));
            PdfWriter.getInstance(document,new FileOutputStream(filename));
            // step 3
            document.open();
            // step 4
           
 
              Font titleFont = new Font(Font.FontFamily.HELVETICA, 30,
          	      Font.BOLD,clr);//BaseColor.Color.getHSBColor(258, 100, 13));
            Paragraph emptyLine = new Paragraph();
            addEmptyLine(emptyLine, 2);
            Paragraph title = new Paragraph("Test Report",titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            
            BaseColor BC = new BaseColor(164,188,196) ;
    	   
           
    	    
            document.add(title);
            document.add(emptyLine);
            emptyLine = new Paragraph(" ");
            Date dNow = new Date( );
            SimpleDateFormat ft = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
        //    addEmptyLine(emptyLine, 0);
            document.add(new Paragraph("Report created on : "+ft.format(dNow),small));
            document.add(emptyLine);   
            document.add(emptyLine);   
            document.add(emptyLine);   
            document.add(new Paragraph("Test Summary Report",subFont));
           // document.add(emptyLine);
            PdfPTable table = createTable1();
           
            PdfPTable table2 = createTable2();
            document.add(new Paragraph(" ")); 
            document.add(emptyLine);   
            document.add(table);
            document.add(emptyLine);   
            document.add(emptyLine);
            document.add(emptyLine);   
             document.add(new Paragraph("Test Error Report",subFont));
             document.add(emptyLine);
             document.add(emptyLine);   
            document.add(table2);           
            table.setSpacingBefore(5);
            table.setSpacingAfter(5); 
            table2.setSpacingBefore(5);
            table2.setSpacingAfter(5); 
            // step 5
            //adding  graphs
            
            //PdfWriter writer ;
          //  writer = PdfWriter.getInstance(document, outputStream);
        Image[] img= new Image[5] ;
           
        img[0] = createGraphs("Average Response Time of samples for each Request","Average(ms)",2); // df *add 90%line and no of samples to csv summary model table
        img[1] = createGraphs("Number of samples processed for each Request","Number of Samples",2);
        img[2] = createGraphs("Error % of samples for each Request","Error %",6);
        img[3] = createGraphs("Throughput of samples for each Request","Throughput(ms)",7);
        img[4] = createGraphs("90 % Line of samples for each Request","90% line(ms)",2);
           
           // document.add(image1);

            document.add(img[0]);
           // document.
            
            
           //release resources 
            document.close();
            document = null;
             
        
            
        }
     
        /**
         * Creates a table; widths are set with setWidths().
         * @return a PdfPTable
         * @throws DocumentException
         */
        public  PdfPTable createTable1() throws DocumentException {
        	PdfPTable table = new PdfPTable(7);
       
            table.setWidthPercentage(570 / 5.23f);
            table.setWidths(new int[]{4,4,3,3,3,3,5});
            PdfPCell cell = null;
            for(int k=0;k<10;k++)
            {
            	if(k!=5&&k!=8&&k!=9)
            	{
            		cell = new PdfPCell(new Phrase(JMeterUtils.getResString(COLUMNS[k]),headerFont));         	
            	    BaseColor BC = new BaseColor(164,188,196) ;
            	    cell.setBackgroundColor(BC);
                    cell.setColspan(1);
                    table.addCell(cell);
            	}
            }
           
            	for(int l=0;l<model.getRowCount();l++)  //row count does not include table headers
            		for(int k=0;k<10;k++)
            {
            			if(k!=5&&k!=8&&k!=9)
            				{
            				cell = new PdfPCell(new Phrase(model.getValueAt(l, k).toString(),rowFont)); 
            				cell.setGrayFill(2);
            				table.addCell(cell);
            				}
            }
            return table;
        	
        	/* original basic statements
        	PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(288 / 5.23f);
            table.setWidths(new int[]{2, 1, 1});
            PdfPCell cell;
            cell = new PdfPCell(new Phrase("Table 1"));
            cell.setColspan(3);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("Cell with rowspan 2"));
            cell.setRowspan(2);
            table.addCell(cell);
            table.addCell("row 1; cell 1");
            table.addCell("row 1; cell 2");
            table.addCell("row 2; cell 1");
            table.addCell("row 2; cell 2");
            return table;
            */
        }
        
        public  PdfPTable createTable2() throws DocumentException {
        	PdfPTable table = new PdfPTable(4);
       
            table.setWidthPercentage(570 / 5.23f);
            table.setWidths(new int[]{1,2,1,3});
            PdfPCell cell = null;
            for(int k=0;k<4;k++)
            {
            	{
            		cell = new PdfPCell(new Phrase(model2.getColumnName(k),headerFont));         	
            	    BaseColor BC = new BaseColor(164,188,196) ;
            	    cell.setBackgroundColor(BC);
                    cell.setColspan(1);
                    table.addCell(cell);
            	}
            }
           
            	for(int l=0;l<model2.getRowCount();l++)  //row count does not include table headers
            		for(int k=0;k<4;k++)
            {
            			
            				cell = new PdfPCell(new Phrase(model2.getValueAt(l, k).toString(),rowFont)); 
            				cell.setGrayFill(2);
            				table.addCell(cell);
            				
            }
            return table;
        }
     
        /**
         * Creates a table; widths are set with setWidths().
         * @return a PdfPTable
         * @throws DocumentException
         */
        
        //createGraphs("Average Response Time of samples for each Request","Average(ms)",2);
        public Image createGraphs(String chartName,String colName,int colNo)
        {
        	 DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        	 for(int i=0;i<model.getRowCount();i++) 
        	 dataset.setValue((long)model.getValueAt(i, colNo), "Average",model.getValueAt(i, 0).toString());
        	 ChartFactory.setChartTheme(StandardChartTheme.createDarknessTheme());
        
        	 
        	 final JFreeChart chart = ChartFactory.createBarChart3D(
        	            chartName,      // chart title
        	            "Requests",               // domain axis label
        	              
        	            "Average (ms)",                  // range axis label
        	            dataset,                  // data
        	            PlotOrientation.VERTICAL, // orientation
        	            true,                     // include legend
        	            true,                     // tooltips
        	            false                     // urls
        	        );
                      
        	        final CategoryPlot plot = chart.getCategoryPlot();
        	        final CategoryAxis axis = plot.getDomainAxis();
        	        axis.setCategoryLabelPositions(
        	            CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 8.0)
        	        );
        	        final BarRenderer3D renderer = (BarRenderer3D) plot.getRenderer();
        	        renderer.setDrawBarOutline(false);
        	        renderer.setMaximumBarWidth(0.05);
        	       
        	       
        	        
        	        
					try {
						//java.io.OutputStream out= new OutputStream(new FileOutputStream(BASEPATH+"//MyFile.jpg"));
						ChartUtilities.saveChartAsJPEG(new File(BASEPATH+"//MyFile.jpg"), chart, 500, 400);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
           
					
					
					Image img=null;
        		 try {
					     img= Image.getInstance(BASEPATH+"//MyFile.jpg");
				} catch (BadElementException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            
        	return img;
        }
        		
        	
        
     
// ends
    
    public JPanel createSavePanel()
    {
    
    
    
    saveAsCsv.setSelected(true);
   
	browse.setActionCommand(BROWSE);
	browse.addActionListener(this);
	
   
   // createErrorLog.setActionCommand(CEL);
   JPanel basepathPanel = new JPanel();
    JPanel panel1 = new JPanel(new GridLayout(1,3));
    JPanel panel2 = new JPanel();
    JPanel panel3 = new JPanel(new BorderLayout());
    JPanel panel5 = new JPanel(new BorderLayout());
    JPanel panel4 = new JPanel(new BorderLayout());

    GridLayout grid = new GridLayout(1,2);
    grid.setHgap(50);
    panel2.setLayout(grid);

    panel1.add(saveAsCsv);
    panel1.add(saveAsPdf);
  // panel1.add(save);
    
    panel2.add(viewTestHistory);
    panel2.add(compareTestReports);
    panel3.add(panel1,BorderLayout.NORTH);
    panel3.add(panel2,BorderLayout.SOUTH);
    panel5.add(selectDir,BorderLayout.WEST);
    panel5.add(basepath,BorderLayout.CENTER);
    panel5.add(browse,BorderLayout.EAST);
  
	viewTestHistory.addActionListener(this);
	viewTestHistory.setEnabled(true);
	
	compareTestReports.addActionListener(this);
	compareTestReports.setEnabled(true);
	 
    panel4.add(panel5,BorderLayout.NORTH);
    panel4.add(panel3,BorderLayout.SOUTH);
	 
    return panel4;
    }
    
    
    @Override
    public void actionPerformed(ActionEvent ev) { 
      /*
    	if(ev.equals(SAVEASCSV))
        {
        	saveAsCsv.setSelected(true);
        }
        else if (ev.equals(SAVEASPDF)
        		{
        	saveAsPdf.setSelected(true);
        		}
    	*/
    	
    	String action = ev.getActionCommand();
    	
    	if(action.equals(BROWSE))
    	{
    		
    		JFileChooser j = new JFileChooser();
    		j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    		 int returnVal = j.showOpenDialog(this);
     	   
     	    if (returnVal == JFileChooser.APPROVE_OPTION) {
     	    	basepath.setText(j.getSelectedFile().getAbsolutePath());
     	    	BASEPATH=basepath.getText().trim();
     	    	JOptionPane.showMessageDialog(null, "alert",basepath.getText(), JOptionPane.OK_OPTION);
    		
    	}
    	if (ev.getSource() == saveTable) {
            JFileChooser chooser = FileDialoger.promptToSaveFile("summary.csv");//$NON-NLS-1$
            if (chooser == null) {
                return;
            }
            FileWriter writer = null;
            try {
                writer = new FileWriter(chooser.getSelectedFile());
                CSVSaveService.saveCSVStats(model,writer, saveHeaders.isSelected());
            } catch (FileNotFoundException e) {
                log.warn(e.getMessage());
            } catch (IOException e) {
                log.warn(e.getMessage());
            } finally {
                JOrphanUtils.closeQuietly(writer);
            }
        }
    }

 }
    
}


class ErrorSample
{
	public int errorNo;
	public int numSamples;
    public String label;
    public String errorResponse;
    ErrorSample()
    {
    }
    ErrorSample(int en,int ns,String lbl,String eR)
    {
    	errorNo=en;;
    	numSamples=ns;
    	label=lbl;
    	errorResponse=eR;
    }
    	
    
}

