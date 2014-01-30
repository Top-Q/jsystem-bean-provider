package org.jsystemtest;

import javax.swing.tree.MutableTreeNode;

public class BeanObjectTreeNode extends AbstractBeanTreeNode {


    public BeanObjectTreeNode(MutableTreeNode parent, String name, Class<?> objType, Object obj, Object defaultValue, String getMethodInParent) {
        super(parent, NodeType.BEAN, name, objType, obj, defaultValue, getMethodInParent);
    }

    /*public BeanObjectTreeNode(MutableTreeNode parent, NodeType type, String name, Class<?> objType, Object obj) {
        super(parent, type, name, objType, obj);
    }*/
}
