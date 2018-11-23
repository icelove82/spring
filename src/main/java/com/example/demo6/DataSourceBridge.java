package com.example.demo6;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.JDOMException;

import com.zionex.t3series.data.DataHandlerException;
import com.zionex.t3series.data.JdbcDataHandler;
import com.zionex.t3series.framework.ConfigurationException;
import com.zionex.t3series.framework.configuration.Configuration;
import com.zionex.t3series.framework.configuration.ConfigurationBuilder;
import com.zionex.t3simpleserver.data.DataMergenceQuery;
import com.zionex.t3simpleserver.data.DataSelectionQuery;
import com.zionex.t3simpleserver.data.DataSourceConfigurationReader;
import com.zionex.t3simpleserver.data.DataSourceManager;

public class DataSourceBridge {
    private final Logger logger = Logger.getLogger("com.zionex.t3series.ui");
    private final DataSourceManager dataSourceManager = new DataSourceManager();

    private boolean initialized = false;
    private int fetchSize = 0;

    private DataSourceBridge() {
    }

    public static DataSourceBridge getDataSourceBridge() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        private static final DataSourceBridge INSTANCE = new DataSourceBridge();
    }

    public void init(String configPath) {
        if (!initialized) {
            Configuration configuration = getConfiguration(configPath);

            Map<String, Object> map = configuration.toMap();
            DataSourceConfigurationReader.configureDataSources(map, dataSourceManager);
            DataSourceConfigurationReader.configureQuery(map, dataSourceManager);

            initialized = true;
        }
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    @SuppressWarnings("unchecked")
    public DataSelectionQuery createDataSelectionQuery(String queryId, Map<String, Object>... param) {
        JdbcDataHandler datasource = dataSourceManager.getDefaultDatasource();
        return dataSourceManager.createDataSelectionQuery(datasource, dataSourceManager.getQuery(queryId, datasource), param);
    }

    @SuppressWarnings("unchecked")
    public DataSelectionQuery createDataSelectionQuery(String datasourceId, String queryId, Map<String, Object>... param) {
        JdbcDataHandler datasource;
        if (StringUtils.isEmpty(datasourceId)) {
            datasource = dataSourceManager.getDefaultDatasource();
        } else {
            datasource = dataSourceManager.getDataSource(datasourceId);
        }
        return dataSourceManager.createDataSelectionQuery(datasource, dataSourceManager.getQuery(queryId, datasource), param);
    }

    @SuppressWarnings("unchecked")
    public DataMergenceQuery createDataMergenceQuery(String queryId, Map<String, Object> param) {
        JdbcDataHandler datasource = dataSourceManager.getDefaultDatasource();
        return dataSourceManager.createDataMergenceQuery(datasource, dataSourceManager.getQuery(queryId, datasource), null, param);
    }

    @SuppressWarnings("unchecked")
    public DataMergenceQuery createDataMergenceQuery(String datasourceId, String queryId, Map<String, Object> param) {
        JdbcDataHandler datasource;
        if (StringUtils.isEmpty(datasourceId)) {
            datasource = dataSourceManager.getDefaultDatasource();
        } else {
            datasource = dataSourceManager.getDataSource(datasourceId);
        }
        return dataSourceManager.createDataMergenceQuery(datasource, dataSourceManager.getQuery(queryId, datasource), null, param);
    }

    @SuppressWarnings("unchecked")
    public DataMergenceQuery createDataMergenceQuery(String queryId, List<Map<String, Object>> param) {
        JdbcDataHandler datasource = dataSourceManager.getDefaultDatasource();
        return dataSourceManager.createDataMergenceQuery(datasource, dataSourceManager.getQuery(queryId, datasource), param);
    }

    @SuppressWarnings("unchecked")
    public DataMergenceQuery createDataMergenceQuery(String datasourceId, String queryId, List<Map<String, Object>> param) {
        JdbcDataHandler datasource;
        if (StringUtils.isEmpty(datasourceId)) {
            datasource = dataSourceManager.getDefaultDatasource();
        } else {
            datasource = dataSourceManager.getDataSource(datasourceId);
        }
        return dataSourceManager.createDataMergenceQuery(datasource, dataSourceManager.getQuery(queryId, datasource), param);
    }

    public List<Object[]> getDataWithMeta(String queryId) throws DataHandlerException {
        return getDataWithMeta(dataSourceManager.getDefaultDatasource().getDataHandlerId(), queryId, 0);
    }

    public List<Object[]> getDataWithMeta(String queryId, int fetchSize) throws DataHandlerException {
        return getDataWithMeta(dataSourceManager.getDefaultDatasource().getDataHandlerId(), queryId, fetchSize);
    }

    public List<Object[]> getDataWithMeta(String datasourceId, String queryId) throws DataHandlerException {
        return getDataWithMeta(datasourceId, queryId, 0);
    }

    public List<Object[]> getDataWithMeta(String datasourceId, String queryId, int fetchSize) throws DataHandlerException {
        JdbcDataHandler datasource;
        if (StringUtils.isEmpty(datasourceId)) {
            datasource = dataSourceManager.getDefaultDatasource();
        } else {
            datasource = dataSourceManager.getDataSource(datasourceId);
        }
        return datasource.getDataWithMeta(dataSourceManager.getQuery(queryId, datasource), fetchSize > 0 ? fetchSize : this.fetchSize);
    }

    public List<Object[]> getData(String queryId) throws DataHandlerException {
        return getData(dataSourceManager.getDefaultDatasource().getDataHandlerId(), queryId);
    }

    public List<Object[]> getData(String datasourceId, String queryId) throws DataHandlerException {
        JdbcDataHandler datasource;
        if (StringUtils.isEmpty(datasourceId)) {
            datasource = dataSourceManager.getDefaultDatasource();
        } else {
            datasource = dataSourceManager.getDataSource(datasourceId);
        }
        return datasource.getData(dataSourceManager.getQuery(queryId, datasource));
    }

    private Configuration getConfiguration(String configPath) {
        try {
            return new ConfigurationBuilder().buildDir(configPath);
        } catch (JDOMException | IOException | ConfigurationException e) {
            e.printStackTrace();
            logger.warning(e.getMessage());
        }
        return null;
    }
}
