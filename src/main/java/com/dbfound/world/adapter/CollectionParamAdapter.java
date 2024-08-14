package com.dbfound.world.adapter;

import com.nfwork.dbfound.core.Context;
import com.nfwork.dbfound.model.adapter.ExecuteAdapter;
import com.nfwork.dbfound.model.adapter.ObjectQueryAdapter;
import com.nfwork.dbfound.model.base.DataType;
import com.nfwork.dbfound.model.bean.Param;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 集合参数适配器，让集合参数支持(分号、空格、回车、退格)等分割府；
 * 默认情况下 只支持英文分隔的字符串
 */
@Component
public class CollectionParamAdapter implements ObjectQueryAdapter, ExecuteAdapter {

    @Override
    public void beforeQuery(Context context, Map<String, Param> params) {
        initParam(params);
    }

    @Override
    public void beforeExecute(Context context, Map<String, Param> params) {
        initParam(params);
    }

    private void initParam(Map<String, Param> params){
        for(Param param : params.values()){
            if(param.getDataType() == DataType.COLLECTION && param.getValue() instanceof String){
                String value = param.getStringValue();
                value = value.replaceAll("[\\s,;]",",");
                param.setValue(value);
            }
        }
    }
}
