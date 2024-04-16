package com.ws.base.controller.update;

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

public interface UpdateService<S extends AbstractBaseDataService<? extends BaseDataMapper<T>, T>, T extends BaseModel> extends BaseDataController<S, T> {

    /**
     * <p>查询一条</p>
     **/
    @RequestMapping("/update")
    @ResponseBody
    public default int update(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        return this.getService().update(this.getRequestParams(request));
    }

}
