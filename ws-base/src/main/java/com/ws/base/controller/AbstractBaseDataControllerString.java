package com.ws.base.controller;

import com.ws.base.controller.daoru.ImportExcel;
import com.ws.base.controller.delete.Delete;
import com.ws.base.controller.export.ExportExcel;
import com.ws.base.controller.list.List;
import com.ws.base.controller.nestlist.NestList;
import com.ws.base.controller.save.Save;
import com.ws.base.controller.select.Select;
import com.ws.base.controller.update.Update;
import com.ws.base.mapper.BaseDataMapper;
import com.ws.base.model.BaseModel;
import com.ws.base.service.AbstractBaseDataService;

/**
 * @author GSF
 * <p>基础控制器,所有方法响应数据{@link com.ws.base.result.ResultBody}包装后的JSON字符串数据</p>
 */
public abstract class AbstractBaseDataControllerString<S extends AbstractBaseDataService<?, ? extends BaseDataMapper<T>, T>, T extends BaseModel> extends AbstractBaseDataController<S, T> implements Save<S, T>, Delete<S, T>, Update<S, T>, Select<S, T>, List<S, T>, NestList<S, T>, ExportExcel<S, T>, ImportExcel<S, T> {


}
