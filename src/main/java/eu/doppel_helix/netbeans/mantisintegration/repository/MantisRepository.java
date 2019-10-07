package eu.doppel_helix.netbeans.mantisintegration.repository;

import biz.futureware.mantisconnect.AttachmentData;
import biz.futureware.mantisconnect.FilterSearchData;
import biz.futureware.mantisconnect.IssueData;
import biz.futureware.mantisconnect.IssueHeaderData;
import biz.futureware.mantisconnect.IssueNoteData;
import biz.futureware.mantisconnect.MantisConnectLocator;
import biz.futureware.mantisconnect.MantisConnectPortType;
import biz.futureware.mantisconnect.ObjectRef;
import biz.futureware.mantisconnect.ProjectData;
import biz.futureware.mantisconnect.RelationshipData;
import biz.futureware.mantisconnect.TagData;
import biz.futureware.mantisconnect.UserData;
import eu.doppel_helix.netbeans.mantisintegration.MantisConnector;
import eu.doppel_helix.netbeans.mantisintegration.data.Version;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import eu.doppel_helix.netbeans.mantisintegration.query.MantisQuery;
import eu.doppel_helix.netbeans.mantisintegration.swing.EDTInvocationHandler;
import eu.doppel_helix.netbeans.mantisintegration.util.ExceptionHandler;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.xml.rpc.ServiceException;
import org.apache.axis.client.Call;
import org.apache.axis.client.Stub;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.InstanceContent;

public class MantisRepository {
    public static final String PROP_SCHEDULE_DATE_FIELD = "MantisRepository.scheduleDateField";
    public static final String PROP_SCHEDULE_LENGTH_FIELD = "MantisRepository.scheduleLengthField";
    public static final String PROP_COMMIT_RESOLUTION_FIELD = "MantisRepository.commitResolution";
    public static final String PROP_COMMIT_STATUS_FIELD = "MantisRepository.commitStatus";
    private static final String BASE_CONFIG_PATH = "MantisIntegration"; // NOI18N
    private static final Logger logger = Logger.getLogger(MantisRepository.class.getName());
    private static final Image ICON = ImageUtilities.loadImage(
            "eu/doppel_helix/netbeans/mantisintegration/icon.png");

    private final transient InstanceContent ic;
    private final transient Capabilities capabilities = new Capabilities(this);

    private final IssueCache cache = new IssueCache();
    private final RequestProcessor requestProzessor = new RequestProcessor(MantisRepository.class);
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private RepositoryInfo info;
    private MantisRepositoryController controller;
    private MantisConnectPortType client;
    private Version version;

    private IssueInfosHandler issueInfosHandler;
    private UserData account = null;
    private MantisRepositoryQueryStore queryStore;
    private ExceptionHandler exceptionHandler;

    private final transient MasterData masterData = new MasterData(this);

    String getBaseConfigPath() {
        return String.format("%s/%s", BASE_CONFIG_PATH, getInfo().getID());
    }

    FileObject getBaseConfigFileObject() {
        FileObject root = FileUtil.getConfigRoot();
        try {
            return FileUtil.createFolder(root, getBaseConfigPath());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public synchronized IssueInfosHandler getIssueInfosHandler() {
        if(issueInfosHandler == null) {
            issueInfosHandler = new IssueInfosHandler(this);
        }
        return issueInfosHandler;
    }

    private static MantisConnectPortType initClient(String baseUrl, String httpUsername, String httpPassword) throws ServiceException, MalformedURLException {
        ClassLoader origLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(MantisRepository.class.getClassLoader());
        MantisConnectPortType result = null;
        try {
            if (!baseUrl.endsWith("/api/soap/mantisconnect.php")) {
                baseUrl += "/api/soap/mantisconnect.php";
            }

            MantisConnectLocator mcl = new MantisConnectLocator();

            result = mcl.getMantisConnectPort(new URL(baseUrl));

            if(httpUsername != null && (!httpUsername.isEmpty())
                    && httpPassword != null && (!httpPassword.isEmpty())) {
                // enable Basic HTTP Authentication:
                ((Stub) result)._setProperty(Call.USERNAME_PROPERTY, httpUsername);
                ((Stub) result)._setProperty(Call.PASSWORD_PROPERTY, httpPassword);
            }

            if (MantisRepository.class.desiredAssertionStatus()) {
                EDTInvocationHandler invocationHandler = new EDTInvocationHandler(result);
                result = (MantisConnectPortType) Proxy.newProxyInstance(
                        MantisRepository.class.getClassLoader(),
                        new Class[]{MantisConnectPortType.class},
                        invocationHandler);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(origLoader);
        }
        return result;
    }

    static ConnectionCheckResult checkConnection(String url, String username, String password, String httpUser, String httpPassword) throws ServiceException, RemoteException {
        try {
            ConnectionCheckResult ccr = new ConnectionCheckResult();
            MantisConnectPortType mcpt = initClient(url, httpUser, httpPassword);
            // Test Authentication information
            mcpt.mc_projects_get_user_accessible(username, password);

            ccr.setVersion(new Version(mcpt.mc_version()));
            ccr.setResolutionList(mcpt.mc_enum_resolutions(username, password));
            ccr.setStatusList(mcpt.mc_enum_status(username, password));

            return ccr;
        } catch (MalformedURLException ex) {
            throw new ServiceException("Broken client url:" + url, ex);
        }
    }

    public MantisRepository() {
        ic = new InstanceContent();
        init(null);
    }

    public MantisRepository(RepositoryInfo ri) {
        this();
        init(ri);
    }

    private void init(RepositoryInfo ri) {
        if(ri == null) {
            ri = new RepositoryInfo(
                MantisConnector.ID + System.currentTimeMillis(),
                MantisConnector.ID,
                "http://<host>/<mantis-basepath>",
                "",
                "");
        }
        setInfo(ri);
        queryStore = new MantisRepositoryQueryStore(this, pcs);
    }

    public RequestProcessor getRequestProcessor() {
        return requestProzessor;
    }

    private IssueCache getIssueCache() {
        return cache;
    }

    public Image getIcon() {
        return ICON;
    }

    public void remove() {
        // Done
    }

    public MantisRepositoryController getController() {
        if (controller == null) {
            controller = new MantisRepositoryController(this);
        }
        return controller;
    }

    public RepositoryInfo getInfo() {
        return info;
    }

    public void setInfo(RepositoryInfo info) {
        this.info = info;
        client = null;
    }

    public MasterData getMasterData() {
        return masterData;
    }

    public List<MantisIssue> getIssues(boolean onlyCached, String... issues) throws ServiceException, RemoteException {
        BigInteger[] issueIds = new BigInteger[issues.length];
        for(int i = 0; i < issues.length; i++) {
            issueIds[i] = new BigInteger(issues[i]);
        }
        return getIssues(onlyCached, issueIds);
    }

    public List<MantisIssue> getIssues(boolean onlyCached, BigInteger... issues) throws ServiceException, RemoteException {
        List<MantisIssue> results = new ArrayList<>();
        for (int i = 0; i < issues.length; i++) {
            MantisIssue issue = getIssueCache().getIssue(issues[i]);
            if (issue == null && (! onlyCached)) {
                issue = new MantisIssue(this);
                issue.setId(issues[i]);
                issue.refresh();
            }
            if(issue != null) {
                results.add(issue);
            }
        }
        return results;
    }

    public Collection<MantisIssue> simpleSearch(String criteria) throws ServiceException, RemoteException {
        int pageSize = 1000;
        List<MantisIssue> result = new ArrayList<>();

        try {
            BigInteger possibleId = new BigInteger(criteria);
            List<MantisIssue> issues = getIssues(false, possibleId.toString());
            result.addAll(issues);
        } catch (NumberFormatException ex) {
        }

        if (result.isEmpty()) {
            Pattern p = null;
            try {
                p = Pattern.compile(criteria);
            } catch (PatternSyntaxException ex) {
            }

            MantisConnectPortType mcpt = getClient();
            List<BigInteger> matchingIds = new ArrayList<>();
            OUTER:
            for (ProjectData project : masterData.getProjects()) {
                for (int i = 0; i < 1000; i++) {
                    IssueHeaderData[] ids = mcpt.mc_project_get_issue_headers(
                            info.getUsername(),
                            new String(info.getPassword()),
                            project.getId(),
                            BigInteger.valueOf(i),
                            BigInteger.valueOf(pageSize));
                    for (IssueHeaderData id : ids) {
                        if (id.getSummary().contains(criteria)
                                || (p != null && p.matcher(id.getSummary()).find())) {
                            matchingIds.add(id.getId());
                            if (result.size() > 250) {
                                break OUTER;
                            }
                        }
                    }
                    if (ids.length < pageSize) {
                        break;
                    }
                }
            }
            result.addAll(getIssues(false, matchingIds.toArray(new BigInteger[0])));
        }

        return result;
    }

    public MantisQuery createQuery() {
        return new MantisQuery(this);
    }

    public MantisQuery getQuery(String id) {
        return queryStore.getMantisQuery(id);
    }

    public void saveQuery(MantisQuery mq, boolean createIfNotExists) {
        queryStore.saveMantisQuery(mq, createIfNotExists);
    }

    public MantisIssue createIssue() {
        return new MantisIssue(this);
    }

    public Collection<MantisQuery> getQueries() {
        return queryStore.getQueries();
    }

    public void deleteQuery(String id) {
        queryStore.removeMantisQuery(id);
    }

    public Version getVersion() throws ServiceException, RemoteException {
        if (version == null) {
            String versionString = getClient().mc_version();
            version = new Version(versionString);
        }
        return version;
    }

    public URI getIssueUrl(MantisIssue issue) {
        try {
            if (issue.getId() != null) {
                return new URI(getBaseUrl() + "/view.php?id=" + issue.getIdAsString());
            } else {
                return new URI(getBaseUrl());
            }
        } catch (URISyntaxException ex) {
            logger.log(Level.WARNING, "Failed to create issue url", ex);
            return null;
        }
    }

    protected String getBaseUrl() {
        String baseUrl = info.getUrl();
        return cleanUpUrl(baseUrl);
    }

    private static String cleanUpUrl(String baseUrl) {
        if (baseUrl.endsWith("/api/soap/mantisconnect.php")) {
            baseUrl = baseUrl.replaceAll("/api/soap/mantisconnect.php$", "");
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    /**
     * Note! Only present in mantis 1.2.12 or later
     *
     * @return
     * @throws ServiceException
     * @throws RemoteException
     */
    public UserData getAccount() throws ServiceException, RemoteException {
        if (account == null) {
            account = getClient().mc_login(info.getUsername(),
                    new String(info.getPassword()));
        }
        return account;
    }

    protected synchronized MantisConnectPortType getClient() throws ServiceException {
        if (client == null) {
            String baseUrl = getBaseUrl();
            try {
                client = initClient(baseUrl, info.getHttpUsername(), new String(info.getHttpPassword()));
            } catch (MalformedURLException ex) {
                throw new ServiceException("Broken client url:" + baseUrl, ex);
            }
        }
        return client;
    }

    public boolean updateIssueFromRepository(final MantisIssue issue) throws ServiceException, RemoteException {
        try {
            if (issue.getId() == null) {
                return false;
            }
            MantisConnectPortType mcpt = getClient();
            IssueData id = mcpt.mc_issue_get(
                    info.getUsername(),
                    new String(info.getPassword()),
                    issue.getId());
            issue.updateFromIssueData(id);
            getIssueCache().setIssueData(id.getId(), issue);
            return true;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void addIssue(final MantisIssue issue, IssueData newData)
            throws ServiceException, RemoteException {
        if (issue.getId() != null) {
            this.updateIssue(issue, newData);
            return;
        }
        MantisConnectPortType mcpt = getClient();

        BigInteger id = mcpt.mc_issue_add(
                info.getUsername(),
                new String(info.getPassword()),
                newData);
        issue.setId(id);
        issue.refresh();
    }

    public void updateIssue(final MantisIssue issue, IssueData newData)
            throws ServiceException, RemoteException {
        if (issue.getId() == null) {
            addIssue(issue, newData);
            return;
        }
        MantisConnectPortType mcpt = getClient();
        mcpt.mc_issue_update(
                info.getUsername(),
                new String(info.getPassword()),
                issue.getId(),
                newData);
        issue.refresh();
    }

    public void addComment(MantisIssue issue, String comment, ObjectRef viewState, BigInteger timetracking)
            throws ServiceException, RemoteException {
        MantisConnectPortType mcpt = getClient();
        IssueNoteData ind = new IssueNoteData();
        ind.setText(comment);
        ind.setView_state(viewState);
        if(timetracking == null) {
            ind.setTime_tracking(BigInteger.ZERO);
        } else {
            ind.setTime_tracking(timetracking);
        }

        mcpt.mc_issue_note_add(
                info.getUsername(),
                new String(info.getPassword()),
                issue.getId(),
                ind);
        issue.refresh();
    }

    public byte[] getFile(MantisIssue mi, AttachmentData ad) throws ServiceException, RemoteException{
        MantisConnectPortType mcpt = getClient();
        return mcpt.mc_issue_attachment_get(
                info.getUsername(),
                new String(info.getPassword()),
                ad.getId());
    }

    public void removeFile(MantisIssue mi, AttachmentData ad) throws ServiceException, RemoteException {
        MantisConnectPortType mcpt = getClient();
        mcpt.mc_issue_attachment_delete(
                info.getUsername(),
                new String(info.getPassword()),
                ad.getId());
        updateIssueFromRepository(mi);
    }

    public void addFile(MantisIssue issue, File f, String comment) throws ServiceException, RemoteException, IOException {
        MantisConnectPortType mcpt = getClient();
        mcpt.mc_issue_attachment_add(
                info.getUsername(),
                new String(info.getPassword()),
                issue.getId(),
                f.getName(),
                null,
                fileGetContents(f));
        if (comment != null && (!comment.isEmpty())) {
            IssueNoteData ind = new IssueNoteData();
            ind.setText(comment);
            mcpt.mc_issue_note_add(
                    info.getUsername(),
                    new String(info.getPassword()),
                    issue.getId(),
                    ind);
        }
        issue.refresh();
    }

    public void removeRelationship(final MantisIssue issue, final RelationshipData rd)
            throws ServiceException, RemoteException {
        MantisConnectPortType mcpt = getClient();
        mcpt.mc_issue_relationship_delete(
                info.getUsername(),
                new String(info.getPassword()),
                issue.getId(),
                rd.getId());
        issue.refresh();
    }

    public void addRelationship(final MantisIssue issue, final ObjectRef type, final BigInteger target)
            throws ServiceException, RemoteException {
        MantisConnectPortType mcpt = getClient();
        RelationshipData rd = new RelationshipData();
        rd.setTarget_id(target);
        rd.setType(type);
        mcpt.mc_issue_relationship_add(
                info.getUsername(),
                new String(info.getPassword()),
                issue.getId(),
                rd);
        issue.refresh();
    }

    public List<MantisIssue> findIssues(MantisQuery mq) throws ServiceException, RemoteException  {
        MantisConnectPortType mcpt = getClient();
        Set<BigInteger> matchingIds = new HashSet<>();
        if (mq.getServersideFilterId() != null) {
            IssueHeaderData[] ids = mcpt.mc_filter_get_issue_headers(
                    info.getUsername(),
                    new String(info.getPassword()),
                    mq.getProjectId(),
                    mq.getServersideFilterId(),
                    BigInteger.valueOf(0),
                    BigInteger.valueOf(-1));
            for (IssueHeaderData id : ids) {
                if (mq.matchesFilter(id)) {
                    matchingIds.add(id.getId());
                }
            }
        } else {
            IssueHeaderData[] ids;
            if (getVersion().compareTo(new Version("2.0.0")) >= 0) {
                // The mc_project_get_issue_headers suppresses configurable
                // issue states (by default the closed issues). The
                // mc_filter_search_issue_headers was added with mantis 2.0.0
                // and is even useful if no filter is specified, as at least
                // this method returns the complete list
                ids = mcpt.mc_filter_search_issue_headers(
                        info.getUsername(),
                        new String(info.getPassword()),
                        mq.getAsServerFilter(),
                        BigInteger.valueOf(0),
                        BigInteger.valueOf(-1)
                );
            } else {
                ids = mcpt.mc_project_get_issue_headers(
                        info.getUsername(),
                        new String(info.getPassword()),
                        mq.getProjectId(),
                        BigInteger.valueOf(0),
                        BigInteger.valueOf(-1));
            }
            for (IssueHeaderData id : ids) {
                if (mq.matchesFilter(id)) {
                    matchingIds.add(id.getId());
                }
            }
        }
        return getIssues(false, matchingIds.toArray(new BigInteger[matchingIds.size()]));
    }



    public void addTag(MantisIssue issue, String... newTagsString) throws ServiceException, RemoteException  {
        MantisConnectPortType mcpt = getClient();
        ObjectRef[] oldList = issue.getTags();
        List<TagData> newList = new ArrayList<>();
        if (oldList != null) {
            for (ObjectRef or : oldList) {
                newList.add(masterData.getTag(or.getId()));
            }
        }

        for (String tag : newTagsString) {
            TagData newTag = masterData.addTag(tag);

            boolean inList = false;
            for (TagData td : newList) {
                if (td.getId().equals(newTag.getId())) {
                    inList = true;
                    break;
                }
            }
            if (!inList) {
                newList.add(newTag);
            }
        }

        mcpt.mc_issue_set_tags(
                info.getUsername(),
                new String(info.getPassword()),
                issue.getId(),
                newList.toArray(new TagData[0]));
        issue.refresh();
    }

    public void removeTag(MantisIssue issue, ObjectRef... tagsToBeRemoved) throws ServiceException, RemoteException  {
        List<BigInteger> toBeRemoved = new ArrayList<>();
        for (ObjectRef tag : tagsToBeRemoved) {
            toBeRemoved.add(tag.getId());
        }
        ObjectRef[] oldList = issue.getTags();
        List<TagData> newList = new ArrayList<>();
        if (oldList != null) {
            for (ObjectRef or : oldList) {
                if (!toBeRemoved.contains(or.getId())) {
                    newList.add(masterData.getTag(or.getId()));
                }
            }
        }
        getClient().mc_issue_set_tags(
                info.getUsername(),
                new String(info.getPassword()),
                issue.getId(),
                newList.toArray(new TagData[0]));
        issue.refresh();
    }

    public Capabilities getCapabilities() {
        return capabilities;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    private static byte[] fileGetContents(File f) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FileInputStream fis = new FileInputStream(f)) {
            int read;
            byte[] buffer = new byte[1024];
            while ((read = fis.read(buffer)) > 0) {
                baos.write(buffer, 0, read);
            }
        }
        return baos.toByteArray();
    }

    public ExceptionHandler getExceptionHandler() {
        if(exceptionHandler == null) {
            exceptionHandler = new ExceptionHandler(this);
        }
        return exceptionHandler;
    }

    public ObjectRef getCommitStatus() {
        return readObjectRef(info, PROP_COMMIT_STATUS_FIELD);
    }

    public ObjectRef getCommitResolution() {
        return readObjectRef(info, PROP_COMMIT_RESOLUTION_FIELD);
    }

    static ObjectRef readObjectRef(RepositoryInfo ri, String key) {
        String fieldString = ri.getValue(key);
        if (fieldString != null) {
            String[] parts = fieldString.split("#", 2);
            if(parts.length == 2) {
                try {
                    return new ObjectRef(new BigInteger(parts[0]), parts[1]);
                } catch (NumberFormatException ex) {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    static void writeObjectRef(RepositoryInfo ri, String key, ObjectRef value) {
        if(value == null) {
            ri.putValue(key, null);
        } else {
            ri.putValue(key, value.getId() + "#" + value.getName());
        }
    }
}
