package com.ws.base.controller;

import com.ws.base.mapper.BaseDataMapper;
import com.ws.base.model.BaseModel;
import com.ws.base.service.AbstractBaseDataService;

public interface BaseDataController<S extends AbstractBaseDataService<? extends BaseDataMapper<T>, T>, T extends BaseModel> extends BaseController {

    S getService();

    T getModel();

}
