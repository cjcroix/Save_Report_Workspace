package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.JFileChooser;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.apache.jorphan.collections.Data;

public class NewListener1 extends AbstractListenerGui implements Serializable,ActionListener,UnsharedComponent{

private static final long serialVersionUID = 240L;

protected  JTextField basepath;
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
protected static String[]propertyHeaderArray=new String[20];

JMeterProperty BasePath=null;
static JMeterProperty JPropertyName=null;

private static final String BRO = "bro";

TestElement intermediateErrorListenerTestElement;

JTextField JPropertyNameField;

private JLabel errorProperties = new JLabel("Properties Of Failed Error Samples");

 private JTable table;
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

private static final String SELECTALL = "selectAll"; //p[0]
private static final String SAMPLENO = "sampleNo";  //p[1]
private static final String STARTTIME = "startTime"; //p[2] 
private static final String THREADNAME = "threadName"; //p[3]
private static final String LABEL = "label"; //p[4]
private static final String SAMPLETIME = "sampleTime"; //p[5]
private static final String STATUS = "status"; //p[6]
private static final String BYTES = "bytes"; //p[7]
private static final String LATENCY = "latency";  //p[8]

/*
private static Boolean p[0] = true;
private static Boolean p[1] = true;
private static Boolean p[2] = true;
private static Boolean p[3] = true;
private static Boolean p[4] = true;
private static Boolean p[5] = true;
private static Boolean p[6] = true;
private static Boolean p[7] = true;
private static Boolean p[8] = true;
*/

protected static  Boolean[] p = new Boolean[9];

//protected int PropertyCount;

/** Holds the count of properties defined by the user*/
 protected static int PropertyCount=0;

//  private String COLUMN_NAMES_0 = "Properties";

public NewListener1() {
super();

init();
}

public NewListener1(boolean displayName) {
init();
}

public String getLabelResource() {
return this.getClass().getSimpleName(); //$NON-NLS-1$
}

@Override
public String getStaticLabel() {
return "New Listener1";
}

@Override
public void configure(TestElement el) {
if(ErrorListener.updateAllowed==true)
{
 initTableModel();

//super.configure(el);

//BasePath=el.getProperty("FileBasePath");
//basepath.setText(BasePath.toString());

// LogOnlyProperties=el.getProperty("OnlyProperties");
// logOnlyProperties.setSelected(LogOnlyProperties.getBooleanValue());

// LogErrorsAndProperties=el.getProperty("ErrorsAndProperties");
// logErrorsAndProperties.setSelected(LogErrorsAndProperties.getBooleanValue());

if (el instanceof ErrorListener) {

}
}
}
//checkDeleteStatus();



public TestElement createTestElement() {
ErrorListener errorMon = new ErrorListener();
modifyTestElement(errorMon);
//errorMon.getThreadName()
JOptionPane.showMessageDialog(null, "alert",TestPlan.NAME, JOptionPane.OK_OPTION);



return errorMon;
}

public void modifyTestElement(TestElement errorListener) {
if(ErrorListener.updateAllowed==true)
{
if (table.isEditing()) {
table.getCellEditor().stopCellEditing();
}

intermediateErrorListenerTestElement=(TestElement)errorListener.clone();





//super.configureTestElement(errorListener);

//errorListener.setProperty("FileBasePath",basepath.getText().trim());
//BasePath=errorListener.getProperty("FileBasePath");
//ErrorListener.BASEPATH=BasePath.toString();

//Radio Button for Only Properties
//errorListener.setProperty("OnlyProperties",logOnlyProperties.isSelected());
//LogOnlyProperties=errorListener.getProperty("OnlyProperties");
//ErrorListener.ONLYPROPS=LogOnlyProperties.getBooleanValue();

//Radio Button for Errors and Properties
//errorListener.setProperty("ErrorsAndProperties",logErrorsAndProperties.isSelected());
//LogErrorsAndProperties=errorListener.getProperty("ErrorsAndProperties");
//ErrorListener.ERRORSANDPROPS=LogErrorsAndProperties.getBooleanValue();
}
}

@Override
public void clearGui() {
if(ErrorListener.updateAllowed==true)
{
super.clearGui();
}
}

private void init() {
setLayout(new BorderLayout());
setBorder(makeBorder());
Box box = Box.createVerticalBox();	
box.add(makeTitlePanel());
box.add(createBasePathPanel());
box.add(errorProperties);
add(createTablePanel(), BorderLayout.CENTER);
add(Box.createVerticalStrut(70), BorderLayout.EAST);add(createButtonPanel(),BorderLayout.WEST);
add(box, BorderLayout.NORTH);
}

public void browse_loc()
{
	JFileChooser j = new JFileChooser();
	j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	//Integer opt = j.showOpenDialog(this);
	}

public void actionPerformed(ActionEvent e) {
String action = e.getActionCommand();
JFileChooser j = new JFileChooser();
j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);


     if (action.equals(BRO)) {
	JOptionPane.showMessageDialog(null, "alert",propertyHeaderArray[0]+propertyHeaderArray[1]+propertyHeaderArray[2], JOptionPane.OK_OPTION);
    int returnVal = j.showOpenDialog(this);
    j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
    	basepath.setText(j.getSelectedFile().getAbsolutePath());
    	JOptionPane.showMessageDialog(null, "alert",basepath.getText(), JOptionPane.OK_OPTION);
    }
    }
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
createTableHeader();
}


private Component createTablePanel() 
{	
	table = new JTable();
	return makeScrollPane(table);
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
protected void createTableHeader()
{
	
}


protected void initTableModel() {	
}

// String listed23[] = {"Sample #","Start Time","Thread Name","Label","Sample Time(ms)","Status","Bytes","Latency"};

private JPanel createButtonPanel() {
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

int row=0;


protected void stopTableEditing() {
if (table.isEditing()) {
TableCellEditor cellEditor = table.getCellEditor(table.getEditingRow(), table.getEditingColumn());
cellEditor.stopCellEditing();
}
}

protected void deleteArgument() {
}

public void netw()
{
	browse.setActionCommand(BRO);
	browse.addActionListener(this);
	browse.setEnabled(true);
	}

public JPanel createBasePathPanel()
{
JLabel label = new JLabel("Base Directory:");
basepath = new JTextField(10);
basepath.setName(ErrorListener.BASEPATH);
label.setLabelFor(basepath);
browse.setActionCommand("bro");
JPanel basepathPanel = new JPanel(new BorderLayout(72, 0));
basepathPanel.add(label, BorderLayout.WEST);
basepathPanel.add(basepath, BorderLayout.CENTER);
basepathPanel.add(browse,BorderLayout.EAST);
netw();

return basepathPanel;
}

public void clearData() {
}

}
