package com.dbfound.world.dfunction;

import com.nfwork.dbfound.db.dialect.SqlDialect;
import com.nfwork.dbfound.model.dsql.DSqlFunction;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class StartWith extends DSqlFunction {

    @PostConstruct
    public void init() {
        register("start_with");
    }

    @Override
    public Object apply(List<Object> params, SqlDialect sqlDialect) {
        String string = params.get(0).toString();
        String startWith = params.get(1).toString();
        return string.startsWith(startWith);
    }
}
