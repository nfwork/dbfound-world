package com.dbfound.world.controlller;


import com.dbfound.world.entity.User;
import com.dbfound.world.service.UserService;
import com.nfwork.dbfound.dto.ResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping("/user/query")
    public ResponseObject query(@RequestParam(name = "user_code", required = false) String userCode,
                                @RequestParam(name = "user_name", required = false) String userName,
                                @RequestParam(name = "page_start", required = false) Integer start,
                                @RequestParam(name = "page_limit", required = false) Integer limit) {
        if(start == null){
            start = 0;
        }
        if(limit == null){
            limit = 10;
        }
        return userService.query(userCode,userName,start,limit);
    }

    @RequestMapping("/user/search")
    public ResponseObject search(@RequestParam Map<String,String> params) {
        return userService.search(params);
    }

    @RequestMapping("/user/update")
    public ResponseObject updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }
}

