package org.jsystemtest;

import java.util.ArrayList;

import jsystem.framework.sut.SutValidationError;

import javax.swing.tree.DefaultMutableTreeNode;

public class BeanTreeNode extends DefaultMutableTreeNode {
	
	public enum NodeType {
		ROOT, MAIN_SO, EXTENTION_SO, EXTENTION_ARRAY_SO, SUB_SO, ARRAY_SO, TAG, OPTIONAL_TAG
	}
	
	/**
	 * Type of the node.
	 */
	private NodeType type;
    private BeanTreeNode parentNode;
    private String name;
    private String className;


    public BeanTreeNode(NodeType type, String name, Object obj) {

        this.type = type;
        this.name = name;
    }


	public boolean isLeaf() {
        return (type == NodeType.EXTENTION_SO
                || type == NodeType.EXTENTION_ARRAY_SO || type == NodeType.TAG || type == NodeType.OPTIONAL_TAG);
	}

	public BeanTreeNode getParent() {
		return parentNode;
	}

	public Object getName() {
		return name;
	}

	public NodeType getType() {
		return type;
	}

	public void getValidationErrors(ArrayList<SutValidationError> nodeErrors) {
		// TODO Auto-generated method stub
		
	}

	public String getClassName() {
		return className;
	}

	public String getArraySuperClassName() {
		// TODO Auto-generated method stub
		return null;
	}

}
