
package eu.doppel_helix.netbeans.mantisintegration.swing;

import biz.futureware.mantisconnect.ObjectRef;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisPriorityProvider;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;

public class PriorityListCellRenderer extends DefaultListCellRenderer {
    private final MantisPriorityProvider mpp = new MantisPriorityProvider();
    
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        ObjectRef or = null;
        if(value instanceof ObjectRef) {
            or = ((ObjectRef)value);
            value = or.getName();
        } else if (value == null) {
            value = " ";
        }
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if(or != null) {
            label.setIcon(new ImageIcon(mpp.getImageById(or.getId().toString())));
        } else {
            label.setIcon(null);
        }
        return label;
    }
    
}
