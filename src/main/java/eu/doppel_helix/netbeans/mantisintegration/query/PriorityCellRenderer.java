
package eu.doppel_helix.netbeans.mantisintegration.query;

import biz.futureware.mantisconnect.ObjectRef;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisPriorityProvider;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class PriorityCellRenderer extends DefaultTableCellRenderer {
    private final MantisPriorityProvider mpp = new MantisPriorityProvider();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String id = null;
        if(value instanceof ObjectRef) {
            ObjectRef or = (ObjectRef) value;
            value = or.getName();
            id = or.getId().toString();
        }
        JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(id != null) {
            renderer.setIcon(new ImageIcon(mpp.getImageById(id)));
        } else {
            renderer.setIcon(null);
        }
        return renderer;
    }
}
