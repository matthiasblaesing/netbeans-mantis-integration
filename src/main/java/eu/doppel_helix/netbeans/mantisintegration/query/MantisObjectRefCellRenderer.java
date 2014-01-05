
package eu.doppel_helix.netbeans.mantisintegration.query;

import biz.futureware.mantisconnect.ObjectRef;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class MantisObjectRefCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if(value instanceof ObjectRef) {
            ObjectRef or = (ObjectRef) value;
            value = or.getName();
        }
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        return this;
    }
}
