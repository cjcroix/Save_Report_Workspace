
package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import org.apache.jmeter.gui.CommentPanel;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.SamplingStatCalculator;
import org.apache.jorphan.gui.NumberRenderer;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.gui.RateRenderer;
import org.apache.jorphan.gui.RendererUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.Functor;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

import com.itextpdf.text.DocumentException;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Aggregate Table-Based Reporting Visualizer for JMeter. It also Provides the
 * interface to the following functionalities </p>1. Extracting a PDF report
 * from a saved CSV summary report </p>2. Comparing 2 saved CSV summaries and
 * generating PDF Comparison report </p>3. Viewing test history related to this
 * plugin </p>4. Specifying SMS and Email settings for sending a notification on
 * test completion </p> Thanks to all those who've done the other visualizers
 * ahead of me from who I borrowed code to start with creating this plugin.
 * 
 * @author SARP Project Group 2014
 * 
 */
public class SaveReport extends NewAbstractVisualizer implements Serializable,HyperlinkListener,
Clearable, ActionListener {

	private static final long serialVersionUID = 240L;

	private static final Logger log = LoggingManager.getLoggerForClass();

	private static final String USE_GROUP_NAME = "useGroupName"; //$NON-NLS-1$

	private static final String SAVE_HEADERS = "saveHeaders"; //$NON-NLS-1$

	private boolean firstIteration = true;
	static final String[] COLUMNS = { "sampler_label", //$NON-NLS-1$
		"aggregate_report_count", //$NON-NLS-1$
		"average", //$NON-NLS-1$
		"aggregate_report_median", //$NON-NLS-1$
		"aggregate_report_90%_line", //$NON-NLS-1$
		"aggregate_report_min", //$NON-NLS-1$
		"aggregate_report_max", //$NON-NLS-1$
		"aggregate_report_error%", //$NON-NLS-1$
		"aggregate_report_rate", //$NON-NLS-1$
	"aggregate_report_bandwidth" }; //$NON-NLS-1$

	private final String TOTAL_ROW_LABEL = JMeterUtils
			.getResString("aggregate_report_total_label"); //$NON-NLS-1$
	private JTextField csvFileSource;
	private JTextField extractedReportDir;
	private String comparisonOutputDir = new String();
	private JTable myJTable;
	public static boolean TestEnded = false;
	private JScrollPane myScrollPane;
	private final JButton saveTable = new JButton(
			JMeterUtils.getResString("aggregate_graph_save_table")); //$NON-NLS-1$
	private final JCheckBox saveHeaders = // should header be saved with the
			// data?
			new JCheckBox(
					JMeterUtils.getResString("aggregate_graph_save_table_header"), true); //$NON-NLS-1$
	private final JCheckBox useGroupName = new JCheckBox(
			JMeterUtils.getResString("aggregate_graph_use_group_name")); //$NON-NLS-1$
	private JFrame pdfExtractionFrame = new JFrame();
	private JFrame comparisonReportFrame = new JFrame();
	private JFrame messageFrame = new JFrame();
	private JFrame emailFrame = new JFrame();

	private transient ObjectTableModel model;
	private JButton setEmailsButton = new JButton("SMS and E-Mail settings");
	private JButton helpButton = new JButton("Plugin Help");

	/**
	 * Lock used to protect tableRows update + model update
	 */
	private final transient Object lock = new Object();
	private String StartDateTime = new String();
	private String EndDateTime = new String();
	private final Map<String, SamplingStatCalculator> tableRows = new ConcurrentHashMap<String, SamplingStatCalculator>();
	private static Boolean testRunning = false; //$NON-NLS-1$
	private File csvFile;
	private File CsvFile1;
	private File CsvFile2;
	public JTextField csvFileSource1;
	public JTextField csvFileSource2;
	public JTextField comparisonReportDir;
	private final String BROWSE = "browse";
	private final String VIEWTESTHISTORY = "viewTestHistory";
	private final String COMPARETESTREPORTS = "compareTestReports";
	private final String EXTRACTTOPDF = "extractToPdf";
	private final JButton browse = new JButton("Browse");
	private final JButton viewTestHistory = new JButton("View Test History");
	private final JButton compareTestReports = new JButton(
			"Compare Test Summary Reports");
	private final JButton extractToPdf = new JButton(
			"Extract a PDF report from CSV");
	private final JTextField basepath = new JTextField();
	private String BASEPATH = new String();
	private String summaryCsvFile = new String();
	private String errorCsvFile = new String();
	JCheckBox saveAsPdf = new JCheckBox("Save Summary as PDF");
	private DefaultTableModel errorTableModel = new DefaultTableModel();
	private int errorNo;
	private DefaultListModel<String> listModel;
	private String addIDString = "addId", removeIDString = "removeID",
			doneString = "Done";
	private JTextField emailField = new JTextField();
	private JTextField mobileNoField;
	private JList<String> list;
	private JButton addButton;
	private JButton removeButton;
	private JButton doneButton;
	private String mobileNumber;
	private String setEmailsString = "setEmails";
	private String helpString = "help";
	private JTextPane helpPane = new JTextPane();
	private JFrame historyFrame = new JFrame();
	private JFrame helpFrame = new JFrame();
	private CommentPanel commentPanel = new CommentPanel();
	JScrollPane scrollPane  = new JScrollPane();
	static DefaultTableModel hmodel = new DefaultTableModel();

	public SaveReport() {
		super();
		initialize();
		createErrorTable();
		clearData();
		init();
	}

	/**
	 * Function used to initialize class variables when the SaveReport
	 * constructor is called
	 */

	public void initialize() {
		basepath.setText(JMeterUtils.getJMeterBinDir());
		model = new ObjectTableModel(COLUMNS, SamplingStatCalculator.class,
				new Functor[] {
			new Functor("getLabel"), //$NON-NLS-1$
			new Functor("getCount"), //$NON-NLS-1$
			new Functor("getMeanAsNumber"), //$NON-NLS-1$
			new Functor("getMedian"), //$NON-NLS-1$
			new Functor("getPercentPoint", //$NON-NLS-1$
					new Object[] { new Float(.900) }),
					new Functor("getMin"), //$NON-NLS-1$
					new Functor("getMax"), //$NON-NLS-1$
					new Functor("getErrorPercentage"), //$NON-NLS-1$
					new Functor("getRate"), //$NON-NLS-1$
					new Functor("getKBPerSecond") //$NON-NLS-1$
		}, new Functor[] { null, null, null, null, null, null, null,
			null, null, null }, new Class[] { String.class,
			Long.class, Long.class, Long.class, Long.class,
			Long.class, Long.class, String.class, String.class,
			String.class });
		BASEPATH = basepath.getText().trim();
		BASEPATH = BASEPATH + File.separatorChar + this.getName() + "_"
				+ GetTimeStampInHMS();
		saveAsPdf.setSelected(true);
	}

	/**
	 * This function is called from the result collector associated with the
	 * abject of this class.It is called just after the end of a test.
	 */
	public static synchronized void testStopped() {
		testRunning = false;
		TestEnded = true;

	}

	/**
	 * Returns a timestamp of the form yyyy-MM-dd_HH-mm-ss
	 * 
	 * @return String containing the timestamp
	 */
	private String GetTimeStampInHMS() {

		long epoch = System.currentTimeMillis() / 1000;
		String date = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
		.format(new java.util.Date(epoch * 1000));
		return date;

	}

	// Column renderers
	private static final TableCellRenderer[] RENDERERS = new TableCellRenderer[] {
		null, // Label
		null, // count
		null, // Mean
		null, // median
		null, // 90%
		null, // Min
		null, // Max
		new NumberRenderer("#0.00%"), // Error %age //$NON-NLS-1$
		new RateRenderer("#.0"), // Throughput //$NON-NLS-1$
		new NumberRenderer("#.0"), // pageSize   //$NON-NLS-1$
	};

	/** @deprecated - only for use in testing */
	@Deprecated
	public static boolean testFunctors() {
		SaveReport instance = new SaveReport();
		return instance.model.checkFunctors(null, instance.getClass());
	}

	@Override
	public String getLabelResource() {
		return "aggregate_report"; //$NON-NLS-1$
	}

	/**
	 * Get the component's label. This label is used in drop down lists that
	 * give the user the option of choosing one type of component in a list of
	 * many. It should therefore be a descriptive name for the end user to see.
	 * It must be unique to the class. It is also used by Help to find the
	 * appropriate location in the documentation. Normally getLabelResource()
	 * should be overridden instead of this method the definition of this method
	 * in AbstractJMeterGuiComponent is intended for general use.
	 * 
	 * @Override getStaticLabel() in AbstractJMeterGuiComponent
	 * @Returns GUI label for the component.
	 */

	public String getStaticLabel() {
		return "Save Report";
	}

	/**
	 * Used for repeatedly testing the testEnded variable for true to check if a
	 * test has ended
	 */
	Runnable helloRunnable = new Runnable() {
		public synchronized void run() {
			if (TestEnded == true) {
				TestEnded = false;
				EndDateTime = GetTimeStampInHMS();

				try {
					updateHistory();
					saveHistory();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (saveAsPdf.isSelected()) {
					String fileName = "TestReport" + "_" + GetTimeStampInHMS()
							+ ".pdf";
					try {
						CreatePdfReport report = new CreatePdfReport();
						report.initialize(BASEPATH, fileName, true,
								summaryCsvFile, errorTableModel);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (DocumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String reportPath = BASEPATH + File.separatorChar
							+ fileName;
					SaveReportMailer mailer = new SaveReportMailer();
					SaveReportSms sms = new SaveReportSms();

					try {
						mailer.mailReport(reportPath, EndDateTime);
						sms.initialize(EndDateTime);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}					
				}
				/*
				try {
					saveHistory();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 */

			}
		}
	};

	/**
	 * This method is called by sampling thread to inform the visualizer about
	 * the arrival of a new sample.
	 */
	public void add(final SampleResult res) {
		if (firstIteration) {

			ScheduledExecutorService executor = Executors
					.newScheduledThreadPool(1);
			executor.scheduleAtFixedRate(helloRunnable, 0, 2, TimeUnit.SECONDS);
			firstIteration = false;
		}

		if (testRunning == false) {
			BASEPATH = basepath.getText().trim();
			errorNo = 0;
			StartDateTime = GetTimeStampInHMS();

			BASEPATH = BASEPATH + File.separatorChar + this.getName() + "_"
					+ GetTimeStampInHMS();
			try {
				File createDir = new File(BASEPATH);

				createDir.mkdirs();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			summaryCsvFile = BASEPATH + File.separatorChar + "Aggregate_Report"
					+ "_" + GetTimeStampInHMS() + ".csv";
			errorCsvFile = BASEPATH + File.separatorChar + "Error_Report" + "_"
					+ GetTimeStampInHMS() + ".csv";
			testRunning = true;
		}

		JMeterUtils.runSafe(new Runnable() {
			@Override
			public void run() {
				SamplingStatCalculator row = null;
				final String sampleLabel = res.getSampleLabel(useGroupName
						.isSelected());
				synchronized (lock) {
					row = tableRows.get(sampleLabel);
					if (row == null) {
						row = new SamplingStatCalculator(sampleLabel);
						tableRows.put(row.getLabel(), row);
						model.insertRow(row, model.getRowCount() - 1);
					}
					if (!res.isSuccessful()) {
						synchronized (lock) {
							addErrorToTable(res);
						}
					}
				}

				/*
				 * Synch is needed because multiple threads can update the
				 * counts.
				 */
				synchronized (row) {
					row.addSample(res);
				}
				SamplingStatCalculator tot = tableRows.get(TOTAL_ROW_LABEL);
				synchronized (tot) {
					tot.addSample(res);
				}
				model.fireTableDataChanged();
				saveTable();
			}
		});
	}

	/**
	 * Clears this visualizer and its model, and forces a repaint of the table.
	 */
	@Override
	public void clearData() {
		synchronized (lock) {
			model.clearData();
			tableRows.clear();
			tableRows.put(TOTAL_ROW_LABEL, new SamplingStatCalculator(
					TOTAL_ROW_LABEL));
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
		JPanel southPanel = new JPanel(new BorderLayout());
		Border margin = new EmptyBorder(10, 10, 5, 10);
		mainPanel.setBorder(margin);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(makeTitlePanel());
		mainPanel.add(createSavePanel());
		myJTable = new JTable(model);
		myJTable.getTableHeader().setDefaultRenderer(
				new HeaderAsPropertyRenderer());
		myJTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		RendererUtils.applyRenderers(myJTable, RENDERERS);
		myScrollPane = new JScrollPane(myJTable);
		southPanel.add(setEmailsButton, BorderLayout.WEST);
		setEmailsButton.setActionCommand(setEmailsString);
		setEmailsButton.addActionListener(this);
		setEmailsButton.setEnabled(true);
		southPanel.add(helpButton, BorderLayout.EAST);
		helpButton.setActionCommand(helpString);
		helpButton.addActionListener(this);
		helpButton.setEnabled(true);
		southPanel.setBorder(margin);
		this.add(mainPanel, BorderLayout.NORTH);
		this.add(myScrollPane, BorderLayout.CENTER);
		this.add(southPanel, BorderLayout.SOUTH);
	}

	/*
	 * Creates the headers for the Error Table
	 */
	public void createErrorTable() {
		errorTableModel.addColumn("Sr.No");
		errorTableModel.addColumn("Sample Label");
		errorTableModel.addColumn("Sample Count");
		errorTableModel.addColumn("Error Response Message");
	}

	/**
	 * Adds the error to the error table when an error occurs
	 * 
	 * @param res
	 */
	public synchronized void addErrorToTable(SampleResult res) {
		Vector e = new Vector(4);
		if (errorExists(res.getSampleLabel(), res.getResponseMessage()) == -1) {
			e.add(errorNo + 1);
			e.add(res.getSampleLabel());
			e.add(1);
			e.add(res.getResponseMessage());

			errorTableModel.addRow(e);
			++errorNo;
		} else {
			int x = errorExists(res.getSampleLabel(), res.getResponseMessage());

			errorTableModel.setValueAt(
					1 + (int) errorTableModel.getValueAt(x, 2), x, 2);
		}
	}

	/**
	 * Used to determine if the error in the sample has been recorded in the
	 * error table previously
	 * 
	 * @param label
	 * @param message
	 * @return
	 */
	public synchronized int errorExists(String label, String message) {

		if (errorTableModel.getRowCount() == 0)
			return -1;
		for (int i = 0; i < errorTableModel.getRowCount(); i++) {
			if (errorTableModel.getValueAt(i, 1).toString()
					.compareToIgnoreCase(label) == 0
					&& errorTableModel.getValueAt(i, 3).toString()
					.compareToIgnoreCase(message) == 0)
				return i;
		}
		return -1;
	}

	/**
	 * Used to determine if the Csv file used to record test history for the
	 * plugin has been already created
	 * 
	 * @return
	 * @throws IOException
	 */
	public synchronized boolean isHistoryFileEmpty() throws IOException {
		try {
			File historyFile = new File(JMeterUtils.getJMeterBinDir()
					+ File.separatorChar + "SaveReportPluginHistory.csv");
			FileReader fr = new FileReader(historyFile);
			fr.read();
			fr.close();

		} catch (java.io.FileNotFoundException e) {
			File historyFile = new File(JMeterUtils.getJMeterBinDir()
					+ File.separatorChar + "SaveReportPluginHistory.csv");
			historyFile.createNewFile();
			FileWriter fw = new FileWriter(JMeterUtils.getJMeterBinDir()
					+ File.separatorChar + "SaveReportPluginHistory.csv");
			PrintWriter pw = new PrintWriter(fw);
			pw.println("Test_Start_Date-Time,Test_End_Date-Time,Test_Report_Folder_Path");
			pw.flush();
			// Close the Print Writer
			pw.close();
			// Close the File Writer
			fw.close();
		}

		return true;

	}

	public synchronized void updateHistory() throws IOException{
		isHistoryFileEmpty();
		int lastRow = hmodel.getRowCount(); 

		Vector historyRow = new Vector();
		if(lastRow==0)
			historyRow.add(1);
		else
			historyRow.add(Integer.parseInt(hmodel.getValueAt(lastRow-1, 0).toString())+1);
		historyRow.add(StartDateTime);
		historyRow.add(EndDateTime);
		historyRow.add(BASEPATH);
		hmodel.addRow(historyRow);
		System.out.println(historyRow.toString());   

	}


	/**
	 * Saves the details of the Test to the History file.
	 * 
	 * @throws IOException
	 */
	public synchronized void saveHistory() throws IOException {
		isHistoryFileEmpty();
		File historyFile = new File(JMeterUtils.getJMeterBinDir()
				+ File.separatorChar + "SaveReportPluginHistory.csv");

		if (historyFile.exists() && historyFile.length() > 0) {

			int testNo = 0;
			FileReader fr;
			try {
				fr = new FileReader(JMeterUtils.getJMeterBinDir()
						+ File.separatorChar + "SaveReportPluginHistory.csv");
				CSVReader cr = new CSVReader(fr);
				String[] row1;
				cr.readNext();
				while (true) {
					synchronized (this) {
						row1 = cr.readNext();
						if (row1 == null)
							break;
						else {

							testNo = Integer.parseInt(row1[0]);
						}
					}
				}
				cr.close();
				fr.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			PrintWriter out =null;
		
			try {
				out = new PrintWriter(new BufferedWriter(
						new FileWriter(JMeterUtils.getJMeterBinDir()
								+ File.separatorChar
								+ "SaveReportPluginHistory.csv", true)));
				out.println(Integer.toString(testNo + 1) + "," + StartDateTime
						+ "," + EndDateTime + "," + BASEPATH);
			} catch (IOException e) {
			}
			finally{
				out.flush();
				out.close();

			}

		}
	}

	/**
	 * Creates an CSV aggregate summary report
	 */
	public synchronized void saveTable() {
		myJTable.getRowCount();
		myJTable.getColumnCount();

		FileWriter writer = null;
		try {
			writer = new FileWriter(summaryCsvFile);
			CSVSaveService
			.saveCSVStats(model, writer, saveHeaders.isSelected());
		} catch (FileNotFoundException e) {
			log.warn(e.getMessage());
		} catch (IOException e) {
			log.warn(e.getMessage());
		} finally {
			JOrphanUtils.closeQuietly(writer);
		}

		writer = null;
		try {
			writer = new FileWriter(errorCsvFile);
			CSVSaveService.saveCSVStats(errorTableModel, writer,
					saveHeaders.isSelected());
		} catch (FileNotFoundException e) {
			log.warn(e.getMessage());
		} catch (IOException e) {
			log.warn(e.getMessage());
		} finally {
			JOrphanUtils.closeQuietly(writer);
		}

	}

	/**
	 * Creates a JPanel that contains the browse panel the button Panel and the
	 * checkBox Panel
	 */
	public JPanel createSavePanel() {
		JPanel masterPanel = new JPanel(new BorderLayout());
		JPanel savePanel = new JPanel(new VerticalLayout());
		JPanel browsePanel = new JPanel(new BorderLayout());
		JPanel checkboxPanel = new JPanel(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		JPanel buttonBorderPanel = new JPanel(new BorderLayout());
		JPanel titlePanel = new JPanel(new VerticalLayout());
		JPanel tempPanel = new JPanel(new BorderLayout());
		browsePanel.setPreferredSize(new Dimension(976, 37));
		JLabel selectDir = new JLabel("Select directory to store Reports");
		selectDir.setAlignmentX(CENTER_ALIGNMENT);
		JLabel directoryLabel = new JLabel(" Report directory: ");
		JLabel emptyLabel = new JLabel("          ");

		Border lineBorder = BorderFactory.createEtchedBorder();
		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);

		browse.setActionCommand(BROWSE);
		browse.addActionListener(this);

		extractToPdf.setActionCommand(EXTRACTTOPDF);
		extractToPdf.addActionListener(this);

		viewTestHistory.setActionCommand(VIEWTESTHISTORY);
		viewTestHistory.addActionListener(this);

		compareTestReports.setActionCommand(COMPARETESTREPORTS);
		compareTestReports.addActionListener(this);

		browsePanel.add(directoryLabel, BorderLayout.WEST);
		browsePanel.add(basepath, BorderLayout.CENTER);
		browsePanel.add(browse, BorderLayout.EAST);
		browsePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

		GridLayout grid = new GridLayout(1, 3);
		grid.setHgap(50);
		buttonPanel.setLayout(grid);
		buttonPanel.add(viewTestHistory);
		buttonPanel.add(compareTestReports);
		buttonPanel.add(extractToPdf);
		buttonPanel.setBorder(emptyBorder);
		buttonBorderPanel.add(buttonPanel, BorderLayout.CENTER);
		buttonBorderPanel.setBorder(lineBorder);
		checkboxPanel.add(saveAsPdf, BorderLayout.CENTER);
		titlePanel.add(selectDir);
		titlePanel.setBackground(Color.lightGray);
		titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		tempPanel.add(titlePanel, BorderLayout.NORTH);
		tempPanel.add(savePanel, BorderLayout.CENTER);
		savePanel.add(browsePanel);
		savePanel.add(checkboxPanel);
		savePanel.setBorder(lineBorder);
		masterPanel.add(tempPanel, BorderLayout.NORTH);
		masterPanel.add(emptyLabel, BorderLayout.CENTER);
		masterPanel.add(buttonBorderPanel, BorderLayout.SOUTH);
		masterPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		return masterPanel;
	}

	/**
	 * Creates and displays a JFrame that show the Help document
	 * 
	 * @throws IOException
	 */
	private void displayHelp() throws IOException {
		helpFrame.setTitle("Help");
		helpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		helpFrame.setSize(1000, 600);
		helpFrame.setLocationRelativeTo(null);

		scrollPane.getViewport().add(helpPane);
		helpFrame.getContentPane().add(scrollPane);
		helpPane.setContentType("text/html");
		helpPane.setText("<!DOCTYPE html><html><body><p><h1><a href=\"#P1\">1.   Using the Plugin in a test</a></h1><h1><a href=\"#P2\">2.   Using the extract CSV to PDF  utility</a></h1><h1><a href=\"#P3\">3.   Using the compare CSV result utility</a></h1><h1><a href=\"#P4\">4.   Viewing Save Report test history </a></h1></body></html>");
		helpPane.setEditable(false);
		helpPane.addHyperlinkListener(this);
		helpPane.setEnabled(true);
		helpPane.setFont(new Font("Garamond", Font.PLAIN, 20));
		helpPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		helpFrame.setVisible(true);

	}

	/**
	 * Used for storing the Mobile number entered in ther GUI to a text file.
	 */
	private void storeMobileNo() {
		File mobileNoFile = new File(JMeterUtils.getJMeterBinDir()
				+ File.separatorChar + "SaveReportMobileNumber.txt");
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(
					new FileWriter(mobileNoFile, false)));
			writer.print(mobileNumber);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Creates the panel that contains the name panel and the Comments panel
	 */
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
		useGroupName
		.setSelected(el.getPropertyAsBoolean(USE_GROUP_NAME, false));
		saveHeaders.setSelected(el.getPropertyAsBoolean(SAVE_HEADERS, true));
	}

	/**
	 * Displays a notification on screen that disappears after 4 seconds
	 * 
	 * @param message
	 */
	public void displayMessage(String message) {
		JLabel messageLabel = new JLabel(message);
		JPanel messagePanel = new JPanel(new BorderLayout());
		messageFrame.setSize(400, 200);
		messageFrame.setLocationRelativeTo(null);
		messageFrame.setLayout(new BorderLayout());
		messagePanel.add(messageLabel, BorderLayout.CENTER);
		messageFrame.add(messagePanel, BorderLayout.CENTER);
		messageFrame.setVisible(true);
		javax.swing.Timer timer = new javax.swing.Timer(4000,
				new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Clear text or whatever you want
				messageFrame.dispose();
			}
		});
		// start Tick-Tack
		timer.start();

	}

	/**
	 * Creates and displays the JFrame that provides the interface for creating
	 * a comparison report.
	 */
	public void displayComparisonDialog() {
		Border margin = new EmptyBorder(10, 5, 10, 5);
		comparisonReportFrame
		.setTitle("Create PDF Comparison Report from 2 saved CSV results");
		comparisonReportFrame.setSize(700, 340);
		comparisonReportFrame.setLocationRelativeTo(null);
		comparisonReportFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		JPanel comparisonPanel1 = new JPanel();
		JPanel comparisonPanel2 = new JPanel();
		JPanel comparisonPanel3 = new JPanel();
		JPanel comparisonPanel4 = new JPanel(new BorderLayout());
		JPanel comparisonPanel5 = new JPanel();
		JPanel comparisonPanel6 = new JPanel(new VerticalLayout());

		csvFileSource1 = new JTextField("");
		csvFileSource2 = new JTextField("");
		comparisonReportDir = new JTextField("");
		comparisonReportDir.setText(JMeterUtils.getJMeterBinDir().trim());

		JLabel selectFile1 = new JLabel("Select the first CSV file");
		JLabel selectFile2 = new JLabel("Select the second CSV file");
		JLabel selectOutputDir = new JLabel(
				"Select the destination folder for the PDF Comparison report    ");
		comparisonPanel1.setLayout(new BorderLayout());
		comparisonPanel2.setLayout(new BorderLayout());
		comparisonPanel3.setLayout(new BorderLayout());

		GridLayout g = new GridLayout(10, 1);
		comparisonPanel5.setLayout(g);
		comparisonPanel5.setBorder(margin);

		JButton selectCsv1 = new JButton("Browse");
		selectCsv1.addActionListener(this);
		selectCsv1.setActionCommand("Browse1");

		JButton selectCsv2 = new JButton("Browse");
		selectCsv2.addActionListener(this);
		selectCsv2.setActionCommand("Browse2");

		JButton setComparisonDir = new JButton("Browse");
		setComparisonDir.addActionListener(this);
		setComparisonDir.setActionCommand("Browse3");

		JButton createReportButton = new JButton("Create Comparison Report");
		createReportButton.addActionListener(this);
		createReportButton.setActionCommand("createComparisonReport");

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand("cancel2");

		selectCsv1.setSize(78, 34);
		selectCsv2.setSize(78, 34);
		setComparisonDir.setSize(300, 34);

		selectFile1.setBorder(new EmptyBorder(5, 5, 5, 5));
		selectFile2.setBorder(new EmptyBorder(5, 5, 5, 5));
		comparisonPanel1.add(selectFile1, BorderLayout.NORTH);
		comparisonPanel1.add(csvFileSource1, BorderLayout.CENTER);
		comparisonPanel1.add(selectCsv1, BorderLayout.EAST);
		comparisonPanel1.setBackground(Color.lightGray);
		comparisonPanel1.setBorder(BorderFactory.createEtchedBorder());

		comparisonPanel2.add(selectFile2, BorderLayout.NORTH);
		comparisonPanel2.add(csvFileSource2, BorderLayout.CENTER);
		comparisonPanel2.add(selectCsv2, BorderLayout.EAST);
		comparisonPanel2.setBackground(Color.lightGray);
		comparisonPanel2.setBorder(BorderFactory.createEtchedBorder());

		comparisonPanel3.setLayout(new BorderLayout());
		comparisonPanel3.add(comparisonPanel1, BorderLayout.NORTH);
		comparisonPanel3.add(comparisonPanel2, BorderLayout.SOUTH);
		comparisonPanel3
		.setBorder(BorderFactory
				.createTitledBorder("Select the 2 CSV result files to be used for the Comparison Report"));

		comparisonPanel3.setPreferredSize(new Dimension(655, 145));
		comparisonPanel4.setPreferredSize(new Dimension(650, 58));
		comparisonPanel5.setPreferredSize(new Dimension(655, 50));

		selectOutputDir.setBorder(new EmptyBorder(5, 5, 5, 5));
		comparisonPanel4.add(selectOutputDir, BorderLayout.NORTH);
		comparisonPanel4.add(comparisonReportDir, BorderLayout.CENTER);
		comparisonPanel4.add(setComparisonDir, BorderLayout.EAST);
		comparisonPanel4.setBorder(BorderFactory.createEtchedBorder());
		comparisonPanel4.setBackground(Color.LIGHT_GRAY);

		selectOutputDir.setBorder(new EmptyBorder(5, 5, 5, 5));
		selectOutputDir.setBackground(Color.lightGray);
		comparisonPanel5.add(createReportButton);
		comparisonPanel5.add(cancelButton);
		GridLayout bottomGrid = new GridLayout(1, 2);
		bottomGrid.setHgap(200);
		comparisonPanel5.setLayout(bottomGrid);

		comparisonPanel6.add(comparisonPanel3);
		comparisonPanel6.add(comparisonPanel4);
		comparisonPanel6.add(comparisonPanel5);

		comparisonPanel6.setBorder(new EmptyBorder(15, 5, 7, 5));

		comparisonReportFrame.add(comparisonPanel6);
		comparisonReportFrame.setLocationRelativeTo(null);
		comparisonReportFrame.setVisible(true);
	}

	void createHistory() throws IOException{


		hmodel.addColumn("Sr.No.");
		hmodel.addColumn("Test Start Date-Time");// report
		hmodel.addColumn("Test End Date-Time");
		hmodel.addColumn("Test Report Output Directory");
		isHistoryFileEmpty();
		FileReader fr;
		try {
			fr = new FileReader(JMeterUtils.getJMeterBinDir()
					+ File.separatorChar + "SaveReportPluginHistory.csv");
			CSVReader cr = new CSVReader(fr);
			String[] row1;
			cr.readNext();
			while (true) {
				synchronized (this) {
					row1 = cr.readNext();
					if (row1 == null)
						break;
					else {
						hmodel.addRow(row1);
					}
				}
			}
			cr.close();
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	/**
	 * Creates and displays the JFrame that shows the test history.
	 * 
	 * @throws IOException
	 */
	public void displayHistory() throws IOException {
		if(hmodel.getColumnCount()==0)
			createHistory();
        historyFrame = new JFrame();
		historyFrame.setTitle("Test History ");
		historyFrame.setSize(800, 500);
		historyFrame.setLocationRelativeTo(null);
		JPanel hpanel = new JPanel(new BorderLayout());

		historyFrame.add(hpanel);
		// Set up the table itself
		JTable htable = new JTable();
		// create model fro csv file

		htable = new JTable(hmodel);
		JTableHeader header = htable.getTableHeader();
		header.setBackground(Color.LIGHT_GRAY);

		htable.setBounds(400, 250, 780, 480);
		JScrollPane tableScrollPanel = new JScrollPane(htable);
		tableScrollPanel.setViewportBorder(BorderFactory.createEmptyBorder(2,
				2, 2, 2));
		hpanel.add(tableScrollPanel, BorderLayout.CENTER);
		historyFrame.setVisible(true);
	}

	/**
	 * Creates and displays the JFrame that provides the interface for
	 * extracting a PDF report from a CSV summary file.
	 */
	public void displayExtractionDialog() {
		pdfExtractionFrame.setTitle("Create PDF Report from saved CSV result");
		pdfExtractionFrame.setSize(700, 250);
		pdfExtractionFrame.setLocationRelativeTo(null);
		pdfExtractionFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel(new BorderLayout());
		JPanel panel4 = new JPanel();
		JPanel blank = new JPanel();
		panel4.setLayout(new VerticalLayout());
		JLabel selectCsvFile = new JLabel(
				"Select the CSV result file to be used for the report");
		selectCsvFile.setBackground(Color.gray);
		JLabel selectDir = new JLabel(
				"Select the destination folder for the PDF report    ");
		selectDir.setBackground(Color.lightGray);
		JLabel blankLabel = new JLabel("   ");

		csvFileSource = new JTextField(""); // source csv file
		csvFileSource.setText(null);
		extractedReportDir = new JTextField(""); // extractedReportDir
		extractedReportDir.setText(JMeterUtils.getJMeterBinDir().trim());

		panel1.setLayout(new BorderLayout());
		panel2.setLayout(new BorderLayout());

		JButton selectCsv = new JButton("Browse");
		selectCsv.addActionListener(this);
		selectCsv.setActionCommand("browse1");

		JButton selectOutputDir = new JButton("Browse");
		selectOutputDir.addActionListener(this);
		selectOutputDir.setActionCommand("browse2");

		JButton extractReport = new JButton("Create Report");
		extractReport.addActionListener(this);
		extractReport.setActionCommand("createReport");

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		cancel.setActionCommand("cancel");

		selectCsv.setSize(78, 34);
		selectOutputDir.setSize(78, 34);
		extractReport.setSize(90, 34);

		csvFileSource.setText("");
		selectCsvFile.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		selectDir.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel1.add(selectCsvFile, BorderLayout.NORTH);
		panel1.add(csvFileSource, BorderLayout.CENTER);
		panel1.add(selectCsv, BorderLayout.EAST);
		panel1.setBorder(BorderFactory.createEtchedBorder());
		panel1.setBackground(Color.LIGHT_GRAY);
		panel1.setPreferredSize(new Dimension(665, 58));
		panel2.add(selectDir, BorderLayout.NORTH);
		panel2.add(extractedReportDir, BorderLayout.CENTER);
		panel2.add(selectOutputDir, BorderLayout.EAST);
		panel2.setBackground(Color.LIGHT_GRAY);
		panel2.setBorder(BorderFactory.createEtchedBorder());
		panel2.setPreferredSize(new Dimension(665, 58));

		panel3.add(extractReport, BorderLayout.WEST);
		panel3.add(cancel, BorderLayout.EAST);
		panel3.setPreferredSize(new Dimension(665, 29));
		// panel4.add(selectCsvFile);
		panel4.add(panel1);
		blank.add(blankLabel);
		blank.setPreferredSize(new Dimension(665, 15));
		panel4.add(blank);

		// panel4.add(selectDir);
		panel4.add(panel2);
		panel4.add(blank);

		panel4.add(panel3);
		panel4.setBorder(BorderFactory.createEmptyBorder(20, 5, 0, 5));
		pdfExtractionFrame.add(panel4);
		pdfExtractionFrame.setLocationRelativeTo(null);
		pdfExtractionFrame.setVisible(true);
	}

	/**
	 * Checks if the entered email address follows the correct syntax for an
	 * Email-id
	 * 
	 * @return
	 */
	public Boolean isEmailValid() {
		return (this.emailField
				.getText()
				.trim()
				.matches(
						"[a-zA-Z0-9\\.]+@[a-zA-Z0-9\\-\\_\\.]+\\.[a-zA-Z0-9]{3}+\\.[a-zA-Z]{2}")
						|| this.emailField
						.getText()
						.trim()
						.matches(
								"[a-zA-Z0-9\\.]+@[a-zA-Z0-9\\-\\_\\.]+\\.[a-zA-Z0-9]{3}") || this.emailField
								.getText().trim()
								.matches("[a-zA-Z0-9\\.]+@[a-zA-Z0-9\\-\\_\\.]+\\.[a-zA-Z]{2}"));
	}

	/**
	 * Creates a List model from the saved CSV file containing the list of
	 * email-ids.
	 * 
	 * @throws IOException
	 */
	private void createEmailModel() throws IOException {
		File emailFile = new File(JMeterUtils.getJMeterBinDir()
				+ File.separatorChar + "SaveReportMailingList.csv");
		listModel.removeAllElements();
		if (emailFile.exists() && emailFile.length() > 0) {
			FileReader fr;
			try {
				fr = new FileReader(JMeterUtils.getJMeterBinDir()
						+ File.separatorChar + "SaveReportMailingList.csv");
				CSVReader cr = new CSVReader(fr);
				String row1[];
				while (true) {
					synchronized (this) {
						row1 = cr.readNext();
						if (row1 == null)
							break;
						else {
							listModel.addElement(row1[0]);
							;
						}
					}
				}
				cr.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Stores the contents of the list of email-Ids entered in the GUI to a CSV
	 * file
	 */
	private void storeEmailModel() {
		try {
			PrintWriter emailWriter = new PrintWriter(new BufferedWriter(
					new FileWriter(JMeterUtils.getJMeterBinDir()
							+ File.separatorChar + "SaveReportMailingList.csv",
							false)));
			for (int i = 0; i < listModel.size(); i++) {
				emailWriter.println(listModel.get(i));
			}
			emailWriter.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Creates and displays a frame that allows user to set the Mobile number
	 * and Email address for sending the notification at the end of the test.
	 * 
	 * @throws IOException
	 */
	public void displaySetEmailsFrame() throws IOException {

		emailFrame.setTitle("Set Mobile number and Email-Ids");
		emailFrame.setSize(600, 400);
		emailFrame.setLocationRelativeTo(null);
		emailFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		Border lineBorder = BorderFactory.createLineBorder(Color.DARK_GRAY);
		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		JLabel emailLabel = new JLabel(
				"  Set the E-mail IDs that need to be notified and e-mailed the Test Report when test has completed");
		emailLabel.setPreferredSize(new Dimension(570, 20));
		emailLabel.setBorder(emptyBorder);
		JLabel mobileLabel = new JLabel(
				"                           Set the mobile number that need to be notified when test has completed");
		mobileLabel.setPreferredSize(new Dimension(570, 20));
		mobileLabel.setBorder(emptyBorder);
		JLabel mobileNoLabel = new JLabel("Mobile Number    +91");
		mobileNoLabel.setBorder(emptyBorder);
		JLabel emptyLabel = new JLabel("          ");
		addButton = new JButton("Add E-mail to list");
		removeButton = new JButton("Remove Email-Id from List");
		doneButton = new JButton("OK");
		listModel = new DefaultListModel<String>();
		mobileNoField = new JTextField(10);
		getMobileNo();
		mobileNoField.setColumns(10);
		emailFrame.setLayout(new BorderLayout());
		// Create the list and put it in a scroll pane.
		createEmailModel();
		list = new JList<String>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setVisibleRowCount(5);
		list.setBorder(lineBorder);
		JScrollPane listScrollPane = new JScrollPane(list);
		listScrollPane.setBorder(emptyBorder);

		addButton.setActionCommand(addIDString);
		addButton.addActionListener(this);
		addButton.setEnabled(true);

		removeButton.setActionCommand(removeIDString);
		removeButton.addActionListener(this);
		removeButton.setEnabled(true);

		doneButton.setActionCommand(doneString);
		doneButton.addActionListener(this);
		doneButton.setEnabled(true);

		emailField = new JTextField(10);
		emailField.setText(null);

		JPanel buttonPane = new JPanel();
		JPanel donePanel = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new VerticalLayout());
		JPanel mobilePanel = new JPanel(new BorderLayout());
		JPanel mobileNoPanel = new JPanel();
		JPanel emailPanel = new JPanel(new BorderLayout());
		mobilePanel.add(mobileLabel, BorderLayout.NORTH);
		mobileNoPanel.add(mobileNoLabel);
		mobileNoPanel.add(mobileNoField);
		mobileNoPanel.add(mobileNoField);
		mobilePanel.add(mobileNoPanel, BorderLayout.SOUTH);
		mobilePanel.setBorder(lineBorder);
		mobilePanel.setBackground(Color.lightGray);
		mobilePanel.setSize(570, 20);
		emailPanel.setBackground(Color.lightGray);
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.add(removeButton);
		buttonPane.add(Box.createHorizontalStrut(5));
		buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
		buttonPane.add(Box.createHorizontalStrut(5));
		buttonPane.add(emailField);
		buttonPane.add(addButton);
		emailPanel.add(emailLabel, BorderLayout.NORTH);
		emailPanel.add(buttonPane, BorderLayout.CENTER);
		emailPanel.setBorder(lineBorder);
		buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		northPanel.add(emptyLabel);
		northPanel.add(emptyLabel);
		northPanel.add(mobilePanel);
		northPanel.add(emailPanel);
		northPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		donePanel.add(doneButton, BorderLayout.EAST);
		donePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		emailFrame.add(northPanel, BorderLayout.NORTH);
		emailFrame.add(listScrollPane, BorderLayout.CENTER);
		emailFrame.add(donePanel, BorderLayout.SOUTH);
		emailFrame.setVisible(true);
	}

	/**
	 * Reads the mobile number set in the privios test from the text file
	 * SaveReportMobileNumber.txt
	 */
	private void getMobileNo() {
		// TODO Auto-generated method stub
		char[] number = new char[10];
		File mobileNoFile = new File(JMeterUtils.getJMeterBinDir()
				+ File.separatorChar + "SaveReportMobileNumber.txt");
		try {
			FileReader reader = new FileReader(mobileNoFile);
			reader.read(number);
			if (number.toString() != "")
				mobileNumber = String.valueOf(number);
			mobileNoField.setText(mobileNumber);
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Checks if the Mobile Number entered is a valid mobile phone number.
	 * 
	 * @return
	 */
	private Boolean isMobileNumValid() {
		if (mobileNoField.getText().trim().length() == 10
				|| mobileNoField.getText().trim().length() == 0) {
			for (char c : mobileNoField.getText().trim().toCharArray()) {
				if (!Character.isDigit(c)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Invoked when an action occurs
	 */
	@Override
	public void actionPerformed(ActionEvent ev) {
		String action = ev.getActionCommand();

		if (action.equals(BROWSE)) {

			JFileChooser j = new JFileChooser();
			j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = j.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				basepath.setText(j.getSelectedFile().getAbsolutePath());
				BASEPATH = basepath.getText().trim();
			}
		} else if (action.equals(EXTRACTTOPDF)) { // Extract pdf from csv button
			// pressed
			if(!pdfExtractionFrame.isShowing())
				displayExtractionDialog();

		} else if (action.equals("browse1")) {

			JFileChooser chooser = FileDialoger.promptToOpenFile();
			if (chooser == null) {
				return;
			}
			if (!chooser.getSelectedFile().exists()) {
				JOptionPane.showMessageDialog(null,
						"The selected  file does not exist",
						"Selected file does not exist", JOptionPane.OK_OPTION);
				csvFileSource.setText("");
			} else {
				csvFileSource.setText(chooser.getSelectedFile()
						.getAbsolutePath());
				csvFile = chooser.getSelectedFile();
			}

		} else if (action.equals("browse2")) {

			JFileChooser j = new JFileChooser();
			j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = j.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				extractedReportDir.setText(j.getSelectedFile()
						.getAbsolutePath());
				if (!j.getSelectedFile().exists()) {
					JOptionPane.showMessageDialog(null,
							"The selected directory does not exist",
							"Selected directory does not exist",
							JOptionPane.OK_OPTION);
					extractedReportDir.setText("");
				} else {
					BASEPATH = extractedReportDir.getText().trim();
					BASEPATH = BASEPATH + File.separatorChar + this.getName()
							+ "_" + GetTimeStampInHMS();
					try {
						File createDir = new File(BASEPATH);
						createDir.mkdirs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		} else if (action.equals("createReport")) {
			if (csvFileSource.getText().trim().length() == 0
					|| extractedReportDir.getText().trim().length() == 0
					|| !new File(csvFileSource.getText().trim()).exists()
					|| !new File(extractedReportDir.getText().trim())
			.isDirectory())
				JOptionPane
				.showMessageDialog(
						null,
						"Please select a valid source CSV file and the Destination folder for the output",
						"Select Source csv file and Output folder",
						JOptionPane.OK_OPTION);
			else {
				try {
					CreatePdfReport extractedReport = new CreatePdfReport(); // creates
					// a PDF report from CSV file

					String fileName = "ExtractedTestReport" + "_" 
							// by creating an object of CreatePdfReport
							+ GetTimeStampInHMS() + ".pdf";
					extractedReport.initialize(BASEPATH, fileName, false,
							csvFile.getAbsolutePath(), null);
					displayMessage("A PDF report was created from the CSV file");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					displayMessage("Error occoured while creating report");
				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					displayMessage("Error occoured while creating report");
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane
					.showMessageDialog(
							null,
							"Error occoured while creating report\nPlease select a valid JMeter Summary Report CSV file",
							"Select Source csv file and Output folder",
							JOptionPane.OK_OPTION);

				}
				pdfExtractionFrame.setVisible(false);
				csvFileSource.setText("");
				extractedReportDir.setText("");
				csvFileSource = null;
				extractedReportDir = null;
				pdfExtractionFrame = new JFrame();
			}
		} else if (action.equals("cancel")) {

			pdfExtractionFrame.setVisible(false);
            csvFileSource.setText("");
			extractedReportDir.setText("");
			csvFileSource = null;
			extractedReportDir = null;
			pdfExtractionFrame = new JFrame();

		} else if (action.equals(VIEWTESTHISTORY)) {

			try {
				if(!historyFrame.isShowing())
					displayHistory();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (action.equals(COMPARETESTREPORTS)) {
			if(!comparisonReportFrame.isShowing())
				displayComparisonDialog();
		}

		else if (action.equals("Browse1")) {
			JFileChooser chooser = FileDialoger.promptToOpenFile();
			if (chooser == null) {
				return;
			}
			if (!chooser.getSelectedFile().exists()) {
				JOptionPane.showMessageDialog(null,
						"The selected  file does not exist",
						"Selected file does not exist", JOptionPane.OK_OPTION);
				csvFileSource1.setText("");
			} else {
				csvFileSource1.setText(chooser.getSelectedFile()
						.getAbsolutePath());
				CsvFile1 = chooser.getSelectedFile();
			}
		} else if (action.equals("Browse2")) {
			JFileChooser chooser = FileDialoger.promptToOpenFile();
			if (chooser == null) {
				return;
			}
			if (!chooser.getSelectedFile().exists()) {
				JOptionPane.showMessageDialog(null,
						"The selected  file does not exist",
						"Selected file does not exist", JOptionPane.OK_OPTION);
				csvFileSource2.setText("");
			} else {
				csvFileSource2.setText(chooser.getSelectedFile()
						.getAbsolutePath());
				CsvFile2 = chooser.getSelectedFile();
			}
		} else if (action.equals("Browse3")) {
			JFileChooser j = new JFileChooser();
			j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = j.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				comparisonReportDir.setText(j.getSelectedFile()
						.getAbsolutePath());
			}
			if (!j.getSelectedFile().exists()) {
				JOptionPane.showMessageDialog(null,
						"The selected directory does not exist",
						"Selected directory does not exist",
						JOptionPane.OK_OPTION);
				comparisonReportDir.setText("");
			} else {
				comparisonOutputDir = comparisonReportDir.getText().trim();
				comparisonOutputDir = comparisonOutputDir + File.separatorChar
						+ this.getName() + "_" + GetTimeStampInHMS();
			}

		} else if (action.equals("createComparisonReport")) {
			if (csvFileSource1.getText().trim().length() == 0
					|| csvFileSource2.getText().trim().length() == 0
					|| comparisonReportDir.getText().trim().length() == 0
					|| !new File(csvFileSource1.getText().trim()).exists()
					|| !new File(csvFileSource2.getText().trim()).exists()
					|| !new File(comparisonReportDir.getText().trim())
			.isDirectory())
				JOptionPane
				.showMessageDialog(
						null,
						"Please select valid source CSV files and the Destination folder for the output",
						"Select source csv files and Output folder",
						JOptionPane.OK_OPTION);
			else

			{
				try {
					File createDir = new File(comparisonOutputDir);

					createDir.mkdirs();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					CreateComparisonPdfReport report = new CreateComparisonPdfReport();
					// Creates a comparison report by creating an object of create comparison report.
					report.initialize(comparisonOutputDir, CsvFile1, CsvFile2);
					displayMessage("A comparison PDF report was created from the 2 CSV files");

				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				comparisonReportFrame.setVisible(false);
				csvFileSource1.setText("");
				csvFileSource2.setText("");
				comparisonReportDir.setText("");
				csvFileSource1 = null;
				csvFileSource2 = null;
				comparisonReportDir = null;
				comparisonReportFrame = new JFrame();
			}
		} else if (action.equals("cancel2")) {
			comparisonReportFrame.setVisible(false);
			csvFileSource1.setText("");
			csvFileSource2.setText("");
			comparisonReportDir.setText("");
			csvFileSource1 = null;
			csvFileSource2 = null;
			comparisonReportDir = null;
			comparisonReportFrame = new JFrame();
		} else if (action.equals(setEmailsString)) {
			if(!emailFrame.isShowing())
				try {
					displaySetEmailsFrame();
				} catch (IOException e) {
					e.printStackTrace();
				}
		} else if (action.equals(addIDString)) {
			if (isEmailValid())
				listModel.addElement(emailField.getText().trim());
			else
				JOptionPane.showMessageDialog(null,
						"Please enter a valid E-Mail ID",
						"E-Mail ID not valid !", JOptionPane.OK_OPTION);
			emailField.setText("");

		} else if (action.equals(removeIDString)) {
			listModel.removeElement(list.getSelectedValue());
		} else if (action.equals(doneString)) {

			if (!isMobileNumValid())
				JOptionPane.showMessageDialog(null,
						"Please enter a valid 10 digit Mobile number",
						"Mobile Number not valid !", JOptionPane.OK_OPTION);
			else {
				storeEmailModel();
				mobileNumber = mobileNoField.getText().trim();
				storeMobileNo();
				emailFrame.setVisible(false);
				emailFrame = new JFrame();
			}
		} else if (action.equals(helpString)) {
			try {
				displayHelp();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (ev.getSource() == saveTable) {
			JFileChooser chooser = FileDialoger
					.promptToSaveFile("aggregate.csv");//$NON-NLS-1$
			if (chooser == null) {
				return;
			}
			FileWriter writer = null;
			try {
				writer = new FileWriter(chooser.getSelectedFile());
				CSVSaveService.saveCSVStats(model, writer,
						saveHeaders.isSelected());
			} catch (FileNotFoundException e) {
				log.warn(e.getMessage());
			} catch (IOException e) {
				log.warn(e.getMessage());
			} finally {
				JOrphanUtils.closeQuietly(writer);
			}
		}
	}


	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {


		// TODO Auto-generated method stub
		if(e.getEventType()==EventType.ACTIVATED)
		{	System.out.println("link wrkin" );
		char parts = e.getDescription().charAt(2);
		System.out.println(" link wrkin"+parts );
		if(parts=='0')		
			helpPane.setText("<p><h1><a href=\"#P1\">1.   Using the Plugin in a test</a></h1><h1><a href=\"#P2\">2.   Using the extract CSV to PDF  utility</a></h1><h1><a href=\"#P3\">3.   Using the compare CSV result utility</a></h1><h1><a href=\"#P4\">4.   Viewing Save Report test history </a></h1>");

		if(parts=='1')
			helpPane.setText("<a href=\"#P0\">Back</a><h2>1.   Using the Plugin in a test</h2><ul> <ul><p> 1.1		Set the  Test report output folder by using the Browse button </p></ul> <ul><p> 1.2		If you want the PDF report to be generated the 'Save as PDF' checkbox 		should be in its checked state.</p></ul>  <ul><p><a href=\"#P5\">1.3		SMS and E-Mail Notification settings</a></p></ul><ul><p>When you run the test, the  test report will be saved in the folder specified		and will bear the name specified for the listener in the name panel and the timestamp when the test was started.</p></ul></ul>");

		if(parts=='2')
			helpPane.setText("<a href=\"#P0\">Back</a><h2>2.   Using the extract CSV to PDF  utility</h2><ul> <ul><p> 2.1		Click on the extract csv to pdf button </p></ul> <ul><p>   2.2		Specify the source csv file using the browse button</p></ul>  <ul><p>2.3		Specify  the report output folder using the browse button</p></ul><ul><p>2.4		Click on the 'create report' button</p></ul></ul>");

		if(parts=='3')
			helpPane.setText("<a href=\"#P0\">Back</a><h2>3.   Using the compare CSV result utility</h2><ul> <ul><p>3.1		Click on the compare csv results button </p></ul> <ul><p>   3.2		Specify the two source csv files  </ul>  <ul><p>  3.3		Specify report output folder using the browse button</p></ul><ul><p> 3.4		Click create comparison report button</p></ul></ul>");

		if(parts=='4')
			helpPane.setText("<a href=\"#P0\">Back</a><h2>4.   Viewing Save Report test history </h2><ul> <ul><p>4.1  Click on the view history button </p></ul></ul>");

		if(parts=='5')
			helpPane.setText("<a href=\"#P1\">Back</a><h2>1.3		SMS and E-Mail Notification settings.</h2>		<ul> <ul><p>1.3.1	    Click on the 'set mobile No and Emails IDs' button</p></ul>		<ul><p> 1.3.2	    Set the Mobile Phone Number that should be messaged a notification              	           		 when the test completes.</p></ul>		 <ul><p>1.3.3	    Set the list of email ids that should be emailed the test report on   test completion .</p></ul>		<ul> <p>1.3.4	    The next time the plugin is used from the same installation , these              	           		 same details will be used for notification until changed.</p></ul> <ul><p>1.3.5	    The email server settings can be changed in the SaveReportEmail.properties file in JMeter bin .</p></ul></ul>");


		}
	}

}
