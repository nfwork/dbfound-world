package com.dbfound.world.controlller;

import com.dbfound.world.service.ExcelService;
import com.github.nfwork.dbfound.starter.annotation.ContextAware;
import com.nfwork.dbfound.core.Context;
import com.nfwork.dbfound.dto.ResponseObject;
import com.nfwork.dbfound.web.file.FilePart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExcelController {

    @Autowired
    ExcelService excelService;

    @RequestMapping("user/import")
    public ResponseObject importUser(@ContextAware Context context) {
        FilePart filePart = (FilePart) context.getData("param.file");
        if(filePart == null) {
            throw new RuntimeException("file can not be null");
        }
        return excelService.importData(filePart);
    }

    @RequestMapping("user/export")
    public void exportUser(@ContextAware Context context) throws Exception {
        excelService.exportData(context);
    }
}
