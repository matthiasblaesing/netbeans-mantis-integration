package eu.doppel_helix.netbeans.mantisintegration.repository;

import biz.futureware.mantisconnect.AccountData;
import biz.futureware.mantisconnect.AttachmentData;
import biz.futureware.mantisconnect.FilterData;
import biz.futureware.mantisconnect.IssueData;
import biz.futureware.mantisconnect.IssueHeaderData;
import biz.futureware.mantisconnect.IssueNoteData;
import biz.futureware.mantisconnect.MantisConnectLocator;
import biz.futureware.mantisconnect.MantisConnectPortType;
import biz.futureware.mantisconnect.ObjectRef;
import biz.futureware.mantisconnect.ProjectData;
import biz.futureware.mantisconnect.ProjectVersionData;
import biz.futureware.mantisconnect.RelationshipData;
import biz.futureware.mantisconnect.TagData;
import biz.futureware.mantisconnect.UserData;
import eu.doppel_helix.netbeans.mantisintegration.MantisConnector;
import eu.doppel_helix.netbeans.mantisintegration.data.Version;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import eu.doppel_helix.netbeans.mantisintegration.query.MantisQuery;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.InstanceContent;

public class MantisRepository {
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
    private ObjectRef[] priorities;
    private ObjectRef[] states;
    private ObjectRef[] resolutions;
    private ObjectRef[] reproducibilities;
    private ObjectRef[] viewStates;
    private ObjectRef[] severities;
    private ObjectRef[] projections;
    private ObjectRef[] etas;
    // Hardcoded, as no api exists to retrieve this info
    private final ObjectRef[] relationships = new ObjectRef[]{
        new ObjectRef(new BigInteger("0"), "duplicate of"),
        new ObjectRef(new BigInteger("1"), "related to"),
        new ObjectRef(new BigInteger("2"), "parent of"),
        new ObjectRef(new BigInteger("3"), "child of"),
        new ObjectRef(new BigInteger("4"), "has duplicate"),};
    private HashMap<BigInteger, ProjectData> projects;
    private HashMap<BigInteger, TagData> tags;
    private final HashMap<BigInteger, String[]> categories = new HashMap<>();
    private final HashMap<BigInteger, AccountData[]> users  = new HashMap<>();
    private final HashMap<BigInteger, ProjectVersionData[]> versions = new HashMap<>();
    private final HashMap<BigInteger, FilterData[]> filters = new HashMap<>();
    private IssueInfosHandler issueInfosHandler;
    private UserData account = null;
    private MantisRepositoryQueryStore queryStore;

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
    
    public static Version checkConnection(String url, String username, String password) throws ServiceException, RemoteException {
        String baseUrl = cleanUpUrl(url);

        if (!baseUrl.endsWith("/api/soap/mantisconnect.php")) {
            baseUrl += "/api/soap/mantisconnect.php";
        }

        ClassLoader origLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(MantisRepository.class.getClassLoader());
        try {

            MantisConnectLocator mcl = new MantisConnectLocator(
                    baseUrl + "?wsdl",
                    new QName("http://futureware.biz/mantisconnect", "MantisConnect"));
            try {
                MantisConnectPortType mcpt = mcl.getMantisConnectPort(new URL(baseUrl));
                // Test Authentication information
                mcpt.mc_projects_get_user_accessible(username, password);
                // Return version for user information
                return new Version(mcpt.mc_version());
            } catch (MalformedURLException ex) {
                throw new ServiceException("Broken client url:" + baseUrl, ex);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(origLoader);
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
        List<MantisIssue> result = new ArrayList<MantisIssue>();

        try {
            BigInteger possibleId = new BigInteger(criteria);
            List<MantisIssue> issues = getIssues(false, possibleId.toString());
            result.addAll(issues);
        } catch (NumberFormatException ex) {
        }

        if (result.isEmpty()) {
            initProjectList();

            Pattern p = null;
            try {
                p = Pattern.compile(criteria);
            } catch (PatternSyntaxException ex) {
            }

            MantisConnectPortType mcpt = getClient();
            List<BigInteger> matchingIds = new ArrayList<BigInteger>();
            OUTER:
            for (BigInteger projectID : projects.keySet()) {
                for (int i = 0; i < 1000; i++) {
                    IssueHeaderData[] ids = mcpt.mc_project_get_issue_headers(
                            info.getUsername(),
                            new String(info.getPassword()),
                            projectID,
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
    
    public void saveQuery(MantisQuery mq) {
        queryStore.saveMantisQuery(mq);
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
            baseUrl = baseUrl.substring(0, baseUrl.length() - 2);
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
    
    protected MantisConnectPortType getClient() throws ServiceException {
        if (client == null) {
            ClassLoader origLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            try {
                String baseUrl = getBaseUrl();
                
                if (!baseUrl.endsWith("/api/soap/mantisconnect.php")) {
                    baseUrl += "/api/soap/mantisconnect.php";
                }
                
                MantisConnectLocator mcl = new MantisConnectLocator(
                        baseUrl + "?wsdl",
                        new QName("http://futureware.biz/mantisconnect", "MantisConnect"));
                try {
                    client = mcl.getMantisConnectPort(new URL(baseUrl));
                } catch (MalformedURLException ex) {
                    throw new ServiceException("Broken client url:" + baseUrl, ex);
                }
            } finally {
                Thread.currentThread().setContextClassLoader(origLoader);
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

    public void checkin(MantisIssue issue, String comment, boolean closeAsFixed)
            throws ServiceException, RemoteException {
        MantisConnectPortType mcpt = getClient();
        mcpt.mc_issue_checkin(
                info.getUsername(),
                new String(info.getPassword()),
                issue.getId(),
                comment,
                closeAsFixed);
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

    public ObjectRef[] getPriorities() throws ServiceException, RemoteException {
        if (priorities == null) {
            MantisConnectPortType mcpt = getClient();
            priorities = mcpt.mc_enum_priorities(
                    info.getUsername(),
                    new String(info.getPassword()));
        }
        return priorities;
    }

    public ObjectRef[] getRelationships() {
        return relationships;
    }

    public ObjectRef[] getStates() throws ServiceException, RemoteException {
        if (states == null) {
            MantisConnectPortType mcpt = getClient();
            states = mcpt.mc_enum_status(
                    info.getUsername(),
                    new String(info.getPassword()));
        }
        return states;
    }

    public ObjectRef[] getResolutions() throws RemoteException, ServiceException{
        if (resolutions == null) {
            MantisConnectPortType mcpt = getClient();
            resolutions = mcpt.mc_enum_resolutions(
                    info.getUsername(),
                    new String(info.getPassword()));
        }
        return resolutions;
    }

    public ObjectRef[] getReproducibilities() throws ServiceException, RemoteException {
        if (reproducibilities == null) {
            MantisConnectPortType mcpt = getClient();
            reproducibilities = mcpt.mc_enum_reproducibilities(
                    info.getUsername(),
                    new String(info.getPassword()));
        }
        return reproducibilities;
    }

    public ObjectRef[] getViewStates() throws ServiceException, RemoteException {
        if (viewStates == null) {
            MantisConnectPortType mcpt = getClient();
            viewStates = mcpt.mc_enum_view_states(
                    info.getUsername(),
                    new String(info.getPassword()));
        }
        return viewStates;
    }

    public ObjectRef[] getSeverities() throws ServiceException, RemoteException {
        if (severities == null) {
            MantisConnectPortType mcpt = getClient();
            severities = mcpt.mc_enum_severities(
                    info.getUsername(),
                    new String(info.getPassword()));
        }
        return severities;
    }

    public ObjectRef[] getProjections() throws ServiceException, RemoteException {
        if (projections == null) {
            MantisConnectPortType mcpt = getClient();
            projections = mcpt.mc_enum_projections(
                    info.getUsername(),
                    new String(info.getPassword()));
        }
        return projections;
    }

    public ObjectRef[] getEtas() throws ServiceException, RemoteException {
        if (etas == null) {
            MantisConnectPortType mcpt = getClient();
            etas = mcpt.mc_enum_etas(
                    info.getUsername(),
                    new String(info.getPassword()));
        }
        return etas;
    }

    private void initProjectList() throws ServiceException, RemoteException {
        if (projects == null) {
            MantisConnectPortType mcpt = getClient();
            projects = new HashMap<BigInteger, ProjectData>();
            ProjectData[] projectList = mcpt.mc_projects_get_user_accessible(
                    info.getUsername(),
                    new String(info.getPassword()));
            for (ProjectData pd : projectList) {
                projects.put(pd.getId(), pd);
            }
        }
    }

    public ProjectData getProject(BigInteger id) throws ServiceException, RemoteException  {
        initProjectList();
        return projects.get(id);
    }

    public ProjectData[] getProjects() throws ServiceException, RemoteException  {
        initProjectList();
        Collection<ProjectData> projectCollection = projects.values();
        return projectCollection.toArray(new ProjectData[projectCollection.size()]);
    }

    public String[] getCategories(BigInteger projectID) throws ServiceException, RemoteException {
        String[] result = categories.get(projectID);
        if (result == null) {
            MantisConnectPortType mcpt = getClient();
            if (BigInteger.ZERO.equals(projectID)) {
                TreeSet<String> categoriesSet = new TreeSet<String>();
                for (ProjectData pd : getProjects()) {
                    String[] projCategories = mcpt.mc_project_get_categories(
                            info.getUsername(),
                            new String(info.getPassword()),
                            pd.getId());
                    categoriesSet.addAll(Arrays.asList(projCategories));
                }
                categories.put(projectID, categoriesSet.toArray(new String[0]));
            } else {
                String[] projCategories = mcpt.mc_project_get_categories(
                        info.getUsername(),
                        new String(info.getPassword()),
                        projectID);
                categories.put(projectID, projCategories);
            }
        }
        return categories.get(projectID);
    }

    public AccountData[] getUsers(BigInteger projectID) throws ServiceException, RemoteException  {
        if (users.get(projectID) == null) {
            MantisConnectPortType mcpt = getClient();
            try {
                AccountData[] projCategories = mcpt.mc_project_get_users(
                        info.getUsername(),
                        new String(info.getPassword()),
                        projectID,
                        BigInteger.ONE);
                users.put(projectID, projCategories);
            } catch (RemoteException ex) {
                // @todo: Find a sane solution for the problem where the API
                //        limits the usefulness (=> here mantis fails to return
                //        user counts > approx. 15000 Users)
                logger.log(Level.INFO, "", ex);
                users.put(projectID, new AccountData[0]);
            }
        }
        AccountData[] result = users.get(projectID);
        if(result == null) {
            result = new AccountData[0];
        }
        return result;
    }

    public ProjectVersionData[] getVersions(BigInteger projectID) throws ServiceException, RemoteException  {
        if (versions.get(projectID) == null) {
            MantisConnectPortType mcpt = getClient();
            try {
                ProjectVersionData[] projVersions = mcpt.mc_project_get_versions(
                        info.getUsername(),
                        new String(info.getPassword()),
                        projectID);
                Arrays.sort(projVersions, new ProjectVersionDataComparator());
                versions.put(projectID, projVersions);
            } catch (RemoteException ex) {
                logger.log(Level.INFO, "", ex);
                versions.put(projectID, new ProjectVersionData[0]);
            }
        }
        ProjectVersionData[] result = versions.get(projectID);
        if(result == null) {
            result = new ProjectVersionData[0];
        }
        return result;
    }
    
    public FilterData[] getFilters(BigInteger projectID) throws ServiceException, RemoteException  {
        if (filters.get(projectID) == null) {
            MantisConnectPortType mcpt = getClient();
            FilterData[] fd = mcpt.mc_filter_get(
                    info.getUsername(),
                    new String(info.getPassword()),
                    projectID);
            filters.put(projectID, fd);
        }
        return filters.get(projectID);
    }

    public List<MantisIssue> findIssues(MantisQuery mq) throws ServiceException, RemoteException  {
        BigInteger PAGE_SIZE = BigInteger.valueOf(500);
        MantisConnectPortType mcpt = getClient();
        Set<BigInteger> matchingIds = new HashSet<BigInteger>();
        if (mq.getServersideFilterId() != null) {
            for (int i = 0; i < 1000; i++) {
                IssueHeaderData[] ids = mcpt.mc_filter_get_issue_headers(
                        info.getUsername(),
                        new String(info.getPassword()),
                        mq.getProjectId(),
                        mq.getServersideFilterId(),
                        BigInteger.valueOf(i),
                        PAGE_SIZE);
                for (IssueHeaderData id : ids) {
                    if (mq.matchesFilter(id)) {
                        matchingIds.add(id.getId());
                    }
                }
                if (ids.length < PAGE_SIZE.intValue()) {
                    break;
                }
            }
        } else {
            for (int i = 0; i < 1000; i++) {
                IssueHeaderData[] ids = mcpt.mc_project_get_issue_headers(
                        info.getUsername(),
                        new String(info.getPassword()),
                        mq.getProjectId(),
                        BigInteger.valueOf(i),
                        PAGE_SIZE);
                for (IssueHeaderData id : ids) {
                    if (mq.matchesFilter(id)) {
                        matchingIds.add(id.getId());
                    }
                }
                if (ids.length < PAGE_SIZE.intValue()) {
                    break;
                }
            }
        }
        String ids[] = new String[matchingIds.size()];
        int count = 0;
        for(BigInteger bi: matchingIds) {
            ids[count] = bi.toString();
            count++;
        }
        return getIssues(false, matchingIds.toArray(new BigInteger[0]));
    }

    private void initTags(boolean enforceUpdate) throws ServiceException, RemoteException {
        if (enforceUpdate) {
            tags = null;
        }
        if (tags == null) {
            tags = new HashMap<BigInteger, TagData>();
            MantisConnectPortType mcpt = getClient();
            BigInteger PAGE_SIZE = BigInteger.valueOf(1000);
            for (int i = 0; i < 1000; i++) {
                TagData[] tagList = mcpt.mc_tag_get_all(
                        info.getUsername(),
                        new String(info.getPassword()),
                        BigInteger.valueOf(i),
                        PAGE_SIZE).getResults();
                for (TagData tag : tagList) {
                    tags.put(tag.getId(), tag);
                }
                if (tagList.length < PAGE_SIZE.intValue()) {
                    break;
                }
            }
        }
    }

    public void addTag(MantisIssue issue, String... newTagsString) throws ServiceException, RemoteException  {
        initTags(false);
        MantisConnectPortType mcpt = getClient();
        ObjectRef[] oldList = issue.getTags();
        List<TagData> newList = new ArrayList<TagData>();
        if (oldList != null) {
            for (ObjectRef or : oldList) {
                newList.add(tags.get(or.getId()));
            }
        }

        for (String tag : newTagsString) {
            TagData newTag = null;
            for (TagData td : tags.values()) {
                if (td.getName().equals(tag)) {
                    newTag = td;
                }
            }
            if (newTag == null) {
                newTag = new TagData();
                newTag.setDescription("");
                newTag.setName(tag);
                BigInteger id = mcpt.mc_tag_add(
                        info.getUsername(),
                        new String(info.getPassword()),
                        newTag);
                newTag.setId(id);
                tags.put(id, newTag);
            }
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
        initTags(false);
        List<BigInteger> toBeRemoved = new ArrayList<BigInteger>();
        for (ObjectRef tag : tagsToBeRemoved) {
            toBeRemoved.add(tag.getId());
        }
        ObjectRef[] oldList = issue.getTags();
        List<TagData> newList = new ArrayList<TagData>();
        if (oldList != null) {
            for (ObjectRef or : oldList) {
                if (!toBeRemoved.contains(or.getId())) {
                    newList.add(tags.get(or.getId()));
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

    public TagData getTag(BigInteger id) throws ServiceException, RemoteException  {
        initTags(false);
        return tags.get(id);
    }

    public List<TagData> getTags() throws ServiceException, RemoteException  {
        initTags(false);
        return new ArrayList<TagData>(tags.values());
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
        FileInputStream fis = new FileInputStream(f);
        int read = 0;
        byte[] buffer = new byte[1024];
        while ((read = fis.read(buffer)) > 0) {
            baos.write(buffer, 0, read);
        }
        fis.close();
        return baos.toByteArray();
    }

}
