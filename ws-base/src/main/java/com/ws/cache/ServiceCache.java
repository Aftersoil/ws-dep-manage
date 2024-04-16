package com.ws.cache;

import com.ws.base.mapper.BaseDataMapper;
import com.ws.base.model.BaseModel;
import com.ws.base.service.BaseDataService;
import com.ws.tool.CommonTool;
import lombok.Data;

@Data
public class ServiceCache {

    public Class<? extends BaseModel> modelGeneric;
    public Class<? extends BaseDataMapper<? extends BaseModel>> mapperGeneric;

    public ServiceCache(Class<? extends BaseDataService> serviceClazz) {
        this.modelGeneric = CommonTool.getServiceModel(serviceClazz);
        this.mapperGeneric = CommonTool.getServiceMapper(serviceClazz);
    }

}
