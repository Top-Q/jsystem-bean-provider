package org.jsystemtest;

import java.util.ArrayList;

import jsystem.framework.sut.SutValidationError;

public class BeanTreeNode {
	
	public enum NodeType {
		ROOT, MAIN_SO, EXTENTION_SO, EXTENTION_ARRAY_SO, SUB_SO, ARRAY_SO, TAG, OPTIONAL_TAG
	}
	
	/**
	 * Type of the node.
	 */
	private NodeType type;

	public boolean isLeaf() {
		// TODO Auto-generated method stub
		return false;
	}

	public BeanTreeNode getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public NodeType getType() {
		return type;
	}

	public void getValidationErrors(ArrayList<SutValidationError> nodeErrors) {
		// TODO Auto-generated method stub
		
	}

	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getArraySuperClassName() {
		// TODO Auto-generated method stub
		return null;
	}

}
