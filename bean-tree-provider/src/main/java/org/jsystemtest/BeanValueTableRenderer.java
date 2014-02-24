package org.jsystemtest;

import jsystem.treeui.images.ImageCenter;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * User: Moshe Goldyan (mgoldyan)
 * Date: 2/11/14
 * Time: 10:04 AM
 *
 *
 */
public class BeanValueTableRenderer implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        JLabel label = new JLabel("");
        Component component = label;

        Object pathComponent = ((JXTreeTable) table).getPathForRow(row).getLastPathComponent();
        if (pathComponent instanceof AbstractBeanTreeNode) {
            AbstractBeanTreeNode node = (AbstractBeanTreeNode)pathComponent;
            String className = node.getClassName();

            if (className != null) {
                label.setToolTipText(className);
            }

            // Set a specific icon for each node type
            switch(node.getNodeType()) {
                case ROOT:
                    //break;
                case BEAN:
                    if(node.getObjType() == Boolean.class) {
                        JCheckBox checkBox = new JCheckBox(value.toString());

                        boolean selectedCB = (Boolean)(node.parseObjectFromString(node.getUserObject().toString()));
                        checkBox.setSelected(selectedCB);
                        component = checkBox;
                    } else if(node.getObjType().isEnum()) {
                        JComboBox comboBox = new JComboBox();
                        String selectedItem = node.getUserObject().toString();
                        if(selectedItem != null) {
                            comboBox.addItem(selectedItem);
                            comboBox.setSelectedItem(selectedItem);
                        }
                        component = comboBox;
                    } else {
                        if(AbstractBeanTreeNode.isTypeNumberOrString(node.getObjType())) {
                            component = new JLabel(node.getValue().toString());
                        }
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