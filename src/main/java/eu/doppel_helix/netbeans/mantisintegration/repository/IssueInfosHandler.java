package eu.doppel_helix.netbeans.mantisintegration.repository;

import eu.doppel_helix.netbeans.mantisintegration.issue.serialization.IssueInfo;
import eu.doppel_helix.netbeans.mantisintegration.issue.serialization.IssueInfos;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor.Task;

public class IssueInfosHandler {

    private static final Logger LOG = Logger.getLogger(IssueInfosHandler.class.getName());

    private static JAXBContext ctx;

    static {
        try {
            ctx = JAXBContext.newInstance(
                    "eu.doppel_helix.netbeans.mantisintegration.issue.serialization",
                    IssueInfosHandler.class.getClassLoader());
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    private final IssueInfos infos;
    private final FileObject storage;
    private final Task SAVER;

    public IssueInfosHandler(MantisRepository mr) {
        storage = mr.getBaseConfigFileObject();
        FileObject dataFile = storage.getFileObject("issueInfos.xml");
        IssueInfos tempInfos;
        if (dataFile != null) {
            try {
                tempInfos = (IssueInfos) ctx.createUnmarshaller().unmarshal(dataFile.getInputStream());
            } catch (JAXBException | FileNotFoundException ex) {
                LOG.log(Level.WARNING, "Failed to read IssueInfos file, please report this as a bug", ex);
                tempInfos = new IssueInfos();
            }
        } else {
            tempInfos = new IssueInfos();
        }
        infos = tempInfos;
        SAVER = mr.getRequestProcessor().create(new Runnable() {
            @Override
            public void run() {
                FileObject dataFile = storage.getFileObject("issueInfos.xml.new");
                try {
                    if (dataFile == null) {
                        dataFile = storage.createData("issueInfos.xml.new");
                    }
                    try (OutputStream os = dataFile.getOutputStream()) {
                        ctx.createMarshaller().marshal(infos, os);
                    }
                    File baseDir = FileUtil.toFile(storage);
                    Files.move(
                            new File(baseDir, "issueInfos.xml.new").toPath(),
                            new File(baseDir, "issueInfos.xml").toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (JAXBException | IOException ex) {
                    LOG.log(Level.WARNING, "Failed to write IssueInfos file, please report this as a bug", ex);
                }
            }
        });
    }

    public IssueInfo getIssueInfo(BigInteger id) {
        IssueInfo ii = infos.getIssueInfo(id);
        if (ii == null) {
            return null;
        }
        return (IssueInfo) (ii.clone());
    }

    public void putIssueInfo(IssueInfo i) {
        infos.putIssueInfo(i);
        SAVER.schedule(1000);
    }
}
