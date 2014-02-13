package org.jsystemtest;

import jsystem.treeui.utilities.CellEditorModel;
import jsystem.utils.beans.CellEditorType;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: mgoldyan
 * Date: 2/12/14
 * Time: 10:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class ComplexCellEditor extends AbstractCellEditor implements TableCellEditor {
    /**
     *
     */
    private static final long serialVersionUID = -4425540069459297907L;

    private CellEditorType currentType;
    private Component component = null;
    private int row,column;
    private JTable table;


    private CellEditorModel model;
    public ComplexCellEditor(CellEditorModel model){
        this.model = model;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {

        component = new JTextField("");

        if(column == BeanTreeTableModel.ColNames.CUR_VAL.ordinal()) {
            Object pathComponent = ((JXTreeTable) table).getPathForRow(row).getLastPathComponent();

            if (pathComponent instanceof AbstractBeanTreeNode) {
                AbstractBeanTreeNode node = (AbstractBeanTreeNode)pathComponent;
                String className = node.getClassName();

                // Set a specific icon for each node type
                switch(node.getNodeType()) {
                    case ROOT:
                        break;
                    case BEAN:
                        if(node.getObjType() == Boolean.class) {
                            JCheckBox checkBox = new JCheckBox(value.toString());
                            boolean selectedCB = (Boolean)(node.parseObjectFromString(node.getUserObject().toString()));
                            checkBox.setSelected(selectedCB);
                            checkBox.setText("Originally: " + String.valueOf(selectedCB));
                            component = checkBox;
                        } else if(node.getObjType().isEnum()) {
                            JComboBox comboBox = new JComboBox(node.getObjType().getEnumConstants());
                            comboBox.setSelectedItem(node.getUserObject().toString());
                            component = comboBox;
                        } else {
                            if(AbstractBeanTreeNode.isTypeNumberOrString(node.getObjType())) {
                                String content = "";
                                if (value != null) {
                                    content = value.toString();
                                }
                                component = new JTextField(content);
                            }
                        }
                    break;
                }
            }
        }

        return component;
    }

    /**
     * This method override the method from AbstractCellEditor, and allow us
     * to check user input value before it is written into the table
     * If the value is invalid, we give a compatible error message, and move the user back to
     * the last edited cell.
     */
    public boolean stopCellEditing() {
        return super.stopCellEditing();
    }

    @Override
    public Object getCellEditorValue() {

        if(component instanceof JTextField) {
            return ((JTextField)component).getText();
        } else if(component instanceof JComboBox) {
            return ((JComboBox)component).getSelectedItem();
        } else if(component instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox) component;
            checkBox.setText(String.valueOf(checkBox.isSelected()));
            return ((JCheckBox)component).isSelected();
        }

        return component;
    }
}
