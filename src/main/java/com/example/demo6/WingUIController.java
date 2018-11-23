package com.example.demo6;

import java.util.List;

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

    @RequestMapping("/getmenu")
    public JSONObject getMenu() {
        return getData();
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
}