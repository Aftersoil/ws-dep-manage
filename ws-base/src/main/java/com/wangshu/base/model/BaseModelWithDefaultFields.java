package com.wangshu.base.model;

import com.wangshu.annotation.Column;
import com.wangshu.enu.Condition;

import java.util.Date;

/**
 * @author GSF
 * <p>BaseModelWithDefaultFields,内置一些常用字段</p>
 */
public class BaseModelWithDefaultFields extends BaseModel {

    @Column(title = "ID", conditions = {Condition.all})
    private String id;

    @Column(title = "创建日期", conditions = {Condition.all})
    private Date createdAt = new Date();

    @Column(title = "更新日期", conditions = {Condition.all})
    private Date updatedAt = new Date();

    @Column(title = "删除日期", conditions = {Condition.all})
    private Date deletedAt;

}
