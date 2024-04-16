package com.ws.base.controller;

import com.ws.base.mapper.BaseDataMapper;
import com.ws.base.model.BaseModel;
import com.ws.base.service.AbstractBaseDataService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class AbstractBaseDataController<S extends AbstractBaseDataService<? extends BaseDataMapper<T>, T>, T extends BaseModel> implements BaseDataController<S, T> {

    public final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Map<String, Object> getRequestParams(HttpServletRequest request) {
        Map<String, Object> requestParams = BaseDataController.super.getRequestParams(request);
        log.info("请求参数: {}", requestParams);
        return requestParams;
    }

}
