package com.dbfound.world.adapter;

import com.dbfound.world.enums.UserField;
import com.nfwork.dbfound.core.Context;
import com.nfwork.dbfound.model.adapter.QueryAdapter;
import com.nfwork.dbfound.model.bean.Param;
import com.nfwork.dbfound.model.enums.EnumHandlerFactory;
import com.nfwork.dbfound.model.enums.EnumTypeHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class UserPartAdapter implements QueryAdapter<Map<String,Object>> {

    @Override
    public void beforeQuery(Context context, Map<String, Param> params) {
        List<String> fields = context.getList("param.fields");
        EnumTypeHandler<UserField> handler = EnumHandlerFactory.getEnumHandler(UserField.class);
        List<UserField> userFields = fields.stream().map(handler::locateEnum).filter(Objects::nonNull).collect(Collectors.toList());
        context.setParamData("fields",userFields);
    }
}
