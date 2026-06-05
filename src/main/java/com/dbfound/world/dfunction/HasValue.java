package com.dbfound.world.dfunction;

import com.nfwork.dbfound.core.Context;
import com.nfwork.dbfound.db.dialect.SqlDialect;
import com.nfwork.dbfound.el.ELEngine;
import com.nfwork.dbfound.model.dsql.DSqlFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class HasValue extends DSqlFunction {

    @PostConstruct
    public void init() {
        register("has_value");
    }

    @PreDestroy
    public void destroy() {
        unRegister();
    }

    @Override
    public Object apply(List<Object> params, SqlDialect sqlDialect) {
        if (params.isEmpty() || params.get(0) == null) {
            return false;
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return false;
        }

        Context context = Context.getCurrentContext(attributes.getRequest(), attributes.getResponse());
        String path = resolvePath(params.get(0).toString(), context);
        Object value = context.getData(path);
        return hasValue(value);
    }

    private String resolvePath(String path, Context context) {
        path = path.trim();
        if (ELEngine.isAbsolutePath(path)) {
            return path;
        }else {
            String currentPath = context.getCurrentPath();
            return currentPath + "." + path;
        }
    }

    private boolean hasValue(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof String) {
            return !((String) value).isEmpty();
        }
        if (value instanceof Collection) {
            return !((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map) {
            return !((Map<?, ?>) value).isEmpty();
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value) > 0;
        }
        return true;
    }
}
