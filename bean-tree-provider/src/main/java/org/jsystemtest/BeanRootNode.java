package org.jsystemtest;

import javax.swing.tree.MutableTreeNode;

public class BeanRootNode extends AbstractBeanTreeNode {

	/**
     * User: Moshe Goldyan (mgoldyan)
     * Concrete class of AbstractBeanTreeNode, used for distinction from
	 */
	private static final long serialVersionUID = 1L;

	public BeanRootNode(MutableTreeNode parent, NodeType type, String name, Class<?> objType ,Object obj) {
		super(parent, type, name, objType, obj, null, null);
	}

}
