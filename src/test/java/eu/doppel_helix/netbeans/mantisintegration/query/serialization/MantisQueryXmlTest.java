
package eu.doppel_helix.netbeans.mantisintegration.query.serialization;

import biz.futureware.mantisconnect.ObjectRef;
import eu.doppel_helix.netbeans.mantisintegration.query.MantisQuery;
import eu.doppel_helix.netbeans.mantisintegration.repository.MantisRepositoryQueryStore;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Date;
import javax.xml.bind.JAXBContext;
import static org.junit.Assert.assertEquals;
import org.junit.Test;


public class MantisQueryXmlTest {

    @Test
    public void testReadTestData() throws Exception {
        JAXBContext tempJaxbContext = JAXBContext.newInstance(
                    "eu.doppel_helix.netbeans.mantisintegration.query.serialization",
                    MantisRepositoryQueryStore.class.getClassLoader());

        try (InputStream is = MantisQueryXmlTest.class.getResourceAsStream("test.xml")) {
            MantisQueryXml xml = (MantisQueryXml) tempJaxbContext.createUnmarshaller().unmarshal(is);
            assertEquals(BigInteger.valueOf(42), xml.getAssignedTo().getId());
            assertEquals("Max", xml.getAssignedTo().getName());
            assertEquals("Max Mustermann", xml.getAssignedTo().getReal_name());
            assertEquals("max.mustermann@test.de", xml.getAssignedTo().getEmail());
            assertEquals("DemoCategory", xml.getCategory());
            assertEquals(MantisQuery.Combination.ALL, xml.getCombination());
            assertEquals("TestId", xml.getId());
            assertEquals("Dummy Query", xml.getName());
            assertEquals(BigInteger.valueOf(23), xml.getProjectId());
            assertEquals(new ObjectRef(BigInteger.ONE, "Prio 1"), xml.getPriority());
            assertEquals(new ObjectRef(BigInteger.valueOf(2), "All are happy"), xml.getResolution());
            assertEquals(new ObjectRef(BigInteger.valueOf(3), "Total loss"), xml.getSeverity());
            assertEquals(new ObjectRef(BigInteger.valueOf(4), "Clearing"), xml.getStatus());
            assertEquals(new ObjectRef(BigInteger.valueOf(5), "Viewing"), xml.getViewStatus());
            assertEquals("exception in runtime", xml.getSummaryFilter());
            assertEquals(BigInteger.TEN, xml.getServersideFilterId());
            assertEquals(BigInteger.valueOf(43), xml.getReporter().getId());
            assertEquals("Maxine", xml.getReporter().getName());
            assertEquals("Maxine Musterfrau", xml.getReporter().getReal_name());
            assertEquals("maxine.musterfrau@customer.de", xml.getReporter().getEmail());
            assertEquals(2018 - 1900, xml.getLastUpdateAfter().getYear());
            assertEquals(1 - 1, xml.getLastUpdateAfter().getMonth());
            assertEquals(1, xml.getLastUpdateAfter().getDate());
            assertEquals(2019 - 1900, xml.getLastUpdateBefore().getYear());
            assertEquals(1 - 1, xml.getLastUpdateBefore().getMonth());
            assertEquals(1, xml.getLastUpdateBefore().getDate());
        }
    }
}
