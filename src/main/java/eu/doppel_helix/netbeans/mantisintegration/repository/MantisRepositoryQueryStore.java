package eu.doppel_helix.netbeans.mantisintegration.repository;

import eu.doppel_helix.netbeans.mantisintegration.query.serialization.MantisQueryXml;
import eu.doppel_helix.netbeans.mantisintegration.query.MantisQuery;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

public class MantisRepositoryQueryStore implements FileChangeListener {
    private static final Logger LOG = Logger.getLogger(MantisRepositoryQueryStore.class.getName());
    private static final JAXBContext jaxbContext;
    private Map<String,WeakReference<MantisQuery>> openIssues = new HashMap<>();
    
    static {
        JAXBContext tempJaxbContext = null;
        try {
            tempJaxbContext = JAXBContext.newInstance(
                    "eu.doppel_helix.netbeans.mantisintegration.query.serialization",
                    MantisRepositoryQueryStore.class.getClassLoader());
        } catch (JAXBException ex) {
            LOG.log(Level.WARNING, "Failed to initialize MantisQuery saving", ex);
        }
        jaxbContext = tempJaxbContext;
    }
    
    private static final String QUERY_BASE_PATH = "Query";
    
    private final PropertyChangeSupport pcs;
    
    /**
     * MantisRepority instance this QueryStore handles queries for
     */
    private final MantisRepository mr;
    
    private final String queryStorageDir;
    
    private FileObject storage;
    
    private final Map<String,FileObject> backingFileMap = new HashMap<>();
    
    public MantisRepositoryQueryStore(MantisRepository mr, PropertyChangeSupport pcs) {
        this.mr = mr;
        this.pcs = pcs;
        queryStorageDir = String.format(
                "%s/%s",
                mr.getBaseConfigPath(),
                QUERY_BASE_PATH);
        storage = FileUtil.getConfigFile(queryStorageDir);
        initFileListener();
    }
    
    private synchronized FileObject getStorage() {
        if(storage == null) {
            try {
                FileObject root = FileUtil.getConfigRoot();
                //FileObject storageParent = root.getFileObject(queryStorageDir);
                storage = FileUtil.createFolder(new File(FileUtil.toFile(root), queryStorageDir));
                initFileListener();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return storage;
    }
    
    public void removeMantisQuery(String id) {
        FileObject fo = backingFileMap.get(id);
        if (fo != null) {
            try {
                fo.delete();
                openIssues.remove(id);
            } catch (IOException ex) {
                LOG.warning(String.format("Failed to delete: %s", fo.getPath()));
            }
        }
    }
    
    public synchronized MantisQuery getMantisQuery(String id) {
        MantisQuery cachedEntry = null;
        WeakReference<MantisQuery> ref = openIssues.get(id);
        if(ref != null) {
            cachedEntry = ref.get();
        }
        if(cachedEntry != null) {
            return cachedEntry;
        }
        MantisQuery result = null;
        if (backingFileMap.containsKey(id)) {
            FileObject fo = backingFileMap.get(id);
            if(fo.canRead()) {
                try (InputStream is = fo.getInputStream()) {
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    MantisQueryXml mqx = (MantisQueryXml) unmarshaller.unmarshal(is);
                    result = new MantisQuery(mr);
                    mqx.toMantisQuery(result);
                    result.setSaved(true);
                } catch (JAXBException ex) {
                    LOG.log(Level.WARNING, "Could not open file: {0}", fo);
                } catch (FileNotFoundException ex) {
                    LOG.log(Level.WARNING, "Could not open file: {0}", fo);
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Could not open file: {0}", fo);
                }
                
            }
        }
        return result;
    }

    public synchronized void saveMantisQuery(final MantisQuery mq, final boolean createIfNotExists) {
        openIssues.put(mq.getId(), new WeakReference<>(mq));
        try {
            getStorage().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                @Override
                public void run() throws IOException {
                    FileObject targetFile = backingFileMap.get(mq.getId());
                    if (targetFile == null) {
                        if(createIfNotExists) {
                            targetFile = storage.createData(mq.getId(), "xml");
                        } else {
                            return;
                        }
                    }
                    try (OutputStream os = targetFile.getOutputStream()) {
                        Marshaller marshaller = jaxbContext.createMarshaller();
                        MantisQueryXml mqx = new MantisQueryXml(mq);
                        marshaller.marshal(mqx, os);
                    } catch (JAXBException ex) {
                        LOG.log(Level.WARNING, "Failed to write query", ex);
                    }
                }
            });
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Failed to write query", ex);
        }
    }

    public List<MantisQuery> getQueries() {
        List<MantisQuery> queries = new ArrayList<>();
        for(String id: backingFileMap.keySet()) {
            MantisQuery mq = getMantisQuery(id);
            if(mq != null) {
                queries.add(mq);
            }
        }
        return queries;
    }
    
    private synchronized void initFileListener() {
        if (storage != null) {
            storage.removeFileChangeListener(this); // Make sure we are only connected once
            storage.addFileChangeListener(this);
            refreshBackingFileMap();
        }
    }
    
    private synchronized void refreshBackingFileMap() {
        if (storage != null) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = jaxbContext.createUnmarshaller();
            } catch (JAXBException ex) {
                throw new RuntimeException(ex);
            }
            for (FileObject fo : storage.getChildren()) {
                if (fo.isData() && fo.canRead()) {
                    try {
                        MantisQueryXml mqx = (MantisQueryXml) unmarshaller.unmarshal(
                                fo.getInputStream());
                        backingFileMap.put(mqx.getId(), fo);
                    } catch (JAXBException ex) {
                        // Ignore
                    } catch (FileNotFoundException ex) {
                        // Ignore - should not happen with the canRead check
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }
    
    @Override
    public void fileFolderCreated(FileEvent fe) {
}

    @Override
    public void fileDataCreated(FileEvent fe) {
        refreshBackingFileMap();
        pcs.firePropertyChange(RepositoryProvider.EVENT_QUERY_LIST_CHANGED, null,
                null);
    }

    @Override
    public void fileChanged(FileEvent fe) {
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        refreshBackingFileMap();
        pcs.firePropertyChange(RepositoryProvider.EVENT_QUERY_LIST_CHANGED, null,
                null);
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }
}
