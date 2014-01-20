package org.jsystemtest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JTable;

import jsystem.framework.scenario.ParameterProvider;
import jsystem.treeui.suteditor.planner.FilterType;
import jsystem.treeui.utilities.CellEditorModel;
import jsystem.utils.beans.CellEditorType;

import org.apache.commons.jxpath.Functions;
import org.apache.commons.jxpath.JXPathContext;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.w3c.dom.Document;

public class BeanTreeTableModel extends AbstractTreeTableModel implements CellEditorModel {

	private Object root;

	private boolean hasChanged;

	static HashSet<String> groups = new HashSet<String>();

	static protected String[] cNames = { "col0", "Class name", "Default value", "col3", "Java documentation" };

	public BeanTreeTableModel(Object root) {
		this.root = root;
	}

	public int getColumnCount() {
		return cNames.length;
	}

	public String getColumnName(int column) {
		return cNames[column];
	}

	public Object getValueAt(Object node, int column) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getChild(Object parent, int index) {
		Method[] methods = parent.getClass().getMethods();
		int count = 0;
		for (Method method : methods) {
			if (method.getName().startsWith("get")) {
				if (method.getName().equals("getClass")) {
					continue;
				}
				if (count++ == index) {
					try {
						return method.invoke(parent, null);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				}
			}
		}
		return null;
	}

	public int getChildCount(Object parent) {
		Method[] methods = parent.getClass().getMethods();
		int count = 0;
		for (Method method : methods) {
			if (method.getName().startsWith("get")) {
				if (method.getName().equals("getClass")) {
					continue;
				}
				count++;
			}
		}
		return count;
	}

	private static boolean isBean(Type type) {
		if (type.toString().contains("java.lang.String")) {
			return false;
		}
		if (type.toString().contains("int")) {
			return false;
		}
		if (type.toString().contains("float")) {
			return false;
		}
		if (type.toString().contains("double")) {
			return false;
		}
		if (type.toString().contains("long")) {
			return false;
		}
		if (type.toString().contains("byte")) {
			return false;
		}
		if (type.toString().contains("short")) {
			return false;
		}
		if (type.toString().contains("boolean")) {
			return false;
		}
		if (type.toString().contains("char")) {
			return false;
		}

		return true;

	}

	public int getIndexOfChild(Object parent, Object child) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setFilter(String searchText) {
		// TODO Auto-generated method stub

	}

	public void setFilterType(FilterType all) {
		// TODO Auto-generated method stub

	}

	public void toXml() {
		// TODO Auto-generated method stub

	}

	public Document getDocument() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeObject(BeanTreeNode node, boolean b) {
		// TODO Auto-generated method stub

	}

	public void addSystemObject(BeanTreeNode parent, Object name, String className) {
		// TODO Auto-generated method stub

	}

	public void moveUp(BeanTreeNode selectedNode) {
		// TODO Auto-generated method stub

	}

	public boolean canMoveUp(BeanTreeNode selectedNode) {
		// TODO Auto-generated method stub
		return false;
	}

	public void moveDown(BeanTreeNode selectedNode) {
		// TODO Auto-generated method stub

	}

	public static BeanTreeTableModel createNewModel(Object root) {
		// TODO: This needs to get the root of the beans tree
		return new BeanTreeTableModel(root);
	}

	public void setHasChanged(boolean hasChanged) {
		this.hasChanged = hasChanged;

	}

	public boolean getHasChanged() {
		return hasChanged;
	}

	public ArrayList<String> getBeansOfType(String baseClass) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addArraySystemObject(BeanTreeNode node, Object name, String className) {
		// TODO Auto-generated method stub

	}

	public boolean canMoveDown(BeanTreeNode selectedNode) {
		// TODO Auto-generated method stub
		return false;
	}

	public void refresh() {
		// TODO Auto-generated method stub

	}

	public CellEditorType getEditorType(JTable table, int row, int column) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getOptions(JTable table, int row, int column) {
		// TODO Auto-generated method stub
		return null;
	}

	public ParameterProvider getProvider(JTable table, int row, int column) {
		// TODO Auto-generated method stub
		return null;
	}

	public Class<?> getCellType(JTable table, int row, int column) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isValidData(JTable table, int row, int column, Object enteredValue) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getLastValidationMessage() {
		// TODO Auto-generated method stub
		return null;
	}

}
