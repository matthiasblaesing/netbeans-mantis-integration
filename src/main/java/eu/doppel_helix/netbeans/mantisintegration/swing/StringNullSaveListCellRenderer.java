
package eu.doppel_helix.netbeans.mantisintegration.swing;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class StringNullSaveListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
            value = " ";
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
