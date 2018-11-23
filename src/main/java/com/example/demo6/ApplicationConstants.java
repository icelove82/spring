package com.example.demo6;

public interface ApplicationConstants {

    String SERVER_ID                    = "T3SeriesWingUI";
    String SERVER_EVALUATE_LICENSE      = "EvaluateLicense";
    String PRODUCT                      = "T3WingUI";
    String VERSION                      = "1.0";

    String PATH_HOME                    = "/WEB-INF";
    String PATH_CONFIGS                 = "/WEB-INF/configs";
    String PATH_VIEWS                   = "/WEB-INF/views";

    String FILE_ADAPTOR                 = "/configs/configuration_adaptor.xml";

    String CONTENT_TYPE_HTML            = "text/html;charset=UTF-8";
    String CONTENT_TYPE_XML             = "text/xml;charset=UTF-8";
    String CONTENT_TYPE_JSON            = "text/json;charset=UTF-8";
    String CONTENT_TYPE_APP             = "application/json";

    String ICON_DEFAULT                 = "default";
    String PARENT_TOP                   = "###super";

    String ADMINISTRATOR                = "administrator";
    String LOCALE                       = "LOCALE";
    String VIEW_ID                      = "viewId";
    String DATA_SOURCE_ID               = "DATA_SOURCE_ID";

    /**
     * simple and general data
     */
    String PARAMETER_KEY_DATA           = "RESULT_DATA";
    String PARAMETER_KEY_DATA_ORIGINAL  = "RESULT_DATA_ORIGINAL";
    String PARAMETER_KEY_USER_ID        = "USER_ID";
    String PARAMETER_KEY_MENU_ID        = "MENU_ID";
    String PARAMETER_KEY_GROUP_CODE     = "GRP_CD";

    String SESSION_KEY_USER_ID          = "USER_INFO_ID";
    String SESSION_KEY_USER_NAME        = "USER_INFO_NAME";

    String REQUEST_KEY_TREE_KEY_ID      = "TREE_KEY_ID";
    String REQUEST_KEY_TREE_PARENT_ID   = "TREE_PARENT_ID";
    String REQUEST_KEY_SERVICE          = "service";
    String REQUEST_KEY_TARGET           = "target";
    String REQUEST_KEY_TIMEOUT_SEC      = "timeout";
    String REQUEST_KEY_UI_SERVICE       = "ui_service";

    String REQUEST_SESSION_SERVICE      = "session_service";
    String REQUEST_SESSION_INFO_ID      = "session_info_id";
    String REQUEST_SESSION_INFO_VALUE   = "session_info_value";
    String REQUEST_FORCE_SAVE           = "FORCE_SAVE";

    String SERVICE_LOGIN                = "Login";
    String SERVICE_LOGOUT               = "Logout";

    String SERVER_AUTH                  = "AuthServer";

    String UPLOAD_ROOT                  = "/upload";
    String TEMPLATE_ROOT                = "template/";

    static String getMajorVersion() {
    	return VERSION.split("\\.")[0];
    }
}