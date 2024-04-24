package com.ws.base.controller.nestlist;

import com.ws.base.controller.BaseDataController;
import com.ws.base.mapper.BaseDataMapper;
import com.ws.base.model.BaseModel;
import com.ws.base.result.ResultBody;
import com.ws.base.result.ResultTableBody;
import com.ws.base.service.AbstractBaseDataService;
import com.ws.tool.CacheTool;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface NestListResultTableBody<S extends AbstractBaseDataService<? extends BaseDataMapper<T>, T>, T extends BaseModel> extends BaseDataController<S, T> {

    /**
     * <p>查询列表</p>
     **/
    @RequestMapping("/getNestList")
    @ResponseBody
    public default ResultBody<List<T>> getNestList(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        Map<String, Object> params = this.getRequestParams(request);
        return ResultTableBody.success(this.getService().getNestList(params), this.getService().getTotal(params), CacheTool.getControllerModelGenericColumnType(this.getClass()));
    }

}
