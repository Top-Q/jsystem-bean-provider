package org.jsystemtest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JTable;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import jsystem.framework.scenario.ParameterProvider;
import jsystem.treeui.suteditor.planner.FilterType;
import jsystem.treeui.utilities.CellEditorModel;
import jsystem.utils.beans.CellEditorType;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

public class BeanTreeTableModel extends AbstractTreeTableModel implements CellEditorModel {

	// private Object root;
	private static AbstractBeanTreeNode rootNode;

	private boolean hasChanged;

	static HashSet<String> groups = new HashSet<String>();

	static protected String[] cNames = { "Name", "Class name", "Current value", "New Value", "Java documentation" };

	public BeanTreeTableModel(DefaultMutableTreeNode root) {
		super(root);
	}

	public int getColumnCount() {
		return cNames.length;
	}

	public String getColumnName(int column) {
		return cNames[column];
	}

	// Moshe's impl.
	public Object getValueAt(Object node, int column) {
		if (!(node instanceof AbstractBeanTreeNode)) {
			return null;
		}
		AbstractBeanTreeNode beanTreeNode = (AbstractBeanTreeNode) node;

		try {
			switch (column) {
			case 0:
				return beanTreeNode.getName();
			case 1: // 'Class name' column
				return beanTreeNode.getClassName();
			case 2: // 'Default value' column
				// return beanTreeNode.getDefaultValue();
				return beanTreeNode.getValue();
			case 3: // 'Actual value' column
				// return beanTreeNode.getActualValue();
				return "";
			case 4: // 'Java documentation' column
				// return beanTreeNode.getJavadoc().replaceAll("\n", "\n, ");
				return "";
			}
		} catch (SecurityException se) {
		}

		return null;
	}

	public int getChildCount(Object parent) {
		if (parent instanceof AbstractBeanTreeNode) {
			return ((AbstractBeanTreeNode) parent).getChildCount();
		}
		return 0;
	}

	public Object getChild(Object parent, int index) {
		if (parent instanceof AbstractBeanTreeNode) {
			return ((AbstractBeanTreeNode) parent).getChildAt(index);
		}
		return null;
	}

	/**
	 * Moshe's impl.
	 * 
	 * @param parent
	 * @param child
	 * @return index of child in parent or -1 if no match was found.
	 */
	public int getIndexOfChild(Object parent, Object child) {
		if (parent instanceof AbstractBeanTreeNode) {
			return ((AbstractBeanTreeNode) parent).getIndex((AbstractBeanTreeNode) child);
		}
		return -1; // Non found
	}

	public void setFilter(String searchText) {
		// TODO Auto-generated method stub

	}

	public void setFilterType(FilterType all) {
		// TODO Auto-generated method stub

	}

	public void removeObject(AbstractBeanTreeNode node, boolean b) {
		// TODO Auto-generated method stub

	}

	/*
	 * public void moveUp(BeanTreeNode selectedNode) { // TODO Auto-generated
	 * method stub
	 * 
	 * }
	 */

	// Moshe's impl.
	public void moveUp(AbstractBeanTreeNode selectedNode) {
		AbstractBeanTreeNode parent = (AbstractBeanTreeNode) selectedNode.getParent();
		int selectedNodeIndex = this.getIndexOfChild(parent, selectedNode);
		int upperNodeIndex = (selectedNodeIndex - 1);
		AbstractBeanTreeNode upperNode = (AbstractBeanTreeNode) this.getChild(parent, upperNodeIndex);
		parent.remove(selectedNodeIndex);
		TreePath parentPath = new TreePath(parent.getPath());
		/**
		 * TODO5 - handle ARRAY_SO if(selectedNode.getType() ==
		 * BeanTreeNode.NodeType.ARRAY_SO){
		 * selectedNode.setIndex(upperNodeIndex);
		 * upperNode.setIndex(selectedNodeIndex); }
		 **/
		modelSupport.fireChildRemoved(parentPath, selectedNodeIndex, selectedNode);
		parent.insert(selectedNode, upperNodeIndex);
		modelSupport.fireChildAdded(parentPath, upperNodeIndex, selectedNode);
	}

	// Moshe's impl.
	public void moveDown(AbstractBeanTreeNode selectedNode) {
		AbstractBeanTreeNode parent = (AbstractBeanTreeNode) selectedNode.getParent();
		int selectedNodeIndex = this.getIndexOfChild(parent, selectedNode);
		int belowNodeIndex = (selectedNodeIndex + 1);
		AbstractBeanTreeNode belowNode = (AbstractBeanTreeNode) this.getChild(parent, belowNodeIndex);
		parent.remove(selectedNodeIndex);
		TreePath parentPath = new TreePath(parent.getPath());
		/**
		 * TODO5 - handle ARRAY_SO if(selectedNode.getType() ==
		 * NodeType.ARRAY_SO) { int index = selectedNode.getIndex();
		 * selectedNode.setIndex(belowNode.getIndex());
		 * belowNode.setIndex(index); }
		 **/
		modelSupport.fireChildRemoved(parentPath, selectedNodeIndex, selectedNode);
		parent.insert(selectedNode, belowNodeIndex);
		modelSupport.fireChildAdded(parentPath, belowNodeIndex, selectedNode);
	}

	// Moshe's impl.
	public boolean canMoveUp(AbstractBeanTreeNode selectedNode) {
		AbstractBeanTreeNode parentNode = (AbstractBeanTreeNode) selectedNode.getParent();
		if (false == selectedNode.isLeaf() && null != parentNode) {
			int indexInParent = this.getIndexOfChild(parentNode, selectedNode);
			return (indexInParent > 0);
		}
		return false;
	}

	/*
	 * public void moveDown(BeanTreeNode selectedNode) { // TODO Auto-generated
	 * method stub
	 * 
	 * }
	 */

	public static BeanTreeTableModel createNewModel(Object root) {
		rootNode = new BeanRootNode(null, AbstractBeanTreeNode.NodeType.ROOT, root.getClass().getSimpleName(),
				root.getClass(), root);
		return new BeanTreeTableModel(rootNode);// rootNode);
	}

	public void setHasChanged(boolean hasChanged) {
		this.hasChanged = hasChanged;

	}

	public boolean getHasChanged() {
		return hasChanged;
	}

	// Moshe's impl.
	public boolean canMoveDown(AbstractBeanTreeNode selectedNode) {
		AbstractBeanTreeNode parentNode = (AbstractBeanTreeNode) selectedNode.getParent();
		if (false == selectedNode.isLeaf() && null != parentNode) {
			int indexInParent = this.getIndexOfChild(parentNode, selectedNode);
			return (indexInParent < (this.getChildCount(parentNode) - 1));
		}
		return false;
	}

	public void refresh() {
		modelSupport.fireNewRoot();
	}

	public void setChildToVisible(AbstractBeanTreeNode father, AbstractBeanTreeNode child) {
		int childIndex = -1;
		if ((childIndex = father.getHiddenChildren().indexOf(child)) < 0) {
			// Child is not exist in the hidden children list
			return;
		}
		father.add(child);
		father.hiddenChildren.remove(childIndex);
		modelSupport.fireChildAdded(new TreePath(father.getPath()), father.getChildCount() - 1, child);
	}

	private static AbstractBeanTreeNode[] getPathToRoot(AbstractBeanTreeNode aNode) {
		List<AbstractBeanTreeNode> path = new ArrayList<AbstractBeanTreeNode>();
		AbstractBeanTreeNode node = aNode;

		while (node != null) {
			path.add(0, node);
			node = (AbstractBeanTreeNode) node.getParent();
			// if (node.getParent() == null){
			// node = null;
			// }else {
			// }

		}
		return path.toArray(new AbstractBeanTreeNode[] {});
	}

	public CellEditorType getEditorType(JTable table, int row, int column) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getOptions(JTable table, int row, int column) {
		// TODO Auto-generated method stub
		return null;
	}

	// Moshe's impl.
	public Class<?> getCellType(JTable table, int row, int column) {
		/**
		 * TODO 4 - complete getCellType
		 **/
		AbstractBeanTreeNode node = (AbstractBeanTreeNode) ((JXTreeTable) table).getPathForRow(row)
				.getLastPathComponent();
		/*
		 * if (node.getType() != null) { return node.getType(); }
		 */
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

	public ParameterProvider getProvider(JTable table, int row, int column) {
		return null;
	}

}
