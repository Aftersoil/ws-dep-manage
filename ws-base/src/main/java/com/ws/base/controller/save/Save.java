package com.ws.base.controller.save;

import com.ws.base.controller.BaseDataController;
import com.ws.base.mapper.BaseDataMapper;
import com.ws.base.model.BaseModel;
import com.ws.base.result.ResultBody;
import com.ws.base.service.AbstractBaseDataService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

public interface Save<S extends AbstractBaseDataService<?, ? extends BaseDataMapper<T>, T>, T extends BaseModel> extends BaseDataController<S, T> {


    /**
     * <p>保存</p>
     **/
    @RequestMapping("/save")
    @ResponseBody
    public default String save(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {
        int line = this.getService().save(this.getRequestParams(request));
        return line > 0 ? ResultBody.success(line).toJson() : ResultBody.error("保存失败").toJson();
    }

}
