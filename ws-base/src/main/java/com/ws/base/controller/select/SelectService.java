package com.ws.base.controller.select;

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

public interface SelectService<S extends AbstractBaseDataService<? extends BaseDataMapper<T>, T>, T extends BaseModel> extends BaseDataController<S, T> {

    /**
     * <p>查询一条</p>
     **/
    @RequestMapping("/select")
    @ResponseBody
    public default T select(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        return this.getService().select(this.getRequestParams(request));
    }

}
