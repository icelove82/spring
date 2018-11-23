package com.example.demo6;

import static com.zionex.t3series.web.ApplicationConstants.ICON_DEFAULT;
import static com.zionex.t3series.web.ApplicationConstants.PARENT_TOP;
import static com.zionex.t3series.web.ConfigurationConstants.ATTRIBUTE_DESCRIPTION;
import static com.zionex.t3series.web.ConfigurationConstants.ATTRIBUTE_ICON;
import static com.zionex.t3series.web.ConfigurationConstants.ATTRIBUTE_ID;
import static com.zionex.t3series.web.ConfigurationConstants.ATTRIBUTE_INIT_EXPAND;
import static com.zionex.t3series.web.ConfigurationConstants.ATTRIBUTE_OPEN;
import static com.zionex.t3series.web.ConfigurationConstants.ATTRIBUTE_PARENT;
import static com.zionex.t3series.web.ConfigurationConstants.ATTRIBUTE_PARENT_VIEW_GROUP;
import static com.zionex.t3series.web.ConfigurationConstants.ATTRIBUTE_SEQ;
import static com.zionex.t3series.web.ConfigurationConstants.ELEMENT_PUBLISH;
import static com.zionex.t3series.web.ConfigurationConstants.ELEMENT_VIEW;
import static com.zionex.t3series.web.ConfigurationConstants.ELEMENT_VIEW_GROUP;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jdom2.JDOMException;

import com.zionex.t3series.framework.ConfigurationException;
import com.zionex.t3series.framework.configuration.Configuration;
import com.zionex.t3series.framework.configuration.ConfigurationBuilder;
import com.zionex.t3series.util.FileUtil;
import com.zionex.t3series.util.NumberUtil;
import com.zionex.t3series.util.ObjectUtil;

public class ConfigurationManager {

    private static final ConfigurationManager configurationManager = new ConfigurationManager();
    private final Logger logger = Logger.getLogger("com.zionex.t3series.ui");
    private final List<ViewItem> groupViewList = new ArrayList<>();
    private final Map<String, Configuration> viewMap = new HashMap<>();
    private Configuration config;
    private String path;
    private ModificationDetector detector;

    private ConfigurationManager() {
    }

    static ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    private void init() {
        init(this.path);
    }

    public boolean init(String path) {
        if (path == null) {
            return false;
        }

        try {
            this.path = path;

            ConfigurationBuilder builder = new ConfigurationBuilder();
            this.config = builder.buildDir(this.path);
            if (this.detector != null) {
                this.detector.terminate();
            }
            this.detector = new ModificationDetector(this);
            this.detector.setDirectory(new File(path));

            // for performance
            viewMap.clear();
            Configuration[] configurations = config.getChildren(ELEMENT_VIEW);
            for (Configuration viewConfig : configurations) {
                String id = viewConfig.getAttribute(ATTRIBUTE_ID);
                viewMap.put(id, viewConfig);
            }

            if (logger.isLoggable(Level.INFO)) {
                logger.info("configuration manager init with " + path);
            }

            return true;

        } catch (JDOMException | IOException | ConfigurationException e) {
            e.printStackTrace();
            logger.warning("can't read configuration : " + e.getMessage());
            return false;
        }
    }

    public Configuration getConfig() {
        return config;
    }

    Configuration getViewConfig(String viewId) {
        if (config == null || viewId == null) {
            return null;
        }

        return viewMap.get(viewId);
    }

    Collection<String> getAllPublishedViews() {
        Collection<String> views = new ArrayList<>();
        detector.checkModification();

        Configuration[] configurations = config.getChildren(ELEMENT_VIEW);
        Configuration[] viewGroups = config.getChildren(ELEMENT_VIEW_GROUP);
        Map<String, Configuration> viewGroupMap = new HashMap<>();

        for (Configuration viewGroup : viewGroups) {
            viewGroupMap.put(viewGroup.getAttribute(ATTRIBUTE_ID), viewGroup);
        }

        for (Configuration viewConfig : configurations) {
            String id = viewConfig.getAttribute(ATTRIBUTE_ID);
            if (viewConfig.hasChild(ELEMENT_PUBLISH)) {
                Configuration publish = viewConfig.getChild(ELEMENT_PUBLISH);
                boolean open = publish.getAttributeAsBoolean(ATTRIBUTE_OPEN, true);
                String parentID = publish.getAttribute(ATTRIBUTE_PARENT);

                if (open) {
                    if (!StringUtils.isEmpty(parentID)) {
                        if (ancestorsOpen(parentID, viewGroupMap)) {
                            views.add(id);
                        }
                    } else {
                        views.add(id);
                    }
                }
            }
        }
        return views;
    }

    private boolean ancestorsOpen(String parentID, Map<String, Configuration> viewGroupMap) {
        Configuration viewGroup = viewGroupMap.get(parentID);
        if (viewGroup == null) {
            return false;
        }
        boolean open = viewGroup.getAttributeAsBoolean(ATTRIBUTE_OPEN, true);

        parentID = viewGroup.getAttribute(ATTRIBUTE_PARENT_VIEW_GROUP);
        if (!StringUtils.isEmpty(parentID)) {
            open = ancestorsOpen(parentID, viewGroupMap);
        }

        return open;
    }

    private ViewItem create(String id, Configuration config, Map<String, Boolean> menusByUser,
                            Map<String, Boolean> bookMarkedByUser, boolean checkAcceptance, String description) {

        boolean open = config.getAttributeAsBoolean(ATTRIBUTE_OPEN, true);
        if (checkAcceptance && !accept(id, menusByUser, open)) {
            return null;
        }

        int seq = NumberUtil.toInteger(config.getAttribute(ATTRIBUTE_SEQ));
        String icon = config.getAttribute(ATTRIBUTE_ICON, ICON_DEFAULT);
        boolean initExpand = ObjectUtil.toBoolean(config.getAttribute(ATTRIBUTE_INIT_EXPAND));

        boolean isBookmarked = false;
        if (bookMarkedByUser != null) {
            if (bookMarkedByUser.get(id) != null) {
                isBookmarked = bookMarkedByUser.get(id);
            }
        }

        return new ViewItem(id, seq, icon, !checkAcceptance, initExpand, isBookmarked, description);
    }

    private String getParent(Configuration config) {
        String parent = config.getAttribute(ATTRIBUTE_PARENT_VIEW_GROUP, PARENT_TOP);

        // remove below when all 'parent' are disappeared.
        if (!config.hasAttribute(ATTRIBUTE_PARENT_VIEW_GROUP)) {
            parent = config.getAttribute(ATTRIBUTE_PARENT, PARENT_TOP);
        }

        if (parent.trim().isEmpty()) {
            parent = PARENT_TOP;
        }
        return parent;
    }

    ViewItem getPublishedViewComposition(Map<String, Boolean> menusByUser, Map<String, Boolean> bookMarkedByUser) {
        detector.checkModification();

        ViewItem superView = new ViewItem("", 0, ICON_DEFAULT, true, true, false, "");

        Map<String, ViewItem> items = new HashMap<>();
        Map<String, String> parents = new HashMap<>();

        items.put(PARENT_TOP, superView);

        Configuration[] viewGroups = config.getChildren(ELEMENT_VIEW_GROUP);
        for (Configuration viewConfig : viewGroups) {
            boolean open = viewConfig.getAttributeAsBoolean(ATTRIBUTE_OPEN, true);

            if (!open) {
                continue;
            }

            String id = viewConfig.getAttribute(ATTRIBUTE_ID);
            ViewItem item = create(id, viewConfig, menusByUser, bookMarkedByUser, false, "");


            if (item != null) {
                items.put(id, item);
                parents.put(id, getParent(viewConfig));
            }
        }

        Configuration[] views = config.getChildren(ELEMENT_VIEW);
        for (Configuration viewConfig : views) {
            String id = viewConfig.getAttribute(ATTRIBUTE_ID);
            String description = viewConfig.hasAttribute(ATTRIBUTE_DESCRIPTION) ? viewConfig.getAttribute(ATTRIBUTE_DESCRIPTION) : "";
            if (viewConfig.hasChild(ELEMENT_PUBLISH)) {
                Configuration publish = viewConfig.getChild(ELEMENT_PUBLISH);
                ViewItem item = create(id, publish, menusByUser, bookMarkedByUser, true, description);
                if (item != null) {
                    items.put(id, item);
                    parents.put(id, getParent(publish));
                }
            }
        }

        for (Map.Entry<String, String> relation : parents.entrySet()) {
            String id = relation.getKey();
            String parent = relation.getValue();
            ViewItem viewItem = items.get(id);
            ViewItem parentViewItem = items.get(parent);

            if (viewItem != null && parentViewItem != null) {
                parentViewItem.addChild(viewItem);
            }
        }

        gatherGroupView(superView);

        while (!groupViewList.isEmpty()) {
            List<ViewItem> emptys = new ArrayList<>();

            for (ViewItem groupView : groupViewList) {
                if (groupView.children.isEmpty()) {
                    ViewItem parentViewItem = items.get(parents.get(groupView.id));
                    parentViewItem.children.remove(groupView);
                    emptys.add(groupView);
                }
            }

            groupViewList.removeAll(emptys);

            if (emptys.isEmpty()) {
                break;
            }
        }

        return superView;
    }

    private void gatherGroupView(ViewItem view) {
        for (ViewItem child : view.children) {
            if (child.isGroupView) {
                groupViewList.add(child);
                gatherGroupView(child);
            }
        }
    }

    private boolean accept(String id, Map<String, Boolean> menusByUser, boolean open) {
        Boolean accept = menusByUser.get(id);

        if (open && accept != null) {
            return accept;
        }
        return false;
    }

    public static class ViewItem implements Comparable<ViewItem> {
        private static final Logger logger = Logger.getLogger("com.zionex.t3series.ui");

        private final String id;
        private final String icon;
        private final int sequence;
        private final boolean isGroupView;
        private final boolean initExpand;
        private final boolean isBookmarked;
        private final String description;
        private final Set<ViewItem> children = new TreeSet<>();
        private int level;

        ViewItem(String id, int sequence, String icon, boolean isGroupView,
                boolean initExpand, boolean isBookmarked, String description) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("find menu with " + id + ", " + sequence + ", " + icon);
            }

            this.id = id;
            this.sequence = sequence;
            this.icon = icon;
            this.isGroupView = isGroupView;
            this.initExpand = initExpand;
            this.isBookmarked = isBookmarked;
            this.description = description;
        }

        int getLevel() {
            return level;
        }

        @Override
        public String toString() {
            return id;
        }

        ViewItem getChild(String id) {
            for (ViewItem child : this.children) {
                if (child.id.equals(id)) {
                    return child;
                }
            }
            return null;
        }

        void addChild(ViewItem child) {
            if (!this.children.contains(child)) {
                logger.fine(child.id + " added to " + this.id);
                this.children.add(child);
                child.level = level + 1;
            }
        }

        String toJsonString() {
            StringBuilder jsonBuilder = new StringBuilder();

            jsonBuilder.append("{\"id\":\"").append(id);
            jsonBuilder.append("\",\"text\":\"").append(id);
            jsonBuilder.append("\",\"level\":\"").append(level);
            jsonBuilder.append("\",\"icon\":\"").append(icon);
            jsonBuilder.append("\",\"seq\":\"").append(sequence);
            jsonBuilder.append("\",\"init-expand\":\"").append(initExpand);
            jsonBuilder.append("\",\"isbookmarked\":\"").append(isBookmarked);
            jsonBuilder.append("\",\"description\":\"").append(description);
            jsonBuilder.append("\",\"items\":[");

            for (ViewItem item : children) {
                jsonBuilder.append(item.toJsonString()).append(",");
            }

            if (children.size() > 0) {
                jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
            }

            jsonBuilder.append("]}");

            return jsonBuilder.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            ViewItem other = (ViewItem) obj;
            if (id == null) {
                return other.id == null;
            } else {
                return id.equals(other.id);
            }
        }

        @Override
        public int compareTo(ViewItem o) {
            if (this.equals(o)) {
                return 0;
            }

            int compare = NumberUtils.compare(this.sequence, o.sequence);
            if (compare != 0) {
                return compare;
            }

            compare = NumberUtils.compare(this.level, o.level);
            if (compare != 0) {
                return compare;
            }
            return this.id.compareTo(o.id);
        }
    }

    private static class ModificationDetector {
        private final Logger logger = Logger.getLogger("com.zionex.t3series.ui");
        private final ConfigurationManager manager;
        private File directory;
        private long basisTime;

        ModificationDetector(ConfigurationManager manager) {
            this.manager = manager;
        }

        void setDirectory(File dir) {
            if (dir == null) {
                logger.warning("observation target directory is invalid : null");
            }

            if (!dir.exists() || !dir.isDirectory()) {
                logger.warning("observation target directory is invalid : " + dir.getPath());
            }

            this.directory = dir;
            basisTime = readTime();
        }

        private long readTime() {
            if (directory == null || !directory.exists() || !directory.isDirectory()) {
                logger.warning("modification detector : directory is not specified yet.");
                return 0;
            }

            long time = 0;
            Collection<File> findFile = FileUtil.listFiles(directory, Integer.MAX_VALUE, pathname -> pathname.getName().endsWith("xml"));
            for (File file : findFile) {
                long each = file.lastModified();
                if (each > time) {
                    time = each;
                }
            }
            return time;
        }

        void terminate() {
            logger.info("terminate modification detector..");
        }

        void checkModification() {
            long time = readTime();
            if (manager != null && basisTime < time) {
                logger.info("detect modification. try to reload configuration..");
                manager.init();
            }
        }

    }
}
