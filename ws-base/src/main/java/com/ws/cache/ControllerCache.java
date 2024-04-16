package com.ws.cache;

import com.ws.base.controller.BaseDataController;
import com.ws.base.model.BaseModel;
import com.ws.base.service.BaseDataService;
import com.ws.tool.CommonTool;
import lombok.Data;

@Data
public class ControllerCache {

    public Class<? extends BaseModel> modelGeneric;
    public Class<? extends BaseDataService> serviceGeneric;

    public ControllerCache(Class<? extends BaseDataController> controllerClazz) {
        this.modelGeneric = CommonTool.getControllerModel(controllerClazz);
        this.serviceGeneric = CommonTool.getControllerService(controllerClazz);
    }

}
