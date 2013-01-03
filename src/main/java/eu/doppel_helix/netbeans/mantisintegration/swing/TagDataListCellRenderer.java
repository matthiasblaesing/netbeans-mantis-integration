
package eu.doppel_helix.netbeans.mantisintegration.swing;

import biz.futureware.mantisconnect.TagData;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class TagDataListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if(value instanceof TagData) {
            value = ((TagData)value).getName();
        } else if (value == null) {
            value = " ";
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
    
}
