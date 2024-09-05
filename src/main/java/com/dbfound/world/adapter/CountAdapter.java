package com.dbfound.world.adapter;

import com.nfwork.dbfound.core.Context;
import com.nfwork.dbfound.model.adapter.ObjectQueryAdapter;
import com.nfwork.dbfound.model.base.Count;
import com.nfwork.dbfound.model.bean.Param;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CountAdapter implements ObjectQueryAdapter {

    @Override
    public void beforeCount(Context context, Map<String, Param> params, Count count) {
        try {
            count.setCountSql(simpleCountSql(count.getCountSql()));
        }catch (Exception ignore) {
        }
    }

    private String simpleCountSql(String sql) throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        if(plainSelect.getJoins() == null){
            return sql;
        }

        String whereSql = " ";
        if(plainSelect.getWhere()!= null) {
           whereSql = whereSql + plainSelect.getWhere();
        }
        Set<String> aliasSet = findAlias(whereSql);

        Iterator<Join> joinIterator = plainSelect.getJoins().iterator();
        int removeSize = 0;
        while (joinIterator.hasNext()) {
            Join join = joinIterator.next();
            String alias = null;
            if(join.getRightItem().getAlias() != null) {
                alias = join.getRightItem().getAlias().getName();
            }else if(join.getRightItem() instanceof Table){
                alias = ((Table)join.getRightItem()).getName();
            }
            if (alias != null && !aliasSet.contains(alias)) {
                joinIterator.remove();
                removeSize++;
            }
        }

        if(removeSize == 0){
            return sql;
        }else {
            return select.toString();
        }
    }

    private final static Pattern executeParamPattern = Pattern.compile("[ ()+\\-/*=,][0-9a-zA-Z_]+\\.");

    private Set<String> findAlias(String sql){
        Set<String> set = new HashSet<>();
        Matcher m = executeParamPattern.matcher(sql);
        while (m.find()) {
            String param = m.group();
            String alias = param.substring(1, param.length() - 1);
            set.add(alias);
        }
        return set;
    }
}
