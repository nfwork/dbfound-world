package com.dbfound.world;

import com.nfwork.dbfound.core.Context;
import com.nfwork.dbfound.dto.QueryResponseObject;
import com.nfwork.dbfound.model.adapter.ExecuteAdapter;
import com.nfwork.dbfound.model.adapter.QueryAdapter;
import com.nfwork.dbfound.model.bean.Param;
import com.nfwork.dbfound.util.DataUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class UserAdapter implements ExecuteAdapter, QueryAdapter {

    @Override
    public void beforeExecute(Context context, Map<String, Param> params) {
        if(params.get("user_code").getValue()==null){
            params.get("user_code").setValue("xiaoming");
        }
    }

    @Override
    public void afterExecute(Context context, Map<String, Param> params) {
        context.setOutParamData("user_code",params.get("user_code").getValue());
    }

    @Override
    public void beforeQuery(Context context, Map<String, Param> params) {
    }

    @Override
    public void afterQuery(Context context, Map<String, Param> params, QueryResponseObject responseObject) {
        List<Map<String,Object>> dataList = responseObject.getDatas();

        for (Map<String,Object> data : dataList){
            if(DataUtil.isNotNull(data.get("tags"))){
                String[] tags = data.get("tags").toString().split(",");
                data.put("tags",tags);
            }else{
                data.put("tags",null);
            }
        }
    }
}
