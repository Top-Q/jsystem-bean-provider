package org.jsystemtest;

import javax.swing.tree.MutableTreeNode;

public class BeanRootNode extends AbstractBeanTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BeanRootNode(MutableTreeNode parent, NodeType type, String name, Class<?> objType ,Object obj) {
		super(parent, type, name, objType, obj, null);
	}

}
