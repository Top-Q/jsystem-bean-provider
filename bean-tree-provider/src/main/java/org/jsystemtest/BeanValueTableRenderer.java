package org.jsystemtest;

import jsystem.treeui.images.ImageCenter;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: mgoldyan
 * Date: 2/11/14
 * Time: 10:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class BeanValueTableRenderer implements TableCellRenderer {

    //private static int currValueColumn = BeanTreeTableModel.ColNames.CUR_VAL.ordinal();

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

            //label.setText(node.toString());

            // Set a specific icon for each node type
            switch(node.getNodeType()) {
                case ROOT:
                    break;
                case BEAN:
                    if(node.getObjType() == Boolean.class) {
                        JCheckBox checkBox = new JCheckBox(value.toString());

                        boolean selectedCB = (Boolean)(node.parseObjectFromString(node.getUserObject().toString()));
                        checkBox.setSelected(selectedCB);
                        component = checkBox;
                    } else if(node.getObjType().isEnum()) {

                        /*Object fieldsObj = node.getUserObject();
                        java.util.List<String> consts = new ArrayList<String>();
                        for(int i = 0; i < Array.getLength(fieldsObj); i++) {
                            Field field = (Field) Array.get(fieldsObj, i);
                            consts.add(field.getName());
                        }

                        JComboBox comboBox = new JComboBox(consts.toArray());//((Object[])(node.getUserObject())));*/
                        /*Field[] enumFields = node.getObjType().getDeclaredFields();
                        java.util.List<String> consts = new ArrayList<String>();
                        for(int i = 0; i < enumFields.length; i++) {
                            Field field = enumFields[i];
                            if(field.isEnumConstant()) {
                                consts.add(field.getName());
                            }
                        }
                        JComboBox comboBox = new JComboBox(consts.toArray());
                        comboBox.setEnabled(true);
                        comboBox.setRenderer(new ComboBoxRenderer());
                        comboBox.setEditable(true);
//                        comboBox.addActionListener(new ActionListener() {
//                            public void actionPerformed(ActionEvent e) {
//                                System.out.println("You chose " + e.getActionCommand() + " for " + ((JComboBox) e.getSource()).getSelectedItem() + "!");
//
//                            }
//                        });
//                        comboBox.setActionCommand("Hello");*/

                        //((JXTreeTable) table).getColumn(1).setCellEditor(new DefaultCellEditor(comboBox));
                        JComboBox comboBox = new JComboBox();
                        String selectedItem = node.getUserObject().toString();
                        if(selectedItem != null) {
                            comboBox.addItem(selectedItem);
                            comboBox.setSelectedItem(selectedItem);
                        }
                        component = comboBox;
                        //component = new JLabel(node.getUserObject().toString());

                    } else {
                        if(AbstractBeanTreeNode.isTypePrimitiveOrString(node.getObjType())) {
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