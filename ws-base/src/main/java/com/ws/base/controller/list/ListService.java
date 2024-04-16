package com.ws.base.controller.list;

import com.ws.base.controller.BaseDataController;
import com.ws.base.mapper.BaseDataMapper;
import com.ws.base.model.BaseModel;
import com.ws.base.service.AbstractBaseDataService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ListService<S extends AbstractBaseDataService<? extends BaseDataMapper<T>, T>, T extends BaseModel> extends BaseDataController<S, T> {

    /**
     * <p>查询列表</p>
     **/
    @RequestMapping("/getList")
    @ResponseBody
    public default List<Map<String, Object>> getList(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        return this.getService().getList(this.getRequestParams(request));
    }

}
