package com.ws.base.controller;

import com.ws.base.controller.daoru.ImportExcel;
import com.ws.base.controller.delete.DeleteService;
import com.ws.base.controller.export.ExportExcel;
import com.ws.base.controller.list.ListService;
import com.ws.base.controller.nestlist.NestListService;
import com.ws.base.controller.save.SaveService;
import com.ws.base.controller.select.SelectService;
import com.ws.base.controller.update.UpdateService;
import com.ws.base.mapper.BaseDataMapper;
import com.ws.base.model.BaseModel;
import com.ws.base.service.AbstractBaseDataService;

/**
 * @author GSF
 * <p>基础控制器,不经过任何包装,直接响应Service的结果</p>
 */
public abstract class AbstractBaseDataControllerService<S extends AbstractBaseDataService<? extends BaseDataMapper<T>, T>, T extends BaseModel> extends AbstractBaseDataController<S, T> implements SaveService<S, T>, DeleteService<S, T>, UpdateService<S, T>, SelectService<S, T>, ListService<S, T>, NestListService<S, T>, ExportExcel<S, T>, ImportExcel<S, T> {

}
