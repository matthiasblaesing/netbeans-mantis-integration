
package eu.doppel_helix.netbeans.mantisintegration.swing;

import biz.futureware.mantisconnect.ObjectRef;
import eu.doppel_helix.netbeans.mantisintegration.Mantis;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.math.BigInteger;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;

public class StatusListCellRenderer extends DefaultListCellRenderer {

    private final Map<BigInteger,Color> colorMap = Mantis.getInstance().getStatusColorMap();
    private final JPanel protectionPanel = new JPanel(new BorderLayout());

    private final DefaultListCellRenderer fallback = new DefaultListCellRenderer();

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Color foreground = null;
        Color background = null;

        if(value instanceof ObjectRef) {
            ObjectRef or = (ObjectRef)value;
            value = or.getName();
            BigInteger level = or.getId();
            foreground = colorMap.get(level);
            background = Color.BLACK;
        } else if (value == null) {
            return fallback.getListCellRendererComponent(list, " ", index, isSelected, cellHasFocus);
        }

        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        assert c != null;

        if(c instanceof JComponent) {
            ((JComponent)c).setOpaque(true);
        }
        c.setBackground(foreground);
        c.setForeground(background);

        protectionPanel.removeAll();
        protectionPanel.add(c);

        return protectionPanel;
    }
}
