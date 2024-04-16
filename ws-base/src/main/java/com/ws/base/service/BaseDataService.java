package com.ws.base.service;

import com.ws.base.mapper.BaseDataMapper;
import com.ws.base.model.BaseModel;

import java.util.List;
import java.util.Map;

/**
 * @author GSF
 * <p>BaseService</p>
 */
public interface BaseDataService<M extends BaseDataMapper<T>, T extends BaseModel> extends BaseService {

    /**
     * <p>保存</p>
     *
     * @param model 实体类
     **/
    int save(T model);

    /**
     * <p>批量保存</p>
     *
     * @param modelList 实体类列表
     **/
    int batchSave(List<T> modelList);

    /**
     * <p>删除</p>
     *
     * @param map {columnName : value}
     **/
    int delete(Map<String, Object> map);

    /**
     * <p>更新</p>
     *
     * @param map {columnName : value}
     **/
    int update(Map<String, Object> map);

    /**
     * <p>查询1条</p>
     *
     * @param map {columnName : value}
     **/
    T select(Map<String, Object> map);

    /**
     * <p>查询列表</p>
     *
     * @param map {columnName : value}
     **/
    List<Map<String, Object>> getList(Map<String, Object> map);

    /**
     * <p>查询嵌套列表</p>
     *
     * @param map {columnName : value}
     **/
    List<T> getNestList(Map<String, Object> map);

    int getListTotal(Map<String, Object> map);

    int getNestListTotal(Map<String, Object> map);

}
