package eu.doppel_helix.netbeans.mantisintegration.repository;

import biz.futureware.mantisconnect.AccountData;
import biz.futureware.mantisconnect.CustomFieldDefinitionData;
import biz.futureware.mantisconnect.FilterData;
import biz.futureware.mantisconnect.MantisConnectPortType;
import biz.futureware.mantisconnect.ObjectRef;
import biz.futureware.mantisconnect.ProjectData;
import biz.futureware.mantisconnect.ProjectVersionData;
import biz.futureware.mantisconnect.TagData;
import eu.doppel_helix.netbeans.mantisintegration.util.StringUtils;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.rpc.ServiceException;

public class MasterData {
    private static final Logger LOG = Logger.getLogger(MasterData.class.getName());

    private final MantisRepository mr;

    private ObjectRef[] priorities;
    private ObjectRef[] states;
    private ObjectRef[] resolutions;
    private ObjectRef[] reproducibilities;
    private ObjectRef[] viewStates;
    private ObjectRef[] severities;
    private ObjectRef[] projections;
    private ObjectRef[] etas;
    private ObjectRef[] customFieldTypes;
    private HashMap<BigInteger, ProjectData> projects;
    private HashMap<BigInteger, TagData> tags;
    // Hardcoded, as no api exists to retrieve this info
    private final ObjectRef[] relationships = new ObjectRef[]{
        new ObjectRef(new BigInteger("0"), "duplicate of"),
        new ObjectRef(new BigInteger("1"), "related to"),
        new ObjectRef(new BigInteger("2"), "parent of"),
        new ObjectRef(new BigInteger("3"), "child of"),
        new ObjectRef(new BigInteger("4"), "has duplicate"),};
    private final HashMap<BigInteger, String[]> categories = new HashMap<>();
    private final HashMap<BigInteger, AccountData[]> users = new HashMap<>();
    private final HashMap<BigInteger, ProjectVersionData[]> versions = new HashMap<>();
    private final HashMap<BigInteger, FilterData[]> filters = new HashMap<>();
    private final HashMap<BigInteger, CustomFieldDefinitionData[]> customFieldDefinitions = new HashMap<>();

    public MasterData(MantisRepository mr) {
        this.mr = mr;
    }

    public ObjectRef[] getPriorities() throws ServiceException, RemoteException {
        if (priorities == null) {
            MantisConnectPortType mcpt = mr.getClient();
            priorities = mcpt.mc_enum_priorities(
                    mr.getInfo().getUsername(),
                    new String(mr.getInfo().getPassword()));
        }
        return priorities;
    }

    public ObjectRef[] getRelationships() {
        return relationships;
    }

    public ObjectRef[] getStates() throws ServiceException, RemoteException {
        if (states == null) {
            MantisConnectPortType mcpt = mr.getClient();
            states = mcpt.mc_enum_status(
                    mr.getInfo().getUsername(),
                    new String(mr.getInfo().getPassword()));
        }
        return states;
    }

    public ObjectRef[] getResolutions() throws RemoteException, ServiceException {
        if (resolutions == null) {
            MantisConnectPortType mcpt = mr.getClient();
            ObjectRef[] resolutionsBuiltin = mcpt.mc_enum_resolutions(
                    mr.getInfo().getUsername(),
                    new String(mr.getInfo().getPassword()));
            
            ObjectRef[] res2 = new ObjectRef[resolutionsBuiltin.length + 1];
            System.arraycopy(resolutionsBuiltin, 0, res2, 0, resolutionsBuiltin.length);
            res2[resolutionsBuiltin.length] = new ObjectRef(new BigInteger("-1"), "test");
            resolutions = res2;
        }
        
        return resolutions;
    }

    public ObjectRef[] getReproducibilities() throws ServiceException, RemoteException {
        if (reproducibilities == null) {
            MantisConnectPortType mcpt = mr.getClient();
            reproducibilities = mcpt.mc_enum_reproducibilities(
                    mr.getInfo().getUsername(),
                    new String(mr.getInfo().getPassword()));
        }
        return reproducibilities;
    }

    public ObjectRef[] getViewStates() throws ServiceException, RemoteException {
        if (viewStates == null) {
            MantisConnectPortType mcpt = mr.getClient();
            viewStates = mcpt.mc_enum_view_states(
                    mr.getInfo().getUsername(),
                    new String(mr.getInfo().getPassword()));
        }
        return viewStates;
    }

    public ObjectRef[] getSeverities() throws ServiceException, RemoteException {
        if (severities == null) {
            MantisConnectPortType mcpt = mr.getClient();
            severities = mcpt.mc_enum_severities(
                    mr.getInfo().getUsername(),
                    new String(mr.getInfo().getPassword()));
        }
        return severities;
    }

    public ObjectRef[] getProjections() throws ServiceException, RemoteException {
        if (projections == null) {
            MantisConnectPortType mcpt = mr.getClient();
            projections = mcpt.mc_enum_projections(
                    mr.getInfo().getUsername(),
                    new String(mr.getInfo().getPassword()));
        }
        return projections;
    }

    public ObjectRef[] getEtas() throws ServiceException, RemoteException {
        if (etas == null) {
            MantisConnectPortType mcpt = mr.getClient();
            etas = mcpt.mc_enum_etas(
                    mr.getInfo().getUsername(),
                    new String(mr.getInfo().getPassword()));
        }
        return etas;
    }

    private void initProjectList() throws ServiceException, RemoteException {
        if (projects == null) {
            MantisConnectPortType mcpt = mr.getClient();
            projects = new HashMap<>();
            ProjectData[] projectList = mcpt.mc_projects_get_user_accessible(
                    mr.getInfo().getUsername(),
                    new String(mr.getInfo().getPassword()));
            for (ProjectData pd : projectList) {
                projects.put(pd.getId(), pd);
            }
        }
    }

    public ProjectData getProject(BigInteger id) throws ServiceException, RemoteException {
        initProjectList();
        return projects.get(id);
    }

    public ProjectData[] getProjects() throws ServiceException, RemoteException {
        initProjectList();
        Collection<ProjectData> projectCollection = projects.values();
        return projectCollection.toArray(new ProjectData[projectCollection.size()]);
    }

    public String[] getCategories(BigInteger projectID) throws ServiceException, RemoteException {
        String[] result = categories.get(projectID);
        if (result == null) {
            MantisConnectPortType mcpt = mr.getClient();
            if (BigInteger.ZERO.equals(projectID)) {
                TreeSet<String> categoriesSet = new TreeSet<>();
                for (ProjectData pd : getProjects()) {
                    String[] projCategories = mcpt.mc_project_get_categories(
                            mr.getInfo().getUsername(),
                            new String(mr.getInfo().getPassword()),
                            pd.getId());
                    categoriesSet.addAll(Arrays.asList(projCategories));
                }
                categories.put(projectID, categoriesSet.toArray(new String[0]));
            } else {
                String[] projCategories = mcpt.mc_project_get_categories(
                        mr.getInfo().getUsername(),
                        new String(mr.getInfo().getPassword()),
                        projectID);
                categories.put(projectID, projCategories);
            }
        }
        return categories.get(projectID);
    }

    public AccountData[] getUsers(BigInteger projectID) throws ServiceException, RemoteException {
        if (users.get(projectID) == null) {
            MantisConnectPortType mcpt = mr.getClient();
            try {
                AccountData[] projCategories = mcpt.mc_project_get_users(
                        mr.getInfo().getUsername(),
                        new String(mr.getInfo().getPassword()),
                        projectID,
                        BigInteger.ONE);
                users.put(projectID, projCategories);
            } catch (RemoteException ex) {
                // @todo: Find a sane solution for the problem where the API
                //        limits the usefulness (=> here mantis fails to return
                //        user counts > approx. 15000 Users)
                LOG.log(Level.INFO, "", ex);
                users.put(projectID, new AccountData[0]);
            }
        }
        AccountData[] result = users.get(projectID);
        if (result == null) {
            result = new AccountData[0];
        }
        return result;
    }

    public ProjectVersionData[] getVersions(BigInteger projectID) throws ServiceException, RemoteException {
        if (versions.get(projectID) == null) {
            MantisConnectPortType mcpt = mr.getClient();
            try {
                ProjectVersionData[] projVersions = mcpt.mc_project_get_versions(
                        mr.getInfo().getUsername(),
                        new String(mr.getInfo().getPassword()),
                        projectID);
                Arrays.sort(projVersions, new ProjectVersionDataComparator());
                versions.put(projectID, projVersions);
            } catch (RemoteException ex) {
                LOG.log(Level.INFO, "", ex);
                versions.put(projectID, new ProjectVersionData[0]);
            }
        }
        ProjectVersionData[] result = versions.get(projectID);
        if (result == null) {
            result = new ProjectVersionData[0];
        }
        return result;
    }

    public FilterData[] getFilters(BigInteger projectID) throws ServiceException, RemoteException {
        if (filters.get(projectID) == null) {
            MantisConnectPortType mcpt = mr.getClient();
            FilterData[] fd = mcpt.mc_filter_get(
                    mr.getInfo().getUsername(),
                    new String(mr.getInfo().getPassword()),
                    projectID);
            filters.put(projectID, fd);
        }
        return filters.get(projectID);
    }

    public ObjectRef[] getCustomFieldTypes(boolean enforceUpdate) throws ServiceException, RemoteException {
        if (enforceUpdate) {
            customFieldTypes = null;
        }
        if (customFieldTypes == null) {
            MantisConnectPortType mcpt = mr.getClient();
            customFieldTypes = mcpt.mc_enum_custom_field_types(
                    mr.getInfo().getUsername(),
                    new String(mr.getInfo().getPassword()));
        }
        return customFieldTypes;
    }

    public CustomFieldDefinitionData[] getCustomFieldDefinitions(BigInteger projectID) throws ServiceException, RemoteException {
        if (customFieldDefinitions.get(projectID) == null) {
            MantisConnectPortType mcpt = mr.getClient();
            CustomFieldDefinitionData[] cfds = mcpt.mc_project_get_custom_fields(
                    mr.getInfo().getUsername(),
                    new String(mr.getInfo().getPassword()),
                    projectID);
            for (CustomFieldDefinitionData cfd : cfds) {
                if (cfd.getPossible_values().startsWith("=")) {
                    String enumName = cfd.getPossible_values().substring(1).trim();
                    switch (enumName) {
                        case "versions":
                            cfd.setPossible_values(customFieldEnumVersions(projectID));
                            break;
                        case "future_versions":
                            cfd.setPossible_values(customFieldEnumVersionsReleased(projectID, false));
                            break;
                        case "released_versions":
                            cfd.setPossible_values(customFieldEnumVersionsReleased(projectID, true));
                            break;
                        case "categories":
                            cfd.setPossible_values(customFieldEnumCategories(projectID));
                            break;
                        default:
                            cfd.setType(BigInteger.valueOf(-1));
                            LOG.log(Level.WARNING, "Failed to calculate dynamic custom field mr.getInfo(). {0}", enumName);
                    }
                }
            }
            customFieldDefinitions.put(projectID, cfds);
        }
        return customFieldDefinitions.get(projectID);
    }

    /**
     * Get a custom field definition by id.
     *
     * The function only accesses cached values, so it is save to be called on
     * the EDT.
     *
     * The idea: this is used to translate error messages when updating/setting
     * custom fields. At this point the definition has to be fetched already.
     *
     * @param id of custom field definition
     * @return the corresponding definition or null if definition was not found
     */
    public CustomFieldDefinitionData getCustomFieldDefinition(BigInteger id) {
        Iterator<CustomFieldDefinitionData[]> it = customFieldDefinitions.values().iterator();
        while (it.hasNext()) {
            CustomFieldDefinitionData[] cfds = it.next();
            for (CustomFieldDefinitionData cfd : cfds) {
                if (id.equals(cfd.getField().getId())) {
                    return cfd;
                }
            }
        }
        return null;
    }

    private String customFieldEnumCategories(BigInteger projectID) throws ServiceException, RemoteException {
        return StringUtils.toCustomFieldList(getCategories(projectID));
    }

    private String customFieldEnumVersions(BigInteger projectID) throws ServiceException, RemoteException {
        List<String> list = new ArrayList<>();
        for (ProjectVersionData pvd : getVersions(projectID)) {
            list.add(pvd.getName());
        }
        Collections.sort(list);
        return StringUtils.toCustomFieldList(list);
    }

    private String customFieldEnumVersionsReleased(BigInteger projectID, boolean released) throws ServiceException, RemoteException {
        List<String> list = new ArrayList<>();
        for (ProjectVersionData pvd : getVersions(projectID)) {
            if ((released && pvd.getReleased()) || ((!released)
                    && (!pvd.getReleased()))) {
                list.add(pvd.getName());
            }
        }
        Collections.sort(list);
        return StringUtils.toCustomFieldList(list);
    }
    
    private void initTags(boolean enforceUpdate) throws ServiceException, RemoteException {
        if (enforceUpdate) {
            tags = null;
        }
        if (tags == null) {
            tags = new HashMap<>();
            MantisConnectPortType mcpt = mr.getClient();
            BigInteger PAGE_SIZE = BigInteger.valueOf(1000);
            for (int i = 0; i < 1000; i++) {
                TagData[] tagList = mcpt.mc_tag_get_all(
                        mr.getInfo().getUsername(),
                        new String(mr.getInfo().getPassword()),
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
    
    public TagData getTag(BigInteger id) throws ServiceException, RemoteException {
        initTags(false);
        return tags.get(id);
    }

    public List<TagData> getTags() throws ServiceException, RemoteException {
        initTags(false);
        return new ArrayList<>(tags.values());
    }
    
    public TagData addTag(String tagName) throws ServiceException, RemoteException {
        for (TagData td : getTags()) {
            if (td.getName().equals(tagName)) {
                return td;
            }
        }

        TagData newTag = new TagData();
        newTag.setDescription("");
        newTag.setName(tagName);
        BigInteger id = mr.getClient().mc_tag_add(
                mr.getInfo().getUsername(),
                new String(mr.getInfo().getPassword()),
                newTag);
        newTag.setId(id);
        tags.put(id, newTag);
        return newTag;
    }
}
