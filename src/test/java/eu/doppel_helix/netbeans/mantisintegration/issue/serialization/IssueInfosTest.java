
package eu.doppel_helix.netbeans.mantisintegration.issue.serialization;

import java.io.InputStream;
import java.math.BigInteger;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.xml.bind.JAXBContext;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class IssueInfosTest {

    @Test
    public void testReadTestData() throws Exception {
        JAXBContext tempJaxbContext = JAXBContext.newInstance(
            "eu.doppel_helix.netbeans.mantisintegration.issue.serialization",
            IssueInfos.class.getClassLoader());

        try (InputStream is = IssueInfosTest.class.getResourceAsStream("test.xml")) {
            IssueInfos iis = (IssueInfos) tempJaxbContext.createUnmarshaller().unmarshal(is);
            IssueInfo ii = iis.getIssueInfo(BigInteger.valueOf(1));
            assertEquals(2019 - 1900, ii.getScheduleDate().getYear());
            assertEquals(12 - 1, ii.getScheduleDate().getMonth());
            assertEquals(12, ii.getScheduleDate().getDate());
            Date readState = ii.getReadState();
            GregorianCalendar readStateCal = new GregorianCalendar();
            readStateCal.setTime(readState);
            readStateCal.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
            assertEquals(2019, readStateCal.get(Calendar.YEAR));
            assertEquals(11, readStateCal.get(Calendar.MONTH));
            assertEquals(1, readStateCal.get(Calendar.DAY_OF_MONTH));
            assertEquals(7, readStateCal.get(Calendar.HOUR_OF_DAY));
            assertEquals(400, ii.getScheduleLength());
        }
    }

}
