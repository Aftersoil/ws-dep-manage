package com.ws.base.controller;

import com.ws.base.controller.daoru.ImportExcel;
import com.ws.base.controller.delete.DeleteResultBody;
import com.ws.base.controller.export.ExportExcel;
import com.ws.base.controller.list.ListTableResultTableBody;
import com.ws.base.controller.nestlist.NestListResultTableBody;
import com.ws.base.controller.save.SaveResultBody;
import com.ws.base.controller.select.SelectResultBody;
import com.ws.base.controller.update.UpdateResultBody;
import com.ws.base.mapper.BaseDataMapper;
import com.ws.base.model.BaseModel;
import com.ws.base.service.AbstractBaseDataService;

/**
 * @author GSF
 * <p>BaseControllerImpl</p>
 */
public abstract class AbstractBaseDataControllerResultTableBody<S extends AbstractBaseDataService<? extends BaseDataMapper<T>, T>, T extends BaseModel> implements SaveResultBody<S, T>, DeleteResultBody<S, T>, UpdateResultBody<S, T>, SelectResultBody<S, T>, ListTableResultTableBody<S, T>, NestListResultTableBody<S, T>, ExportExcel<S, T>, ImportExcel<S, T> {

}
