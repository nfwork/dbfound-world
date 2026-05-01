package com.dbfound.world.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.nfwork.dbfound.dto.ResponseObject;
import com.nfwork.dbfound.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.time.temporal.Temporal;
import java.util.Date;

@Configuration
public class JacksonConfig {

    @Autowired
    ObjectMapper objectMapper;

    @PostConstruct
    public void objectMapper() {
        SimpleModule module = new SimpleModule();
        //ResponseObjectSerializer的作用主要是为了适配dbfoundui，让outParam和totalCounts不转化为下划线，非dbfoundui项目不用配置
        //module.addSerializer(ResponseObject.class, new JsonUtil.ResponseObjectSerializer());
        module.addSerializer(Temporal.class, new JsonUtil.TemporalSerializer());
        module.addSerializer(Enum.class, new JsonUtil.EnumSerializer());
        module.addSerializer(Date.class, new JsonUtil.DateSerializer());
        objectMapper.registerModule(module);
    }
}

