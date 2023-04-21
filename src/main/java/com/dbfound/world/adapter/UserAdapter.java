package com.dbfound.world.adapter;

import com.dbfound.world.entity.User;
import com.nfwork.dbfound.core.Context;
import com.nfwork.dbfound.dto.QueryResponseObject;
import com.nfwork.dbfound.model.adapter.ExecuteAdapter;
import com.nfwork.dbfound.model.adapter.QueryAdapter;
import com.nfwork.dbfound.model.base.Count;
import com.nfwork.dbfound.model.bean.Param;
import com.nfwork.dbfound.util.DataUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class UserAdapter implements ExecuteAdapter, QueryAdapter<User> {

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
    public void beforeCount(Context context, Map<String, Param> params, Count count) {

    }

    @Override
    public void afterQuery(Context context, Map<String, Param> params, QueryResponseObject<User> responseObject) {
        List<User> dataList = responseObject.getDatas();

        for (User user : dataList){
            if(DataUtil.isNotNull(user.getTags())){
                String[] tags = user.getTags().split(",");
                user.setTagArray(tags);
            }
        }
    }
}
