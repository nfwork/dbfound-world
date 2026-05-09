package com.dbfound.world.adapter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nfwork.dbfound.core.Context;
import com.nfwork.dbfound.dto.QueryResponseObject;
import com.nfwork.dbfound.model.adapter.MapQueryAdapter;
import com.nfwork.dbfound.model.bean.Param;
import com.nfwork.dbfound.util.LogUtil;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class UserInfoCacheAdapter implements MapQueryAdapter {

    private final Cache<String, QueryResponseObject<Map<String, Object>>> userInfoCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .initialCapacity(50)
            .maximumSize(500)
            .build();
    @Override
    public QueryResponseObject<Map<String, Object>> handleQuery(Context context, Map<String, Param> params) {
        String userId = context.getString("param.user_id");
        QueryResponseObject<Map<String, Object>> info = userInfoCache.getIfPresent(userId);
        if(info!=null){
            LogUtil.info("get user info from cache, user_id: " + userId);
        }
        // 如果info不为空，将直接返回info，query停止执行
        return info;
    }

    @Override
    public void afterQuery(Context context, Map<String, Param> params, QueryResponseObject<Map<String, Object>> responseObject) {
        String userId = context.getString("param.user_id");
        userInfoCache.put(userId, responseObject);
    }
}
