
package eu.doppel_helix.netbeans.mantisintegration.swing;

import biz.futureware.mantisconnect.ObjectRef;
import java.awt.Component;
import java.awt.Font;
import java.math.BigInteger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JSeparator;

public class ObjectRefListCellRenderer extends DefaultListCellRenderer {
    private final JSeparator seperator = new JSeparator(JSeparator.HORIZONTAL);

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        boolean negativeValue = false;
        if(value instanceof ObjectRef) {
            ObjectRef or = (ObjectRef) value;
            if(or.getId().equals(BigInteger.ZERO)) {
                return seperator;
            } else if(or.getId().compareTo(BigInteger.ZERO) < 0) {
                negativeValue = true;
            }
            value = or.getName();

        } else if (value == null) {
            value = " ";
        }
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if(negativeValue) {
            c.setFont(c.getFont().deriveFont(Font.ITALIC));
        } else {
            c.setFont(c.getFont().deriveFont(Font.PLAIN));
        }
        return c;
    }

}
