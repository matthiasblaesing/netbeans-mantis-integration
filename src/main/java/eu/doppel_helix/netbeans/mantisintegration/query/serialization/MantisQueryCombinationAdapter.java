
package eu.doppel_helix.netbeans.mantisintegration.query.serialization;

import eu.doppel_helix.netbeans.mantisintegration.query.MantisQuery;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class MantisQueryCombinationAdapter extends XmlAdapter<String, MantisQuery.Combination>{

    @Override
    public MantisQuery.Combination unmarshal(String v) throws Exception {
        try {
            return MantisQuery.Combination.valueOf(v);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public String marshal(MantisQuery.Combination v) throws Exception {
        return v.name();
    }

}
