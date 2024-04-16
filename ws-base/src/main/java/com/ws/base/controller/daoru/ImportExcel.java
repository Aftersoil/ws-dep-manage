package com.ws.base.controller.daoru;

import com.ws.base.controller.BaseDataController;
import com.ws.base.mapper.BaseDataMapper;
import com.ws.base.model.BaseModel;
import com.ws.base.result.ResultBody;
import com.ws.base.service.AbstractBaseDataService;
import com.ws.enu.CommonErrorInfo;
import com.ws.exception.IException;
import com.ws.tool.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface ImportExcel<S extends AbstractBaseDataService<? extends BaseDataMapper<T>, T>, T extends BaseModel> extends BaseDataController<S, T> {

    @RequestMapping("/importExcel")
    @ResponseBody
    public default ResultBody<Object> importExcel(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        Map<String, Object> params = this.getRequestParams(request);
        List<MultipartFile> fileList = ((StandardMultipartHttpServletRequest) request).getMultiFileMap().get("file");
        if (Objects.isNull(fileList) || fileList.isEmpty()) {
            return ResultBody.error("文件不存在");
        }
        Integer headerRowNumber = 1;
        String headerRowStr = String.valueOf(params.get("headerRow"));
        if (StringUtil.isNotEmpty(headerRowStr)) {
            try {
                headerRowNumber = Integer.valueOf(headerRowStr);
            } catch (NumberFormatException e) {
                throw new IException(CommonErrorInfo.BODY_NOT_MATCH);
            }
        }
        if (headerRowNumber < 1) {
            throw new IException(CommonErrorInfo.BODY_NOT_MATCH);
        }
        for (MultipartFile multipartFile : fileList) {
            this.getService().importExcel(multipartFile, headerRowNumber);
        }
        return ResultBody.success("导入成功");
    }

}
