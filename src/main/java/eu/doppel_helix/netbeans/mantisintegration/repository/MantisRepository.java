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
import biz.futureware.mantisconnect.RelationshipData;
import biz.futureware.mantisconnect.TagData;
import biz.futureware.mantisconnect.UserData;
import eu.doppel_helix.netbeans.mantisintegration.Mantis;
import eu.doppel_helix.netbeans.mantisintegration.MantisConnector;
import eu.doppel_helix.netbeans.mantisintegration.entity.Version;
import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import eu.doppel_helix.netbeans.mantisintegration.query.MantisQuery;
import eu.doppel_helix.netbeans.mantisintegration.swing.ImageIconWrapper;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import org.apache.axis.encoding.Base64;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

public class MantisRepository {

    private RequestProcessor requestProzessor = new RequestProcessor(MantisRepository.class);
    private final static Logger logger = Logger.getLogger(MantisRepository.class.getName());
    private final static Image ICON = ImageUtilities.loadImage(
            "eu/doppel_helix/netbeans/mantisintegration/icon.png");
    private final static Version tagVersion = new Version("1.2.9");
    private RepositoryInfo info;
    private MantisRepositoryController controller;
    private Lookup lookup;
    private boolean iconLoaded = false;
    private Image icon = null;
    private final transient InstanceContent ic;
    private MantisConnectPortType client;
    private Cache cache;
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
    private ObjectRef[] relationships = new ObjectRef[]{
        new ObjectRef(new BigInteger("0"), "duplicate of"),
        new ObjectRef(new BigInteger("1"), "related to"),
        new ObjectRef(new BigInteger("2"), "parent of"),
        new ObjectRef(new BigInteger("3"), "child of"),
        new ObjectRef(new BigInteger("4"), "has duplicate"),};
    private HashMap<BigInteger, ProjectData> projects;
    private HashMap<BigInteger, TagData> tags;
    private HashMap<BigInteger, String[]> categories = new HashMap<BigInteger, String[]>();
    private HashMap<BigInteger, AccountData[]> users = new HashMap<BigInteger, AccountData[]>();
    private HashMap<BigInteger, FilterData[]> filters = new HashMap<BigInteger, FilterData[]>();
    private UserData account = null;

    public MantisRepository() {
        info = new RepositoryInfo(
                MantisConnector.ID + System.currentTimeMillis(),
                MantisConnector.ID,
                "http://",
                "",
                "");
        ic = new InstanceContent();
    }

    public MantisRepository(RepositoryInfo ri) {
        this();
        setInfo(ri);
    }

    public RequestProcessor getRequestProcessor() {
        return requestProzessor;
    }

    public Repository getRepository() {
        return Mantis.getInstance().getBugtrackingFactory().getRepository(
                info.getConnectorId(),
                info.getId());
    }

    private IssueCache<MantisIssue, IssueData> getIssueCache() {
        if (cache == null && getRepository() != null) {
            cache = new Cache();
        }
        return cache;
    }

    public Image getIcon() {
        try {
            updateIcon();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        try {
            if (icon == null) {
                String encoded = info.getValue("icon");
                if (encoded != null) {
                    byte[] iconData = Base64.decode(encoded);
                    BufferedImage tempImage = ImageIO.read(new ByteArrayInputStream(iconData));
                    if (tempImage != null) {
                        icon = new ImageIconWrapper(tempImage);
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (icon == null) {
            return ICON;
        } else {
            return icon;
        }
    }

    public Lookup getLookup() {
        if (lookup == null) {
            lookup = new AbstractLookup(ic);
        }
        if (getIssueCache() != null && lookup.lookup(IssueCache.class) == null) {
            ic.add(getIssueCache());
        }
        return lookup;
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

    public MantisIssue[] getIssues(String... issues) {
        MantisIssue[] results = new MantisIssue[issues.length];
        for (int i = 0; i < issues.length; i++) {
            MantisIssue issue = (MantisIssue) getIssueCache().getIssue(issues[i]);
            if (issue == null) {
                issue = new MantisIssue(this);
                issue.setId(new BigInteger(issues[i]));
                issue.refresh();
            }
            results[i] = issue;
        }
        return results;
    }

    public MantisIssue[] getIssues(BigInteger... issues) {
        MantisIssue[] results = new MantisIssue[issues.length];
        for (int i = 0; i < issues.length; i++) {
            MantisIssue issue = (MantisIssue) getIssueCache().getIssue(issues[i].toString());
            if (issue == null) {
                issue = new MantisIssue(this);
                issue.setId(issues[i]);
                issue.refresh();
            }
            results[i] = issue;
        }
        return results;
    }
    
    public Collection<MantisIssue> simpleSearch(String criteria) {
        int pageSize = 1000;
        List<MantisIssue> result = new ArrayList<MantisIssue>();

        try {
            BigInteger possibleId = new BigInteger(criteria);
            MantisIssue[] issues = getIssues(possibleId.toString());
            result.addAll(Arrays.asList(issues));
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
            IssueCache ic = getIssueCache();
            List<BigInteger> matchingIds = new ArrayList<BigInteger>();
            try {
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
                                    || p.matcher(id.getSummary()).find()) {
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
                result.addAll(Arrays.asList(getIssues(matchingIds.toArray(new BigInteger[0]))));
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "Error while doing simple bug search", ex);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Error while doing simple bug search", ex);
            }
        }

        return result;
    }

    public MantisQuery createQuery() {
        return new MantisQuery(this);
    }

    public MantisIssue createIssue() {
        return new MantisIssue(this);
    }

    public Collection<MantisQuery> getQueries() {
        return Collections.EMPTY_LIST;
    }

    public Version getVersion() {
        if (version == null) {
            try {
                String versionString = getClient().mc_version();
                version = new Version(versionString);
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "Failed to fetch version of mantis repository", ex);
            }
        }
        return version;
    }

    public boolean isTagSupport() {
        return getVersion().compareTo(tagVersion) >= 0;
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
        if (baseUrl.endsWith("/api/soap/mantisconnect.php")) {
            baseUrl = baseUrl.replaceAll("/api/soap/mantisconnect.php$", "");
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 2);
        }
        return baseUrl;
    }

    private void updateIcon() throws IOException {
        if (!iconLoaded) {
            URL url = new URL(getBaseUrl() + "/images/favicon.ico");
            InputStream is = url.openStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read = 0;

            while ((read = is.read(buffer)) > 0) {
                baos.write(buffer, 0, read);
            }

            is.close();

            byte[] iconData = baos.toByteArray();
            BufferedImage tempImage = ImageIO.read(new ByteArrayInputStream(iconData));

            if (tempImage != null) {
                icon = new ImageIconWrapper(tempImage);
                info.putValue("icon", Base64.encode(iconData));
            }

            iconLoaded = true;
        }
    }

    public UserData getAccount() {
        if(account == null) {
            try {
                account = getClient().mc_login(info.getUsername(),
                        new String(info.getPassword()));
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "Error retrieving account data", ex);
            }
        }
        return account;
    }
    
    protected MantisConnectPortType getClient() {
        if (client == null) {
            try {
                String baseUrl = getBaseUrl();

                if (!baseUrl.endsWith("/api/soap/mantisconnect.php")) {
                    baseUrl += "/api/soap/mantisconnect.php";
                }
                MantisConnectLocator mcl = new MantisConnectLocator(
                        getBaseUrl() + "/api/soap/mantisconnect.php" + "?wsdl",
                        new QName("http://futureware.biz/mantisconnect", "MantisConnect"));
                client = mcl.getMantisConnectPort(new URL(baseUrl));
            } catch (Throwable ex) {
                logger.log(Level.WARNING, "Error creating client", ex);
            }
        }
        return client;
    }

    public boolean updateIssueFromRepository(final MantisIssue issue) {
        if (issue.getId() == null) {
            return false;
        }
        MantisConnectPortType mcpt = getClient();
        try {
            final IssueData id = mcpt.mc_issue_get(
                    info.getUsername(),
                    new String(info.getPassword()),
                    issue.getId());
            getIssueCache().setIssueData(id.getId().toString(), id);
            issue.updateFromIssueData(id);
            return true;
        } catch (RemoteException ex) {
            logger.log(Level.WARNING, "Error updating issue", ex);
            return false;
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error updating issue", ex);
            return false;
        }
    }

    public void addIssue(final MantisIssue issue, IssueData newData) {
        if (issue.getId() != null) {
            this.updateIssue(issue, newData);
            return;
        }
        MantisConnectPortType mcpt = getClient();

        try {
            BigInteger id = mcpt.mc_issue_add(
                    info.getUsername(),
                    new String(info.getPassword()),
                    newData);
            issue.setId(id);
            issue.refresh();
        } catch (RemoteException ex) {
            logger.log(Level.WARNING, "Failed to create issue", ex);
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Failed to create issue", ex);
        }
    }

    public void updateIssue(final MantisIssue issue, IssueData newData) {
        if (issue.getId() == null) {
            addIssue(issue, newData);
            return;
        }
        MantisConnectPortType mcpt = getClient();
        try {
            mcpt.mc_issue_update(
                    info.getUsername(),
                    new String(info.getPassword()),
                    issue.getId(),
                    newData);
            issue.refresh();
        } catch (RemoteException ex) {
            logger.log(Level.WARNING, "Failed to update issue", ex);
        }
    }

    public void checkin(MantisIssue issue, String comment, boolean closeAsFixed) {
        MantisConnectPortType mcpt = getClient();
        try {
            mcpt.mc_issue_checkin(
                    info.getUsername(),
                    new String(info.getPassword()),
                    issue.getId(),
                    comment,
                    closeAsFixed);
        } catch (RemoteException ex) {
            logger.log(Level.WARNING, "Failed to checkin on issue", ex);
        }
        issue.refresh();
    }

    public void addComment(MantisIssue issue, String comment, ObjectRef viewState) {
        MantisConnectPortType mcpt = getClient();
        try {
            IssueNoteData ind = new IssueNoteData();
            ind.setText(comment);
            ind.setView_state(viewState);
            mcpt.mc_issue_note_add(
                    info.getUsername(),
                    new String(info.getPassword()),
                    issue.getId(),
                    ind);
        } catch (RemoteException ex) {
            logger.log(Level.WARNING, "Error adding comment to issue", ex);
        }
        issue.refresh();
    }

    public byte[] getFile(MantisIssue mi, AttachmentData ad) {
        MantisConnectPortType mcpt = getClient();
        try {
            return mcpt.mc_issue_attachment_get(
                    info.getUsername(),
                    new String(info.getPassword()),
                    ad.getId());
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void removeFile(MantisIssue mi, AttachmentData ad) {
        MantisConnectPortType mcpt = getClient();
        try {
            mcpt.mc_issue_attachment_delete(
                    info.getUsername(),
                    new String(info.getPassword()),
                    ad.getId());
            updateIssueFromRepository(mi);
        } catch (RemoteException ex) {
            logger.log(Level.WARNING, "Error removing attachment from issue", ex);
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error removing attachment from issue", ex);
        }
    }

    public void addFile(MantisIssue issue, File f, String comment) {
        MantisConnectPortType mcpt = getClient();
        try {
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
        } catch (RemoteException ex) {
            logger.log(Level.WARNING, "", ex);
        } catch (IOException ex) {
            logger.log(Level.WARNING, "", ex);
        }
    }

    public void removeRelationship(final MantisIssue issue, final RelationshipData rd) {
        MantisConnectPortType mcpt = getClient();
        try {
            mcpt.mc_issue_relationship_delete(
                    info.getUsername(),
                    new String(info.getPassword()),
                    issue.getId(),
                    rd.getId());
        } catch (RemoteException ex) {
            logger.log(Level.WARNING, "", ex);
        } catch (IOException ex) {
            logger.log(Level.WARNING, "", ex);
        }
        issue.refresh();
    }

    public void addRelationship(final MantisIssue issue, final ObjectRef type, final BigInteger target) {
        MantisConnectPortType mcpt = getClient();
        try {
            RelationshipData rd = new RelationshipData();
            rd.setTarget_id(target);
            rd.setType(type);
            mcpt.mc_issue_relationship_add(
                    info.getUsername(),
                    new String(info.getPassword()),
                    issue.getId(),
                    rd);
        } catch (RemoteException ex) {
            logger.log(Level.WARNING, "", ex);
        } catch (IOException ex) {
            logger.log(Level.WARNING, "", ex);
        }
        issue.refresh();
    }

    public ObjectRef[] getPriorities() {
        if (priorities == null) {
            MantisConnectPortType mcpt = getClient();
            try {
                priorities = mcpt.mc_enum_priorities(
                        info.getUsername(),
                        new String(info.getPassword()));
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "", ex);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "", ex);
            }
        }
        return priorities;
    }

    public ObjectRef[] getRelationships() {
        return relationships;
    }

    public ObjectRef[] getStates() {
        if (states == null) {
            MantisConnectPortType mcpt = getClient();
            try {
                states = mcpt.mc_enum_status(
                        info.getUsername(),
                        new String(info.getPassword()));
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "", ex);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "", ex);
            }
        }
        return states;
    }

    public ObjectRef[] getResolutions() {
        if (resolutions == null) {
            MantisConnectPortType mcpt = getClient();
            try {
                resolutions = mcpt.mc_enum_resolutions(
                        info.getUsername(),
                        new String(info.getPassword()));
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "", ex);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "", ex);
            }
        }
        return resolutions;
    }

    public ObjectRef[] getReproducibilities() {
        if (reproducibilities == null) {
            MantisConnectPortType mcpt = getClient();
            try {
                reproducibilities = mcpt.mc_enum_reproducibilities(
                        info.getUsername(),
                        new String(info.getPassword()));
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "", ex);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "", ex);
            }
        }
        return reproducibilities;
    }

    public ObjectRef[] getViewStates() {
        if (viewStates == null) {
            MantisConnectPortType mcpt = getClient();
            try {
                viewStates = mcpt.mc_enum_view_states(
                        info.getUsername(),
                        new String(info.getPassword()));
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "", ex);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "", ex);
            }
        }
        return viewStates;
    }

    public ObjectRef[] getSeverities() {
        if (severities == null) {
            MantisConnectPortType mcpt = getClient();
            try {
                severities = mcpt.mc_enum_severities(
                        info.getUsername(),
                        new String(info.getPassword()));
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "", ex);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "", ex);
            }
        }
        return severities;
    }

    public ObjectRef[] getProjections() {
        if (projections == null) {
            MantisConnectPortType mcpt = getClient();
            try {
                projections = mcpt.mc_enum_projections(
                        info.getUsername(),
                        new String(info.getPassword()));
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "", ex);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "", ex);
            }
        }
        return projections;
    }

    public ObjectRef[] getEtas() {
        if (etas == null) {
            MantisConnectPortType mcpt = getClient();
            try {
                etas = mcpt.mc_enum_etas(
                        info.getUsername(),
                        new String(info.getPassword()));
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "", ex);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "", ex);
            }
        }
        return etas;
    }

    private void initProjectList() {
        if (projects == null) {
            MantisConnectPortType mcpt = getClient();
            try {
                projects = new HashMap<BigInteger, ProjectData>();
                ProjectData[] projectList = mcpt.mc_projects_get_user_accessible(
                        info.getUsername(),
                        new String(info.getPassword()));
                for (ProjectData pd : projectList) {
                    projects.put(pd.getId(), pd);
                }
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "", ex);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "", ex);
            }
        }
    }

    public ProjectData getProject(BigInteger id) {
        initProjectList();
        return projects.get(id);
    }

    public ProjectData[] getProjects() {
        initProjectList();
        Collection<ProjectData> projectCollection = projects.values();
        return projectCollection.toArray(new ProjectData[projectCollection.size()]);
    }

    public String[] getCategories(BigInteger projectID) {
        String[] result = categories.get(projectID);
        if (result == null) {
            MantisConnectPortType mcpt = getClient();
            try {
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
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "", ex);
            }
        }
        return categories.get(projectID);
    }

    public AccountData[] getUsers(BigInteger projectID) {
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
                logger.log(Level.INFO, "", ex);
                users.put(projectID, new AccountData[0]);
            } catch (IOException ex) {
                logger.log(Level.INFO, "", ex);
                users.put(projectID, new AccountData[0]);
            }
        }
        return users.get(projectID);
    }

    public FilterData[] getFilters(BigInteger projectID) {
        if (filters.get(projectID) == null) {
            MantisConnectPortType mcpt = getClient();
            try {
                FilterData[] fd = mcpt.mc_filter_get(
                        info.getUsername(),
                        new String(info.getPassword()),
                        projectID);
                filters.put(projectID, fd);
            } catch (RemoteException ex) {
                logger.log(Level.WARNING, "", ex);
            } catch (IOException ex) {
                logger.log(Level.WARNING, "", ex);
            }
        }
        return filters.get(projectID);
    }

    public List<MantisIssue> findIssues(MantisQuery mq) {
        BigInteger PAGE_SIZE = BigInteger.valueOf(500);
        List<MantisIssue> result = new ArrayList<MantisIssue>();
        MantisConnectPortType mcpt = getClient();
        Set<BigInteger> matchingIds = new HashSet<BigInteger>();
        IssueCache<MantisIssue, IssueData> ic = getIssueCache();
        try {
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
                        if(mq.matchesFilter(id)) {
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
                        if(mq.matchesFilter(id)) {
                            matchingIds.add(id.getId());
                        }
                    }
                    if (ids.length < PAGE_SIZE.intValue()) {
                        break;
                    }
                }
            }
            return Arrays.asList(getIssues(matchingIds.toArray(new BigInteger[0])));
        } catch (RemoteException ex) {
            logger.log(Level.WARNING, "", ex);
            return Collections.EMPTY_LIST;
        } catch (IOException ex) {
            logger.log(Level.WARNING, "", ex);
            return Collections.EMPTY_LIST;
        }
    }

    private void initTags(boolean enforceUpdate) {
        if (enforceUpdate) {
            tags = null;
        }
        if (tags == null) {
            tags = new HashMap<BigInteger, TagData>();
            MantisConnectPortType mcpt = getClient();
            try {
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
            } catch (RemoteException ex) {
                logger.log(Level.INFO, "", ex);
                tags = new HashMap<BigInteger, TagData>();
            }
        }
    }

    public void addTag(MantisIssue issue, String... newTagsString) {
        initTags(false);
        MantisConnectPortType mcpt = getClient();
        try {
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
        } catch (RemoteException ex) {
            logger.log(Level.WARNING, "", ex);
        }
    }

    public void removeTag(MantisIssue issue, ObjectRef... tagsToBeRemoved) {
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
        try {
            getClient().mc_issue_set_tags(
                    info.getUsername(),
                    new String(info.getPassword()),
                    issue.getId(),
                    newList.toArray(new TagData[0]));
            issue.refresh();
        } catch (RemoteException ex) {
            logger.log(Level.WARNING, "", ex);
        }
    }

    public TagData getTag(BigInteger id) {
        return tags.get(id);
    }

    public List<TagData> getTags() {
        initTags(false);
        return new ArrayList<TagData>(tags.values());
    }

    public static byte[] fileGetContents(File f) throws IOException {
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

    private class Cache extends IssueCache<MantisIssue, IssueData> {

        Cache() {
            super(
                    MantisRepository.this.getInfo().getUrl(),
                    new IssueAccessorImpl(),
                    Mantis.getInstance().getIssueProvider(),
                    MantisRepository.this.getRepository());
        }
    }

    private class IssueAccessorImpl implements IssueCache.IssueAccessor<MantisIssue, IssueData> {

        @Override
        public MantisIssue createIssue(IssueData taskData) {
            MantisIssue issue = new MantisIssue(MantisRepository.this);
            issue.updateFromIssueData(taskData);
            return issue;
        }

        @Override
        public void setIssueData(MantisIssue issue, IssueData taskData) {
            issue.updateFromIssueData(taskData);
        }

        @Override
        public String getRecentChanges(MantisIssue issue) {
            // @todo: Implement method correctly
            return "";
        }

        @Override
        public long getLastModified(MantisIssue issue) {
            return issue.getLast_updated().getTimeInMillis();
        }

        @Override
        public long getCreated(MantisIssue issue) {
            return issue.getDate_submitted().getTimeInMillis();
        }

        @Override
        public String getID(IssueData issueData) {
            assert issueData != null;
            if (issueData.getId() == null) {
                return null;
            } else {
                return issueData.getId().toString();
            }
        }

        @Override
        public Map<String, String> getAttributes(MantisIssue issue) {
            // @todo: Implement methodc correctly
            return Collections.EMPTY_MAP;
        }
    }
}
