package org.jsystemtest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JTable;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import jsystem.framework.scenario.ParameterProvider;
import jsystem.treeui.suteditor.planner.FilterType;
import jsystem.treeui.utilities.CellEditorModel;
import jsystem.utils.beans.CellEditorType;

import org.apache.commons.jxpath.Functions;
import org.apache.commons.jxpath.JXPathContext;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.w3c.dom.Document;

public class BeanTreeTableModel extends AbstractTreeTableModel implements CellEditorModel {

	//private Object root;
    private static BeanTreeNode rootNode;

	private boolean hasChanged;

	static HashSet<String> groups = new HashSet<String>();

	static protected String[] cNames = { "col0", "Class name", "Default value", "col3", "Java documentation" };

	public BeanTreeTableModel(DefaultMutableTreeNode root) {
        //super(new BeanTreeNode(BeanTreeNode.NodeType.ROOT, "Root Node"));
        super(root);
        //rootNode = root;//new BeanTreeNode(BeanTreeNode.NodeType.ROOT, "Root Node");

        //super(rootNode);
		//this.root = root;
	}

	public int getColumnCount() {
		return cNames.length;
	}

	public String getColumnName(int column) {
		return cNames[column];
	}

    // Moshe's impl.
	public Object getValueAt(Object node, int column) {
        BeanTreeNode beanTreeNode = (BeanTreeNode)node;

        try {
            switch (column) {
                case 0:
                    return beanTreeNode.getName();
                case 1: //'Class name' column
                    return beanTreeNode.getClassName();
                case 2: //'Default value' column
                    //return beanTreeNode.getDefaultValue();
                    return "";
                case 3: //'Actual value' column
                    //return beanTreeNode.getActualValue();
                    return "";
                case 4: // 'Java documentation' column
                    //return beanTreeNode.getJavadoc().replaceAll("\n", "\n, ");
                    return "";
            }
        }
        catch  (SecurityException se) { }

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

    /**
     * Moshe's impl.
     * @param parent
     * @param child
     * @return index of child in parent or -1 if no match was found.
     */
	public int getIndexOfChild(Object parent, Object child) {
		String childObjName = child.getClass().getSimpleName();
        String expectedGetMethodName = "get" + childObjName.substring(0,1).toUpperCase() + childObjName.substring(1);
        int index = 0;
        Method[] methods = parent.getClass().getMethods();
        for (Method method : methods) {
            String currentMethodName = method.getName();
            if (currentMethodName.startsWith("get")) {
                if(currentMethodName.equals(expectedGetMethodName)) {
                    return index;
                }
                if (currentMethodName.equals("getClass")) {
                    continue;
                }
                index++;
            }
        }
        return -1; // Non found
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

	/*public void moveUp(BeanTreeNode selectedNode) {
		// TODO Auto-generated method stub

	}*/



    // Moshe's impl.
    public void moveUp(BeanTreeNode selectedNode) {
        BeanTreeNode parent = selectedNode.getParent();
        int selectedNodeIndex = this.getIndexOfChild(parent, selectedNode);
        int upperNodeIndex = (selectedNodeIndex - 1);
        BeanTreeNode upperNode = (BeanTreeNode)this.getChild(parent, upperNodeIndex);
        parent.remove(selectedNodeIndex);
        TreePath parentPath = new TreePath(parent.getPath());
        /** TODO5 - handle ARRAY_SO
        if(selectedNode.getType() == BeanTreeNode.NodeType.ARRAY_SO){
            selectedNode.setIndex(upperNodeIndex);
            upperNode.setIndex(selectedNodeIndex);
        }
        **/
        modelSupport.fireChildRemoved(parentPath, selectedNodeIndex, selectedNode);
        parent.insert(selectedNode, upperNodeIndex);
        modelSupport.fireChildAdded(parentPath, upperNodeIndex, selectedNode);
    }


    // Moshe's impl.
    public void moveDown(BeanTreeNode selectedNode) {
        BeanTreeNode parent = selectedNode.getParent();
        int selectedNodeIndex = this.getIndexOfChild(parent, selectedNode);
        int belowNodeIndex = (selectedNodeIndex + 1);
        BeanTreeNode belowNode = (BeanTreeNode)this.getChild(parent, belowNodeIndex);
        parent.remove(selectedNodeIndex);
        TreePath parentPath = new TreePath(parent.getPath());
        /** TODO5 - handle ARRAY_SO
        if(selectedNode.getType() == NodeType.ARRAY_SO) {
            int index = selectedNode.getIndex();
            selectedNode.setIndex(belowNode.getIndex());
            belowNode.setIndex(index);
        }
        **/
        modelSupport.fireChildRemoved(parentPath, selectedNodeIndex, selectedNode);
        parent.insert(selectedNode, belowNodeIndex);
        modelSupport.fireChildAdded(parentPath, belowNodeIndex, selectedNode);
    }

    // Moshe's impl.
	public boolean canMoveUp(BeanTreeNode selectedNode) {
        BeanTreeNode parentNode = selectedNode.getParent();
        if(false == selectedNode.isLeaf() && null != parentNode) {
            int indexInParent = this.getIndexOfChild(parentNode, selectedNode);
            return (indexInParent > 0);
        }
        return false;
	}

	/*public void moveDown(BeanTreeNode selectedNode) {
		// TODO Auto-generated method stub

	}*/

	public static BeanTreeTableModel createNewModel(Object root) {
		// TODO: This needs to get the root of the beans tree
        rootNode = new BeanTreeNode(BeanTreeNode.NodeType.ROOT, "Root Bean", root);
		return new BeanTreeTableModel(new DefaultMutableTreeNode("ROOT NODE"));// rootNode);
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

    // Moshe's impl.
	public boolean canMoveDown(BeanTreeNode selectedNode) {
        BeanTreeNode parentNode = selectedNode.getParent();
        if(false == selectedNode.isLeaf() && null != parentNode) {
            int indexInParent = this.getIndexOfChild(parentNode, selectedNode);
            return (indexInParent < (this.getChildCount(parentNode) - 1));
        }
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

    // Moshe's impl.
	public Class<?> getCellType(JTable table, int row, int column) {
        /**
         * TODO 4 - complete getCellType
         **/
        BeanTreeNode node = (BeanTreeNode)((JXTreeTable)table).getPathForRow(row).getLastPathComponent();
        /*if (node.getType() != null) {
            return node.getType();
        }*/
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
