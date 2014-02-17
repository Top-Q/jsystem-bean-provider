/*
 * Copyright 2005-2010 Ignis Software Tools Ltd. All rights reserved.
 */
package org.jsystemtest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import jsystem.framework.TestRunnerFrame;
import jsystem.framework.sut.SutEditor;
import jsystem.treeui.WaitDialog;
import jsystem.treeui.actionItems.IgnisAction;
import jsystem.treeui.images.ImageCenter;
import jsystem.treeui.suteditor.planner.FilterType;
import jsystem.treeui.utilities.GenericCellEditor;
import jsystem.utils.StringUtils;
import jsystem.utils.SwingUtils;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTreeTable;
import org.w3c.dom.Document;

import static org.jsystemtest.BeanValueTableRenderer.*;

/**
 * SUT planner dialog. Handles all GUI related operations.
 * 
 * @author Michael Oziransky
 * 
 */
public class BeanTreeDialog extends JDialog implements TreeSelectionListener, MouseListener, SutEditor {

	protected static final Logger log = Logger.getLogger(BeanTreeDialog.class.getName());

	private static final long serialVersionUID = 3140310446094044200L;

	protected JXTreeTable treeTable;

	protected AbstractBeanTreeNode selectedNode = null;

	protected BeanTreeTableModel treeTableModel = null;

	protected boolean enableAddToRoot = true;

	/*
	 * Window properties
	 */
	private static Properties properties = new Properties();
	private String propertiesKey = "general";
	private static String propertiesFileName = "sutTreeDialog.properties";
	private static int screenSizeWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
	private static int screenSizeHeigh = Toolkit.getDefaultToolkit().getScreenSize().height;

	private int width = 3 * screenSizeWidth / 4;
	private int height = screenSizeHeigh / 2;
	private int x = (screenSizeWidth - width) / 2;
	private int y = (screenSizeHeigh - height) / 2;
	private Map<String, Integer> map = new HashMap<String, Integer>(5);

	/*
	 * Tool bar items
	 */
	private JComboBox filterCombo;
	private JTextField filter;
	private JComboBox filterBy;
	private JButton saveButton;
	private JButton cancelButton;

    private NewAction newAction;
	private AddAction addAction;
	private RemoveAction removeAction;
    private HideAction hideAction;
	private UpAction upAction;
	private DownAction downAction;
	private SaveAction saveAction;
	private CancelAction cancelAction;
	private CollapseAction collapseAction;
	private ExpandAction expandAction;
	private List<ShowChildAction> addChildActionList;

	static {
		try {
			File propertiesFile = new File(propertiesFileName);
			if (propertiesFile.exists()) {
				FileInputStream fileInputStream = new FileInputStream(propertiesFile);
				properties.load(fileInputStream);
				fileInputStream.close();
			}
		} catch (Exception e) {
		}
	}

	// /*
	// * Menu items
	// */
	// private JMenuItem popupCollapseTree;
	// private JMenuItem popupExpandTree;
	// private JMenuItem popupRemoveSystemObject;
	// private JMenuItem popupRemoveProperty;

	public BeanTreeDialog(String title) {
		super(TestRunnerFrame.guiMainFrame);
		setTitle(title);
		for (String current : BeanTreeTableModel.cNames) {
			map.put(current, this.width / BeanTreeTableModel.cNames.length - 5);
		}
		setPropertiesKey(title);
		handleLoadWindowProperties();
		setModalityType(ModalityType.APPLICATION_MODAL);
		setPreferredSize(new Dimension(width, height));
		setLocation(x, y);
	}

	private void handleLoadWindowProperties() {
		try {
			Object widthObject = properties.getProperty(getWidthKey());
			Object heightObject = properties.getProperty(getHeightKey());
			Object xObject = properties.getProperty(getXLocationKey());
			Object yObject = properties.getProperty(getYLocationKey());

			width = getIntValueFromObject(widthObject, width, screenSizeWidth);
			height = getIntValueFromObject(heightObject, height, screenSizeHeigh);
			x = getIntValueFromObject(xObject, x, screenSizeWidth);
			y = getIntValueFromObject(yObject, y, screenSizeHeigh);

			for (String currenColumn : BeanTreeTableModel.cNames) {
				Integer integer = map.get(currenColumn);
				Object currentObject = properties
						.getProperty(getKey(toLowerCaseAndReplaceSpacesWith(currenColumn, ".")));
				integer = getIntValueFromObject(currentObject, integer, width);
				map.put(currenColumn, integer);
			}
		} catch (Exception e) {
		}
	}

	private void handleSaveWindowProperties(Dimension dim, Point point, JXTreeTable jxTreeTable) {
		boolean saveToFile = false;
		if (dim != null && (dim.width != this.width || dim.height != this.height)) {
			properties.setProperty(getWidthKey(), String.valueOf(dim.width));
			properties.setProperty(getHeightKey(), String.valueOf(dim.height));
			saveToFile = true;
		}
		if (point != null) {
			if (isLegalValue(point.x, screenSizeWidth) && point.x != this.x) {
				properties.setProperty(getXLocationKey(), String.valueOf(point.x));
				saveToFile = true;
			}
			if (isLegalValue(point.y, screenSizeHeigh) && point.y != this.y) {
				properties.setProperty(getYLocationKey(), String.valueOf(point.y));
				saveToFile = true;
			}
		}
		if (jxTreeTable != null) {
			for (int index = 0; index < jxTreeTable.getColumnCount(); index++) {
				TableColumn column = jxTreeTable.getColumn(index);
				String currentKey = getKey(toLowerCaseAndReplaceSpacesWith(column.getHeaderValue().toString(), "."));
				int currentColumnWidth = map.get(column.getHeaderValue());
				if (currentColumnWidth != column.getWidth()) {
					properties.setProperty(currentKey, String.valueOf(column.getWidth()));
					saveToFile = true;
				}
			}
		}
		if (saveToFile) {
			File propertiesFile = new File(getPropertiesFileName());
			try {
				FileOutputStream fileOutpoutStream = new FileOutputStream(propertiesFile);
				properties.store(fileOutpoutStream, null);
				fileOutpoutStream.close();
			} catch (Exception e) {
			}
		}
	}

	private int getIntValueFromObject(Object obj, int defaultValue, int maxSize) {
		if (obj != null) {
			try {
				int parseInt = Integer.parseInt(obj.toString());
				if (isLegalValue(parseInt, maxSize)) {
					return parseInt;
				}
			} catch (Exception e) {
			}
		}
		return defaultValue;
	}

	private boolean isLegalValue(int value, int maxSize) {
		return value > -1 && value < maxSize;
	}

	private void settingColumnsWidth() {
		for (int index = 0; index < treeTable.getColumnCount(); index++) {
			TableColumn currentColumn = treeTable.getColumn(index);
			Integer width = map.get(currentColumn.getHeaderValue());
			if (width != null) {
				treeTable.getColumn(index).setPreferredWidth(width);
			}
		}
	}

	private String getKey(String value) {
		return propertiesKey + "." + value;
	}

	private String getXLocationKey() {
		return getKey("x");
	}

	private String getYLocationKey() {
		return getKey("y");
	}

	private String getWidthKey() {
		return getKey("width");
	}

	private String getHeightKey() {
		return getKey("height");
	}

	public void valueChanged(TreeSelectionEvent e) {
		TreePath path = e.getPath();

		if (path != null) {
			selectedNode = (AbstractBeanTreeNode) path.getLastPathComponent();
            newAction.updateAction();
			addAction.updateAction();
			removeAction.updateAction();
            hideAction.updateAction();
			upAction.updateAction();
			downAction.updateAction();
			expandAction.updateAction();
			collapseAction.updateAction();
		}
	}

	public void mouseClicked(MouseEvent e) {
		// ignored
	}

	public void mouseEntered(MouseEvent e) {
		// ignored
	}

	public void mouseExited(MouseEvent e) {
		// ignored
	}

	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();

		// save the path for future use
		TreePath clickedPath = treeTable.getPathForLocation(x, y);

		if (e.getSource().equals(treeTable)) {
			if (clickedPath == null)
				return;
			// save the selected node
			selectedNode = (AbstractBeanTreeNode) clickedPath.getLastPathComponent();
			Vector<AbstractBeanTreeNode> hiddenChildren = selectedNode.getHiddenChildren();
			if (hiddenChildren != null && hiddenChildren.size() > 0) {
				addChildActionList = new ArrayList<ShowChildAction>();
				for (AbstractBeanTreeNode child : hiddenChildren) {
					ShowChildAction action = new ShowChildAction(this, selectedNode, child);
					addChildActionList.add(action);
				}
			}

		}

		// if this is the right button
		if (e.getButton() == MouseEvent.BUTTON3) {

			// show the popup menu
			getMenu().show(treeTable, x, y);
		}

	}

	public void mouseReleased(MouseEvent e) {
		// ignored
	}

	private JPopupMenu getMenu() {
		JPopupMenu popup = new JPopupMenu();
		popup.add(collapseAction);
		popup.add(expandAction);
		popup.add(new JSeparator());
        popup.add(newAction);
		popup.add(addAction);
		popup.add(removeAction);
		popup.add(upAction);
		popup.add(downAction);
        popup.add(hideAction);
		popup.add(new JSeparator());
		if (addChildActionList != null && addChildActionList.size() > 0) {
			for (ShowChildAction action : addChildActionList) {
				popup.add(action);
			}
			addChildActionList.clear();
		}
		return popup;
	}

	protected String selectFromSOList(ArrayList<String> soList) {
		ArrayList<ClassPackage> processedList = new ArrayList<ClassPackage>();
		for (String className : soList) {
			ClassPackage cp = new ClassPackage();
			cp.fullName = className;
			cp.name = StringUtils.getClassName(className);
			processedList.add(cp);
		}
		Collections.sort(processedList);

		Object selected = JOptionPane.showInputDialog(TestRunnerFrame.guiMainFrame,
				"Select from the System Object list", "Add System Object", JOptionPane.INFORMATION_MESSAGE, null,
				processedList.toArray(), null);
		if (selected == null) {
			return null;
		}
		return ((ClassPackage) selected).fullName;
	}

	@SuppressWarnings("serial")
	public void buildAndShowDialog(Object root) throws Exception {
		newAction = new NewAction(this);
        addAction = new AddAction(this);
		removeAction = new RemoveAction(this);
		upAction = new UpAction(this);
		downAction = new DownAction(this);
        hideAction = new HideAction(this);
		cancelAction = new CancelAction(this);
		saveAction = new SaveAction(this);
		expandAction = new ExpandAction(this);
		collapseAction = new CollapseAction(this);

		setLayout(new BorderLayout());

		JToolBar toolBar = SwingUtils.getJToolBarWithBgImage("Planner toolbar", JToolBar.HORIZONTAL, ImageCenter
				.getInstance().getImage(ImageCenter.ICON_TOP_TOOLBAR_BG));

		toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 1));
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		createToolBar(toolBar);

		add(toolBar, BorderLayout.NORTH);
		WaitDialog.endWaitDialog();
		WaitDialog.launchWaitDialog("Loading beans...", null);
		try {
			treeTableModel = BeanTreeTableModel.createNewModel(root);
		} finally {
			WaitDialog.endWaitDialog();
		}
		filterCombo.setModel(new DefaultComboBoxModel(BeanTreeTableModel.groups.toArray()) {
			@Override
			public int getSize() {
				return BeanTreeTableModel.groups.toArray().length;
			}

			@Override
			public Object getElementAt(int index) {
				return BeanTreeTableModel.groups.toArray()[index];
			}

		});
		filterCombo.setSelectedItem("");

		treeTableModel.setHasChanged(false);
        treeTable = new JXTreeTable(treeTableModel);
		/*treeTable = new JXTreeTable(treeTableModel) {
			private static final long serialVersionUID = -8037560184152941754L;

			public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
				Component comp = super.prepareRenderer(renderer, row, col);
				JComponent jcomp = (JComponent) comp;
				if (comp == jcomp) {
					String cellValue = (String) getValueAt(row, col) + " ";
					if (cellValue.trim().equals("")) {
						jcomp.setToolTipText(null);
						return comp;
					}
					cellValue = cellValue.replaceAll("\n, ", "<br>");
					String[] tipLines = cellValue.split("<br>");
					int maxLineLength = 50; // max line length
					String tip = "";
					for (String tipLine : tipLines) {
						if (tipLine.length() > maxLineLength) {
							int i = 0;
							int tipLineLength = tipLine.length();
							while (i < (tipLineLength - maxLineLength - 1)) {
								tip = tip.concat(tipLine.substring(i, i + maxLineLength));
								if (tip.charAt(tip.length() - 1) != ' ') {
									int indexOfNextSpace = tipLine.indexOf(" ", i + maxLineLength);
									if (indexOfNextSpace != -1) {
										tip = tip.concat(tipLine.substring(i + maxLineLength, indexOfNextSpace));
										i += (maxLineLength + (indexOfNextSpace - (i + maxLineLength)));
									} else {
										tip = tip.concat(tipLine.substring(i + maxLineLength));
										i += maxLineLength + (tipLine.substring(i + maxLineLength)).length();
									}
								} else {
									i += maxLineLength;
								}
								tip = tip.concat("<br>");
							}
							tip = tip.concat(tipLine.substring(i, tipLineLength));
						} else {
							tip = tip.concat(tipLine);
						}
						tip = tip.concat("<br>");
					}
					// remove all duplicated <br>
					tip = tip.replaceAll("<br><br>", "<br>");
					tip = "<html>" + tip + "</html>";
					jcomp.setToolTipText(tip);
				}
				return comp;
			}

		};*/
		treeTable.setRootVisible(true);
		//GenericCellEditor gce = new GenericCellEditor(treeTableModel);
		/*for (int i = 0; i < treeTable.getColumnCount(); i++) {
			treeTable.getColumnModel().getColumn(i).setCellEditor(gce);
		} */
        //System.out.println(treeTable.getColumnModel().getColumnCount());
        ComplexCellEditor cce = new ComplexCellEditor(treeTableModel);
        treeTable.getColumnModel().getColumn(BeanTreeTableModel.ColNames.CUR_VAL.ordinal()).setCellEditor(cce);

        TableColumn currValueColumn = treeTable.getColumnModel().getColumn(BeanTreeTableModel.ColNames.CUR_VAL.ordinal());

        currValueColumn.setCellRenderer(new BeanValueTableRenderer());
        treeTable.setDefaultRenderer(Boolean.class, new BeanValueTableRenderer());

		treeTable.getTreeSelectionModel().addTreeSelectionListener(this);

		treeTable.setTreeCellRenderer(new BeanTreeRenderer());

        currValueColumn.setCellRenderer(new BeanValueTableRenderer());
        TableCellRenderer tableCellRenderer = treeTable.getColumn(1).getCellRenderer();
        System.out.println(tableCellRenderer == null ? "No Cell Renderer" : tableCellRenderer.toString());


		treeTable.addMouseListener(this);

		treeTable.setSelectionBackground(Color.LIGHT_GRAY);
		treeTable.setSelectionForeground(Color.BLACK);

		treeTable.setBackground(new Color(0xf6, 0xf6, 0xf6));
		JTableHeader treeTableHeader = treeTable.getTableHeader();
		treeTableHeader.setBackground(new Color(0xe1, 0xe4, 0xe6));
		treeTableHeader.setFont(new Font("sansserif", Font.BOLD, 11));


		add(SwingUtils.getJScrollPaneWithWaterMark(
				ImageCenter.getInstance().getAwtImage(ImageCenter.ICON_TEST_TREE_BG), treeTable), BorderLayout.CENTER);

		// Create the bottom panel
		JPanel bottomPanel = SwingUtils.getJPannelWithBgImage(
				ImageCenter.getInstance().getImage(ImageCenter.ICON_SCEANRIO_TOOLBAR_BG), 0);
		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

		saveButton = new JButton(saveAction);
		cancelButton = new JButton(cancelAction);
		bottomPanel.add(saveButton);
		bottomPanel.add(cancelButton);
		add(bottomPanel, BorderLayout.SOUTH);

		setFocusable(true);
		pack();

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (treeTableModel.getHasChanged()) {
					int ans = JOptionPane.showConfirmDialog(BeanTreeDialog.this, getTitle()
							+ " has been changed. Would you like to save changes?", getTitle(),
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
					switch (ans) {
					case JOptionPane.YES_OPTION:
						// treeTableModel.toXml();
						dispose();
						break;
					case JOptionPane.NO_OPTION:
						dispose();
						break;
					case JOptionPane.CANCEL_OPTION:
						break;
					default:
						break;
					}
				} else {
					dispose();
				}
			}
		});
		treeTable.expandAll();
		settingColumnsWidth();
		setVisible(true);

	}

	public void expandAll() {
		treeTable.expandAll();
	}

	/**
	 * Start the editing process based on given sut file. This will block here
	 * until the dialog is closed.
	 * 
	 * @param doc
	 *            The XML document that will be the input to the sut editing
	 * @return doc The modified document or <code>null</code> in case the
	 *         operation was canceled
	 */
	public Document editSut(final Document doc, boolean withSave) throws Exception {
		buildAndShowDialog(doc);
		if (withSave) {
			handleSaveWindowProperties(this.getSize(), this.getLocation(), treeTable);
		}
		return doc;
	}

	public boolean isEditable(Document doc) throws Exception {
		return true;
	}

	private void createToolBar(JToolBar toolbar) {
		toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));

		JLabel filterByLabel = new JLabel("Filter by: ");
		toolbar.add(filterByLabel);

		filterBy = new JComboBox();
		filterBy.addItem("All");
		filterBy.addItem("Class");
		filterBy.addItem("Name");
		filterBy.addItem("Value");
		// filterBy.addActionListener(this);
		filterBy.setBackground(toolbar.getBackground());
		filterBy.setRenderer(new ComboBoxRenderer());
		filterBy.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				fieldChange();
			}
		});
		toolbar.add(filterBy);

		// filter = new JTextField(10);
		// filter.setToolTipText("Enter text to filter");
		// filter.addKeyListener(this);

		filterCombo = new JComboBox();
		filterCombo.setEditable(true);
		filterCombo.setToolTipText("Enter text to filter");
		filter = (JTextField) filterCombo.getEditor().getEditorComponent();
		filter.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {
				fieldChange();
			}

			public void insertUpdate(DocumentEvent e) {
				fieldChange();
			}

			public void changedUpdate(DocumentEvent e) {
				fieldChange();
			}
		});
		filter.setColumns(10);
		filterCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				fieldChange();
			}
		});

		toolbar.add(filterCombo);

        toolbar.add(newAction);
		toolbar.add(addAction);

		toolbar.add(removeAction);
		toolbar.add(upAction);
		toolbar.add(downAction);
        toolbar.add(hideAction);
	}

	public void keyPressed(KeyEvent e) {
	}

	DelaiedUpdater updater;

	public void fieldChange() {
		// The code was moved from keyTyped() method into keyReleased() method
		// since
		// keyTyped() method was not updated with the last pressed character.
		String searchText = filter.getText();
		treeTableModel.setFilter(searchText);
		String filterType = filterBy.getSelectedItem().toString();
		if (filterType.equals("All")) {
			treeTableModel.setFilterType(FilterType.ALL);
		} else if (filterType.equals("Class")) {
			treeTableModel.setFilterType(FilterType.CLASS);
		} else if (filterType.equals("Name")) {
			treeTableModel.setFilterType(FilterType.NAME);
		} else if (filterType.equals("Value")) {
			treeTableModel.setFilterType(FilterType.VALUE);
		}

		if (updater == null || !(updater.update())) {
			if (updater != null) {
				try {
					updater.join(15000);
				} catch (InterruptedException e1) {
					return;
				}
			}
			updater = new DelaiedUpdater(treeTableModel, this);
			updater.start();
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public boolean isEnableAddToRoot() {
		return enableAddToRoot;
	}

	public void setEnableAddToRoot(boolean enableAddToRoot) {
		this.enableAddToRoot = enableAddToRoot;
	}

	public void setPropertiesKey(String propertiesKey) {
		if (propertiesKey != null && !propertiesKey.trim().isEmpty()) {
			this.propertiesKey = toLowerCaseAndReplaceSpacesWith(propertiesKey, ".");
		}
	}

	public String getPropertiesKey() {
		return propertiesKey;
	}

	public void setPropertiesFileName(String propertiesFileName) {
		if (propertiesFileName != null && !propertiesFileName.trim().isEmpty()) {
			BeanTreeDialog.propertiesFileName = toLowerCaseAndReplaceSpacesWith(propertiesFileName, "") + ".properties";
		}
	}

	private String toLowerCaseAndReplaceSpacesWith(String data, String replaceWith) {
		return data.trim().toLowerCase().replaceAll("\\s+", replaceWith);
	}

	public String getPropertiesFileName() {
		return propertiesFileName;
	}
}

class ShowChildAction extends IgnisAction {
	private static final long serialVersionUID = 1L;
	private BeanTreeDialog dialog;
	private AbstractBeanTreeNode parent, child;

	ShowChildAction(BeanTreeDialog dialog, AbstractBeanTreeNode parent, AbstractBeanTreeNode child) {
		String name = "Show " + child.getName();
		putValue(Action.SHORT_DESCRIPTION, name);
		putValue(Action.NAME, name);
		putValue(Action.ACTION_COMMAND_KEY, name);
		this.dialog = dialog;
		this.parent = parent;
		this.child = child;
		setEnabled(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dialog.treeTableModel.setChildToVisible(parent, child);
	}

	public void updateAction() {
		if (dialog.selectedNode.isLeaf()) {
			setEnabled(false);
		} else {
			setEnabled(true);
		}
	}

}

class ExpandAction extends IgnisAction {
	private static final long serialVersionUID = 1L;
	private BeanTreeDialog dialog;

	ExpandAction(BeanTreeDialog dialog) {
		putValue(Action.SHORT_DESCRIPTION, "Expand");
		putValue(Action.NAME, "Expand");
		putValue(Action.ACTION_COMMAND_KEY, "Expand");
		this.dialog = dialog;
		setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dialog.treeTable.expandPath(dialog.treeTable.getPathForRow(dialog.treeTable.getSelectedRow()));
	}

	public void updateAction() {
		if (dialog.selectedNode.isLeaf()) {
			setEnabled(false);
		} else {
			setEnabled(true);
		}
	}
}

class CollapseAction extends IgnisAction {
	private static final long serialVersionUID = 1L;
	private BeanTreeDialog dialog;

	CollapseAction(BeanTreeDialog dialog) {
		putValue(Action.SHORT_DESCRIPTION, "Collapse");
		putValue(Action.NAME, "Collapse");
		putValue(Action.ACTION_COMMAND_KEY, "Collapse");
		this.dialog = dialog;
		setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dialog.treeTable.collapsePath(dialog.treeTable.getPathForRow(dialog.treeTable.getSelectedRow()));
	}

	public void updateAction() {
		if (dialog.selectedNode.isLeaf()) {
			setEnabled(false);
		} else {
			setEnabled(true);
		}
	}
}

class SaveAction extends IgnisAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BeanTreeDialog dialog;

	SaveAction(BeanTreeDialog dialog) {
		putValue(Action.SHORT_DESCRIPTION, "Save");
		putValue(Action.NAME, "Save");
		putValue(Action.ACTION_COMMAND_KEY, "Save");
		this.dialog = dialog;
		setEnabled(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// dialog.treeTableModel.toXml();
		// Document doc = dialog.treeTableModel.getDocument();
		// try {
		// FileUtils.saveDocumentToFile(doc, new File("test.xml"));
		// } catch (Exception e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// return;
		// }
		// List<SutValidationError> errors = new
		// ArrayList<SutValidationError>();
		// ArrayList<SutValidationError> nodeErrors = new
		// ArrayList<SutValidationError>();
		// ((AbstractBeanTreeNode)
		// dialog.treeTableModel.getRoot()).getValidationErrors(nodeErrors);
		// errors.addAll(nodeErrors);
		// SutValidator[] validators = SutImpl.getSutValidators(doc);
		// if (validators != null && validators.length > 0) {
		// for (SutValidator validator : validators) {
		// SutValidationError[] verrors = validator.getValidationErrors(doc);
		// if (verrors != null && verrors.length > 0) {
		// Collections.addAll(errors, verrors);
		// }
		// }
		// }
		// if (errors.isEmpty()) {
		// dialog.treeTableModel.setHasChanged(false);
		// } else {
		// StringBuffer errorBuffer = new StringBuffer();
		// for (SutValidationError error : errors) {
		// errorBuffer.append(error.getSevirity().name());
		// errorBuffer.append(": ");
		// errorBuffer.append(error.getMessage());
		// errorBuffer.append("\r\n");
		// }
		// int result = JOptionPane.showConfirmDialog(dialog,
		// "Following error were found:\r\n" + errorBuffer.toString() + "Save?",
		// "Sut Validation Errors",
		// JOptionPane.YES_NO_CANCEL_OPTION);
		// if (result == JOptionPane.CANCEL_OPTION) {
		// return;
		// } else if (result == JOptionPane.YES_OPTION) {
		// dialog.treeTableModel.setHasChanged(false);
		// }
		// }
		// dialog.dispose();
	}
}

class CancelAction extends IgnisAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BeanTreeDialog dialog;

	CancelAction(BeanTreeDialog dialog) {
		putValue(Action.SHORT_DESCRIPTION, "Cancel");
		putValue(Action.NAME, "Cancel");
		putValue(Action.ACTION_COMMAND_KEY, "Cancel");
		this.dialog = dialog;
		setEnabled(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dialog.dispose();
	}

}

/**
 * New instance action
 */
class NewAction extends IgnisAction {
    private static final long serialVersionUID = 1L;
    private BeanTreeDialog dialog;

    NewAction(BeanTreeDialog dialog) {
        putValue(Action.SHORT_DESCRIPTION, "New");
        putValue(Action.NAME, "New");
        putValue(Action.ACTION_COMMAND_KEY, "new");
        putValue(Action.SMALL_ICON, ImageCenter.getInstance().getImage(ImageCenter.ICON_NEW));
        putValue(Action.LARGE_ICON_KEY, ImageCenter.getInstance().getImage(ImageCenter.ICON_NEW));
        this.dialog = dialog;
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        AbstractBeanTreeNode node = dialog.selectedNode;
        switch (node.getNodeType()) {
            case BEAN:


                if(node.objType.isArray()) {
                    // When instantiating and array: we first need to remove the tree-node's children from the tree
                    dialog.treeTableModel.removeArrayNodeChildren(node);
                }
                Object newValue = node.getObjectDefaultValueInstance(node.objType);
                dialog.treeTableModel.setValueAt(newValue, node, BeanTreeTableModel.ColNames.CUR_VAL.ordinal());
                break;
            default:
                return;
        }
    }

    public void updateAction() {
        switch (dialog.selectedNode.getNodeType()) {
            case BEAN:
                setEnabled(true);
                break;
            case ROOT:
                putValue(Action.SHORT_DESCRIPTION, "New System Object");
                setEnabled(dialog.enableAddToRoot);
                break;
            case PRIMITIVE:
                putValue(Action.SHORT_DESCRIPTION, "New System Object");
                setEnabled(true);
                break;
            default:
                putValue(Action.SHORT_DESCRIPTION, "New");
                setEnabled(false);

        }
    }
}


/**
 * Add action
 */
class AddAction extends IgnisAction {
	private static final long serialVersionUID = 1L;
	private BeanTreeDialog dialog;

	AddAction(BeanTreeDialog dialog) {
		putValue(Action.SHORT_DESCRIPTION, "Add");
		putValue(Action.NAME, "Add");
		putValue(Action.ACTION_COMMAND_KEY, "add");
		putValue(Action.SMALL_ICON, ImageCenter.getInstance().getImage(ImageCenter.ICON_TEST_PASS));
		putValue(Action.LARGE_ICON_KEY, ImageCenter.getInstance().getImage(ImageCenter.ICON_TEST_PASS));
		this.dialog = dialog;
		setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		AbstractBeanTreeNode node = dialog.selectedNode;
		String baseClass = null;
		Enumeration<AbstractBeanTreeNode> children = null;
		switch (node.getNodeType()) {
		case BEAN:
            if(node.objType.isArray()) {
                if(node.getUserObject() == null) {
                    // Instantiating the array object (when null)
                    Object newValue = node.getObjectDefaultValueInstance(node.objType);
                    dialog.treeTableModel.setValueAt(newValue, node, BeanTreeTableModel.ColNames.CUR_VAL.ordinal());
                }

                // Adds a new child instance to the array
                dialog.treeTableModel.addArrayChildNode(node, node.getChildCount());
            } else {
                AbstractBeanTreeNode parentNode = node.getBeanParent();
                if(parentNode != null) {
                    if(parentNode.objType.isArray()) {
                        int position = parentNode.getIndex(node);
                        if(parentNode.getUserObject() == null) {
                            // Instantiating the parent's array object (when null)
                            Object newValue = parentNode.getObjectDefaultValueInstance(parentNode.objType);
                            dialog.treeTableModel.setValueAt(newValue, parentNode, BeanTreeTableModel.ColNames.CUR_VAL.ordinal());
                        }

                        dialog.treeTableModel.addArrayChildNode(parentNode, position);
                    }
                }
            }
			baseClass = node.getClassName();
			break;
		// case EXTENTION_ARRAY_SO:
		// baseClass = node.getClassName();
		// break;
		// case ARRAY_SO:
		// baseClass = node.getArraySuperClassName();
		// break;
		case PRIMITIVE:
			baseClass = node.getClassName();
			break;
		case ROOT:
			baseClass = null;
			break;
		// case SUB_SO:
		// case TAG:
		// case OPTIONAL_TAG:
		default:
			return;
		}
		try {

			children = dialog.selectedNode.children();
		} catch (Exception e1) {
			BeanTreeDialog.log.log(Level.WARNING, "Fail to process system objects", e1);
			return;
		}
		while (children.hasMoreElements()) {
			System.out.println(children.nextElement().toString());
		}

	}

	public void expandSubPath(TreePath p) {
		TreeModel data = dialog.treeTableModel;
		if (p == null) {
			return;
		}
		Object node = p.getLastPathComponent();
		while (true) {
			int count = data.getChildCount(node);
			if (count == 0)
				break;
			node = data.getChild(node, count - 1);
			p = p.pathByAddingChild(node);
		}
		dialog.treeTable.scrollPathToVisible(p);
	}

	public void updateAction() {
        AbstractBeanTreeNode node = dialog.selectedNode;
		switch (node.getNodeType()) {
		// case SUB_SO:
        case ROOT:
        case BEAN:
            if(node.objType.isArray()
                    || (node.getParent() != null && node.getBeanParent().objType.isArray()))
            {
                putValue(Action.SHORT_DESCRIPTION, "Add Array Element");
                setEnabled(true);
            } else {
                setEnabled(false);
            }
			break;
            //putValue(Action.SHORT_DESCRIPTION, "Add System Object");
            //setEnabled(dialog.enableAddToRoot);
          //break;
		// case EXTENTION_ARRAY_SO:
		// case ARRAY_SO:
		// putValue(Action.SHORT_DESCRIPTION, "Add New Array Element");
		// setEnabled(true);
		// break;
		case PRIMITIVE:
			putValue(Action.SHORT_DESCRIPTION, "Add System Object");
			setEnabled(true);
			break;
		default:
			putValue(Action.SHORT_DESCRIPTION, "Add");
			setEnabled(false);

		}
	}
}

/**
 * Remove action
 */
class RemoveAction extends IgnisAction {
	private static final long serialVersionUID = 1L;
	private BeanTreeDialog dialog;

	RemoveAction(BeanTreeDialog dialog) {
		putValue(Action.SHORT_DESCRIPTION, "Remove");
		putValue(Action.NAME, "Remove");
		putValue(Action.ACTION_COMMAND_KEY, "Remove");
		putValue(Action.SMALL_ICON, ImageCenter.getInstance().getImage(ImageCenter.ICON_DELETE));
		putValue(Action.LARGE_ICON_KEY, ImageCenter.getInstance().getImage(ImageCenter.ICON_DELETE));
		this.dialog = dialog;
		setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			dialog.treeTableModel.removeArrayChildNode(dialog.selectedNode);
//            dialog.treeTableModel.setChildToHidden(dialog.selectedNode);
		} catch (Exception e1) {
			BeanTreeDialog.log.log(Level.WARNING, "Fail to remove array element", e1);
		}
	}

	public void updateAction() {
		switch (dialog.selectedNode.getNodeType()) {
		// case TAG:
		// putValue(Action.SHORT_DESCRIPTION, "Remove Property");
		// setEnabled(true);
		// break;
		// case OPTIONAL_TAG:
		// case EXTENTION_ARRAY_SO:
		// case SUB_SO:
		case PRIMITIVE:
		case ROOT:
			putValue(Action.SHORT_DESCRIPTION, "Remove");
			setEnabled(false);
			break;
		// case ARRAY_SO:
		case BEAN:
            boolean setToEnabled = AbstractBeanTreeNode.isParentAnArray(dialog.selectedNode);
            putValue(Action.SHORT_DESCRIPTION, "Remove Array Element");
            setEnabled(setToEnabled);
			break;
		default:
			putValue(Action.SHORT_DESCRIPTION, "Remove");
			setEnabled(false);

		}
	}
}


/**
 * Remove action
 */
class HideAction extends IgnisAction {
    private static final long serialVersionUID = 1L;
    private BeanTreeDialog dialog;

    HideAction(BeanTreeDialog dialog) {
        putValue(Action.SHORT_DESCRIPTION, "Hide");
        putValue(Action.NAME, "Hide");
        putValue(Action.ACTION_COMMAND_KEY, "Hide");
        putValue(Action.SMALL_ICON, ImageCenter.getInstance().getImage(ImageCenter.ICON_EMPTY));
        putValue(Action.LARGE_ICON_KEY, ImageCenter.getInstance().getImage(ImageCenter.ICON_EMPTY));
        this.dialog = dialog;
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            dialog.treeTableModel.setChildToHidden(dialog.selectedNode);
        } catch (Exception e1) {
            BeanTreeDialog.log.log(Level.WARNING, "Fail to hide node", e1);
        }
    }

    public void updateAction() {
        switch (dialog.selectedNode.getNodeType()) {
            case PRIMITIVE:
            case ROOT:
                putValue(Action.SHORT_DESCRIPTION, "Hide");
                setEnabled(false);
                break;
            // case ARRAY_SO:
            case BEAN:

                putValue(Action.SHORT_DESCRIPTION, "Hide Object");
                setEnabled(true);
                break;
            default:
                putValue(Action.SHORT_DESCRIPTION, "Hide");
                setEnabled(false);

        }
    }
}

/**
 * Up action
 */
class UpAction extends IgnisAction {
	private static final long serialVersionUID = 1L;
	private BeanTreeDialog dialog;

	UpAction(BeanTreeDialog dialog) {
		putValue(Action.SHORT_DESCRIPTION, "Up");
		putValue(Action.NAME, "Up");
		putValue(Action.ACTION_COMMAND_KEY, "Up");
		putValue(Action.SMALL_ICON, ImageCenter.getInstance().getImage(ImageCenter.ICON_UP));
		putValue(Action.LARGE_ICON_KEY, ImageCenter.getInstance().getImage(ImageCenter.ICON_UP));
		this.dialog = dialog;
		setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			dialog.treeTableModel.moveUp(dialog.selectedNode);
		} catch (Exception e1) {
			BeanTreeDialog.log.log(Level.WARNING, "Fail to move object up system objects", e1);
		}
	}

	public void updateAction() {
		switch (dialog.selectedNode.getNodeType()) {
		// case OPTIONAL_TAG:
		// case EXTENTION_ARRAY_SO:
		case ROOT:
		case PRIMITIVE:
			// case TAG:
			// setEnabled(false);
			// break;
			// case SUB_SO:
			// case ARRAY_SO:
		case BEAN:
			setEnabled(dialog.treeTableModel.canMoveUp(dialog.selectedNode));
			break;
		default:
			setEnabled(false);

		}
	}
}

/**
 * Up action
 */
class DownAction extends IgnisAction {
	private static final long serialVersionUID = 1L;
	private BeanTreeDialog dialog;

	DownAction(BeanTreeDialog dialog) {
		putValue(Action.SHORT_DESCRIPTION, "Down");
		putValue(Action.NAME, "Down");
		putValue(Action.ACTION_COMMAND_KEY, "Down");
		putValue(Action.SMALL_ICON, ImageCenter.getInstance().getImage(ImageCenter.ICON_DOWN));
		putValue(Action.LARGE_ICON_KEY, ImageCenter.getInstance().getImage(ImageCenter.ICON_DOWN));
		this.dialog = dialog;
		setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			dialog.treeTableModel.moveDown(dialog.selectedNode);
		} catch (Exception e1) {
			BeanTreeDialog.log.log(Level.WARNING, "Fail to move object down system objects", e1);
		}
	}

	public void updateAction() {
		switch (dialog.selectedNode.getNodeType()) {
		// case OPTIONAL_TAG:
		// case EXTENTION_ARRAY_SO:
		case ROOT:
		case PRIMITIVE:
			// case TAG:
			setEnabled(false);
			break;
		// case SUB_SO:
		// case ARRAY_SO:
		case BEAN:
			setEnabled(dialog.treeTableModel.canMoveDown(dialog.selectedNode));
			break;
		default:
			setEnabled(false);

		}
	}
}

class ComboBoxRenderer extends BasicComboBoxRenderer {

	private static final long serialVersionUID = 1L;

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
			if (-1 < index) {
				list.setToolTipText((value == null) ? "" : new String(value.toString()));
			}
		} else {
			setBackground(Color.white);
			setForeground(Color.black);
		}
		setFont(list.getFont());
		setText((value == null) ? "" : value.toString());
		return this;
	}
}

class ClassPackage implements Comparable<Object> {
	public String name;
	public String fullName;

	public int compareTo(Object o) {
		return name.compareTo(o.toString());
	}

	public String toString() {
		return name;
	}
}

/**
 * The updater wait for half of second silent in the text field and only then it
 * will be updated.
 * 
 * @author guy.arieli
 * 
 */
class DelaiedUpdater extends Thread {
	long endTime = 0;
	boolean updated = true;
	BeanTreeTableModel model;
	BeanTreeDialog std;

	public DelaiedUpdater(BeanTreeTableModel model, BeanTreeDialog std) {
		this.model = model;
		this.std = std;
	}

	public void run() {
		endTime = System.currentTimeMillis() + 500l;
		while (System.currentTimeMillis() < endTime) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return;
			}
		}
		updated = false;
		model.refresh();
		std.expandAll();
	}

	public boolean update() {
		endTime = System.currentTimeMillis() + 500l;
		return updated;
	}
}