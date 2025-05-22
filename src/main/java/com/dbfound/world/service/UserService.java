package com.dbfound.world.service;

import com.dbfound.world.entity.User;
import com.github.nfwork.dbfound.starter.ModelExecutor;
import com.nfwork.dbfound.core.Context;
import com.nfwork.dbfound.dto.ResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {

    @Autowired
    ModelExecutor modelExecutor;

    public ResponseObject query(String userCode, String userName, int start, int limit) {
        Context context = new Context()
                .withParam("user_code", userCode)
                .withParam("user_name", userName)
                .withPageStart(start)
                .withPageLimit(limit);
        return modelExecutor.query(context, "user", "");
    }

    public ResponseObject search(Map<String,String> params) {
        Context context = new Context().withMapParam(params);
        return modelExecutor.query(context, "user", "");
    }

    public ResponseObject updateUser(User user) {
        Context context = new Context()
                .withBeanParam(user)
                .withParam("last_update_by", 1);
        ResponseObject responseObject = modelExecutor.execute(context, "user", "update");
        int updateNum = responseObject.getOutParam("update_num");
        if (updateNum == 0) {
            responseObject.setSuccess(false);
            responseObject.setMessage("没有找到相应的记录");
        }
        return responseObject;
    }
}
