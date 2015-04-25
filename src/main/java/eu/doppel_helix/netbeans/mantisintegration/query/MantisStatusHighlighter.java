package eu.doppel_helix.netbeans.mantisintegration.query;

import biz.futureware.mantisconnect.ObjectRef;
import eu.doppel_helix.netbeans.mantisintegration.Mantis;
import java.awt.Color;
import java.awt.Component;
import java.math.BigInteger;
import java.util.Map;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;

public class MantisStatusHighlighter extends AbstractHighlighter {

    private final Map<BigInteger, Color> colorMap = Mantis.getInstance().getStatusColorMap();

    @Override
    protected Component doHighlight(Component cmpnt, ComponentAdapter ca) {
        Object value = ca.getValue();
        if (value instanceof ObjectRef) {
            Color color = colorMap.get(((ObjectRef) value).getId());
            if (color != null) {
                cmpnt.setBackground(color);
                cmpnt.setForeground(Color.BLACK);
            }
        }
        return cmpnt;
    }

}
