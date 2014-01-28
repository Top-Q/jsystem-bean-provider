package org.jsystemtest;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: mgoldyan
 * Date: 1/28/14
 * Time: 1:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setSelected(((Boolean)value).booleanValue());
        return this;
    }
}
