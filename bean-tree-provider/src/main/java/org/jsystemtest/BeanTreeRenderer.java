/*
 * Copyright 2005-2010 Ignis Software Tools Ltd. All rights reserved.
 */
package org.jsystemtest;

import java.awt.Component;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;

import jsystem.treeui.images.ImageCenter;

/**
 * It used to draw the SUT planner tree. The tree node icons change
 * depend on the node type.
 * @author guy.arieli
 */
public class BeanTreeRenderer implements TreeCellRenderer {
	
	protected boolean bSelected = false;
	protected boolean bFocus = false;

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {

		String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row, hasFocus);

        //System.out.println(stringValue);


		JLabel label = new JLabel(stringValue);
        Component component = label;


		if (value instanceof AbstractBeanTreeNode) {
			AbstractBeanTreeNode node = (AbstractBeanTreeNode)value;
			String className = node.getClassName();

			if (className != null) {
				label.setToolTipText(className);
			}
			
			label.setText(node.toString());

			// Default icons in case there is an error
			if (leaf) {
				label.setIcon(UIManager.getIcon("Tree.leafIcon"));
			} else if (expanded) {
				label.setIcon(UIManager.getIcon("Tree.openIcon"));
			} else {
				label.setIcon(UIManager.getIcon("Tree.closedIcon"));
			}

			// Set a specific icon for each node type
			switch(node.getNodeType()) {
			case ROOT:
				label.setIcon(ImageCenter.getInstance().getImage(ImageCenter.ICON_SETUP));
				label.setToolTipText("System Under Test");
				break;
			case BEAN:
                if(node.getObjType() == Boolean.class) {
                    label.setIcon(ImageCenter.getInstance().getImage(ImageCenter.ICON_CLEAR));
                } else if(node.getObjType() == String.class) {
				    label.setIcon(ImageCenter.getInstance().getImage(ImageCenter.ICON_COMMANT));
                } else if( AbstractBeanTreeNode.isObjTypePrimitiveNumber(node.objType)) {
                    label.setIcon(ImageCenter.getInstance().getImage(ImageCenter.ICON_DEVICE));
                } else if(node.getObjType().isEnum()) {
                    label.setIcon(ImageCenter.getInstance().getImage(ImageCenter.ICON_TEST));
                } else {
                    label.setIcon(ImageCenter.getInstance().getImage(ImageCenter.ICON_PATH));
                }
				break;
			case PRIMITIVE:
				label.setIcon(ImageCenter.getInstance().getImage(ImageCenter.ICON_DEVICE));
				break;
            default:
                System.out.println("Renderer: Unknown Node Type");
            }


		}

		return component;
	}
	
	
}
