package org.jsystemtest;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import jsystem.framework.sut.SutValidationError;

public abstract class AbstractBeanTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 1L;

	public enum NodeType {
		ROOT, BEAN, PRIMITIVE
	}

	/**
	 * Type of the node.
	 */
	private NodeType type;
//	private AbstractBeanTreeNode parentNode;
	private String name;
	protected Vector<AbstractBeanTreeNode> hiddenChildren;

	public AbstractBeanTreeNode(MutableTreeNode parent, NodeType type, String name, Class<?> objType, Object userObject) {
		super(userObject);
		this.type = type;
		this.name = name;
		this.parent = parent;
		initChildren();
	}

    @SuppressWarnings("unchecked")
	protected void initChildren() {
		if (null == children) {
			children = new Vector<AbstractBeanTreeNode>();
		}
		if (null == hiddenChildren){
			hiddenChildren = new Vector<AbstractBeanTreeNode>();
		}

		if (null == userObject) {
			return;
		}
		Method[] methods = userObject.getClass().getMethods();
		for (Method method : methods) {
			if ((method.getName().startsWith("get") || method.getName().startsWith("is"))
					&& method.getParameterTypes().length == 0) {
				if (method.getName().equals("getClass")) {
					continue;
				}
				if (userObject.getClass().getName().equals("java.lang.String")) {
					if (method.getName().equals("isEmpty") || method.getName().equals("getBytes")) {
						continue;
					}
				}
				try {
					Class<?> type = method.getReturnType();
					Object childObj = method.invoke(userObject, new Object[] {});
					Object newInstance = userObject.getClass().newInstance();
					//TODO : Add factory
					final AbstractBeanTreeNode node = new BeanObjectTreeNode(this, NodeType.BEAN, method.getName()
							.replaceFirst("get", ""), type, childObj);

					if (childObj.equals(method.invoke(newInstance, new Object[] {}))) {
						//It is important to set the parent to null unless we want null pointer exception later on
						node.setParent(null);
						hiddenChildren.add(node);
					}else {
						children.add(node);
						
					}
				} catch (Exception e) {
					System.out.println("Failed to invoke method " + method.getName());
				}
			}
		}

	}

	public String toString() {
		return name;
	}
	
	public boolean isLeaf() {
		// return (type == NodeType.PRIMITIVE
		// || type == NodeType.EXTENTION_ARRAY_SO || type == NodeType.TAG ||
		// type == NodeType.OPTIONAL_TAG);
		return false;
	}

	private static boolean isBean(Type type) {
		if (type.toString().contains("java.lang.String")) {
			return false;
		}
		if (type.toString().contains("int")) {
			return false;
		}
        if (type.toString().contains("boolean")) {
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
		if (type.toString().contains("char")) {
			return false;
		}

		return true;

	}

//	public AbstractBeanTreeNode getParent() {
//		return parent;
//	}
	
	public Object getName() {
		return name;
	}

	public NodeType getType() {
		return type;
	}

	public void getValidationErrors(ArrayList<SutValidationError> nodeErrors) {
		// TODO Auto-generated method stub

	}

	public Object getValue() {
		if (userObject != null) {
			return userObject.toString();
		}
		return "";
	}

	public String getClassName() {
		if (userObject != null) {
			return userObject.getClass().getSimpleName();
		}
		return "";
	}

	public String getArraySuperClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<AbstractBeanTreeNode> getHiddenChildren() {
		return hiddenChildren;
	}
	
	

}
