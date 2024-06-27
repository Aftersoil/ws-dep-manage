package com.ws.base.controller.delete;

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

import java.util.Map;

public interface Delete<S extends AbstractBaseDataService<?, ? extends BaseDataMapper<T>, T>, T extends BaseModel> extends BaseDataController<S, T> {

    /**
     * <p>删除</p>
     **/
    @RequestMapping("/delete")
    @ResponseBody
    public default String delete(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {
        Map<String, Object> map = this.getRequestParams(request);
        int line = this.getService().delete(map);
        return line > 0 ? ResultBody.success().toJson() : ResultBody.error("记录不存在").toJsonyMdHms();
    }


}
