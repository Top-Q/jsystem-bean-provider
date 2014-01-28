package org.jsystemtest;

import javax.swing.tree.MutableTreeNode;

public class BeanObjectTreeNode extends AbstractBeanTreeNode {


    public BeanObjectTreeNode(MutableTreeNode parent, String name, Class<?> objType, Object obj, Object defaultValue) {
        super(parent, NodeType.BEAN, name, objType, obj, defaultValue);
    }

    /*public BeanObjectTreeNode(MutableTreeNode parent, NodeType type, String name, Class<?> objType, Object obj) {
        super(parent, type, name, objType, obj);
    }*/
}
