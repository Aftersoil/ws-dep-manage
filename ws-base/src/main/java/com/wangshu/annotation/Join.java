package com.wangshu.annotation;

import com.wangshu.base.model.BaseModel;
import com.wangshu.enu.JoinCondition;
import com.wangshu.enu.JoinType;

import java.lang.annotation.*;

/**
 * <p>标记连表查询字段</p>
 * <p>select</p>
 * <p>currentModelFields,</p>
 * <p>leftTable.[leftSelectFields]</p>
 * <p>from currentModel</p>
 * <p>left join leftTable on</p>
 * <p>(左) leftTable.leftJoinField = rightTable.rightJoinField (右)</p>
 *
 * @author GSF
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Join {

    String comment() default "";

    /**
     * <p>
     * 目标join表,建议指定
     * 如果不指定且该字段类型是BaseModel的子类,则该值则为该字段的类型
     * </p>
     **/
    Class<? extends BaseModel> leftTable() default BaseModel.class;

    /**
     * <p>关联属性</p>
     **/
    String leftJoinField() default "id";

    /**
     * <p>查询关联表的属性,默认目标join类的所有属性或自己指定</p>
     **/
    String[] leftSelectFields() default {"*"};

    /**
     * <p>
     * 一般在关联的属性在其他实体时指定
     * 如果不指定,默认是当前实体类(当前属性所在类的类型)
     * </p>
     **/
    Class<? extends BaseModel> rightTable() default BaseModel.class;

    /**
     * <p>关联属性</p>
     **/
    String rightJoinField() default "id";

    JoinType joinType() default JoinType.left;

    JoinCondition joinCondition() default JoinCondition.equal;

    /**
     * <p>用于字段alias</p>
     **/
    String infix() default "Model";

}
