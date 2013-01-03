
package eu.doppel_helix.netbeans.mantisintegration.swing;

import biz.futureware.mantisconnect.ObjectRef;
import eu.doppel_helix.netbeans.mantisintegration.Mantis;
import java.awt.Color;
import java.awt.Component;
import java.math.BigInteger;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;

public class StatusListCellRenderer extends DefaultListCellRenderer {
    private Map<BigInteger,Color> colorMap = Mantis.getInstance().getStatusColorMap();
    
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Color color = null;
        
        if(value instanceof ObjectRef) {
            ObjectRef or = (ObjectRef)value;
            value = or.getName();
            BigInteger level = or.getId();
            color = colorMap.get(level);
        } else if (value == null) {
            value = " ";
        }
        
        if(color == null) {
            color = Color.WHITE;
        }
        
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        if(c instanceof JComponent) {
            ((JComponent)c).setOpaque(true);
        }
        c.setBackground(color);
        
        return c;
    }
    
}
