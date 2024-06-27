package com.ws.base.controller;

import com.ws.base.controller.daoru.ImportExcel;
import com.ws.base.controller.delete.DeleteResultBody;
import com.ws.base.controller.export.ExportExcel;
import com.ws.base.controller.list.ListResultBody;
import com.ws.base.controller.nestlist.NestListResultBody;
import com.ws.base.controller.save.SaveResultBody;
import com.ws.base.controller.select.SelectResultBody;
import com.ws.base.controller.update.UpdateResultBody;
import com.ws.base.mapper.BaseDataMapper;
import com.ws.base.model.BaseModel;
import com.ws.base.service.AbstractBaseDataService;

/**
 * @author GSF
 * <p>基础控制器,所有方法响应数据{@link com.ws.base.result.ResultBody}包装后的数据</p>
 */
public abstract class AbstractBaseDataControllerResultBody<S extends AbstractBaseDataService<?, ? extends BaseDataMapper<T>, T>, T extends BaseModel> extends AbstractBaseDataController<S, T> implements SaveResultBody<S, T>, DeleteResultBody<S, T>, UpdateResultBody<S, T>, SelectResultBody<S, T>, ListResultBody<S, T>, NestListResultBody<S, T>, ExportExcel<S, T>, ImportExcel<S, T> {

}
