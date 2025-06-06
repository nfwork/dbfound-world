package com.dbfound.world.service;

import com.github.nfwork.dbfound.starter.ModelExecutor;
import com.google.common.collect.Lists;
import com.nfwork.dbfound.core.Context;
import com.nfwork.dbfound.dto.ResponseObject;
import com.nfwork.dbfound.excel.ExcelColumn;
import com.nfwork.dbfound.excel.ExcelReader;
import com.nfwork.dbfound.excel.ExcelWriter;
import com.nfwork.dbfound.web.file.FilePart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ExcelService {

    @Autowired
    ModelExecutor modelExecutor;

    public ResponseObject importData(FilePart filePart) {
        List<ExcelColumn> columns = Lists.newArrayList(
                new ExcelColumn("user_code", "用户账号"),
                new ExcelColumn("user_name", "用户昵称")
        );
        List<List<Map<String, Object>>> datas = ExcelReader.readExcel(filePart, columns);
        Context context = new Context().withParam("userList", datas.get(0));
        return modelExecutor.execute(context, "userExcel", "");
    }

    public void exportData(Context context) throws Exception {
        List<ExcelColumn> columns = Lists.newArrayList(
                new ExcelColumn("user_code", "用户账号", 100),
                new ExcelColumn("user_name", "用户昵称", 100),
                new ExcelColumn("create_date", "创建时间", 180)
        );
        List<?> datas = modelExecutor.queryList(context, "userExcel", "");
        ExcelWriter.excelExport(context, datas, columns);
    }
}
