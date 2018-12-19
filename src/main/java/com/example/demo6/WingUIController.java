package com.example.demo6;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zionex.t3series.data.DataHandlerException;
import com.zionex.t3series.util.ObjectUtil;
import com.zionex.t3series.util.time.TimeStamp;
import com.zionex.t3simpleserver.data.DataSelectionQuery;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WingUIController {

    private static final JSONObject languageObj = new JSONObject();

    @RequestMapping("/getmenubadge")
    public JSONObject getMenuBadge() {
        return getData();
    }

    @RequestMapping("/getmenu")
    public JSONObject getMenu() {
        return getData2();
    }

    public JSONObject getData() {
        DataSourceBridge dataSourceBridge = DataSourceBridge.getDataSourceBridge();
        dataSourceBridge.init("C:\\Users\\yunmy\\VsProjects\\demo6\\configs");

        JSONObject result = new JSONObject();
        JSONArray listItem = new JSONArray();

        DataSelectionQuery dataSelectionQuery = DataSourceBridge.getDataSourceBridge().createDataSelectionQuery(null, "GetMenuBadge", null);

        try {
            if (dataSelectionQuery.make(null, DataSourceBridge.getDataSourceBridge().getFetchSize())) {
                List<Object[]> data = dataSelectionQuery.data();

                for (Object[] row : data) {
                    String menuId = ObjectUtil.toString(row[0]);
                    String badgeContent = ObjectUtil.toString(row[1]);
                    TimeStamp expiredTime = new TimeStamp(ObjectUtil.toDate(row[2]));

                    JSONObject item = new JSONObject();
                    item.put("MENU_ID", menuId);
                    item.put("BADGE_CONTENT", badgeContent);
                    item.put("EXPIRED_TIME", expiredTime.toString());

                    listItem.add(item);
                }
                result.put("items", listItem);
            }
        } catch (DataHandlerException e) {
            e.printStackTrace();
            //logger.warning(e.getMessage());
        }

        return result;
    }

    private JSONObject getData2() {
        String languageCode = "KR";
        
        JSONObject resultLanguageObj;
        // if (languageObj.containsKey(languageCode)) {
        //     resultLanguageObj = new JSONObject();
        //     resultLanguageObj.put(languageCode, languageObj.get(languageCode));
        // } else {
            resultLanguageObj = getLanguage(languageCode);
        // }

        return resultLanguageObj;
    }

    public static JSONObject getLanguage(String languageCode) {
        Map<String, Object> contents = new HashMap<>();
        contents.put("LOCALE", languageCode);

        DataSourceBridge dataSourceBridge = DataSourceBridge.getDataSourceBridge();
        dataSourceBridge.init("C:\\Users\\yunmy\\VsProjects\\demo6\\configs");

        DataSelectionQuery dataSelectionQuery = DataSourceBridge.getDataSourceBridge().createDataSelectionQuery("GetLanguages", contents);

        JSONObject resultLanguageObj = new JSONObject();
        try {
            if (dataSelectionQuery.make(null, DataSourceBridge.getDataSourceBridge().getFetchSize())) {
                List<Object[]> data = dataSelectionQuery.data();

                for (Object[] row : data) {
                    String localeCode = (String) row[0];
                    String langCode = (String) row[1];
                    String langValue = (String) row[2];

                    JSONObject localeObj = (JSONObject) resultLanguageObj.get(localeCode);
                    if (localeObj == null) {
                        localeObj = new JSONObject();
                        resultLanguageObj.put(localeCode, localeObj);
                    }
                    localeObj.put(langCode, langValue);
                }
            }
        } catch (DataHandlerException e) {
            e.printStackTrace();
            //logger.warning(e.getMessage());
        }

        for (Object localeCode : resultLanguageObj.keySet()) {
            languageObj.put(localeCode, resultLanguageObj.get(localeCode));
        }
        return resultLanguageObj;
    }

}