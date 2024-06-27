package com.ws.generate.metadata.field;

import com.ws.annotation.Join;
import com.ws.enu.Condition;
import com.ws.enu.DataBaseType;
import com.ws.enu.JoinCondition;
import com.ws.enu.JoinType;
import com.ws.generate.metadata.model.ModelInfo;
import com.ws.tool.MysqlTypeMapInfo;
import com.ws.tool.StringUtil;
import org.apache.ibatis.type.JdbcType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface ColumnInfo<T, M extends ModelInfo<?, ?>> extends Column {

    T getMetaData();

    M getModel();

    M getLeftModel();

    M getRightModel();

    String getName();

    String getJavaTypeName();

    com.ws.annotation.Column getColumn();

    default String getTitle() {
        String title = null;
        if (this.isBaseField()) {
            title = this.getColumn().title();
            if (StringUtil.isEmpty(title)) {
                title = this.getName();
            }
        }
        return title;
    }

    default String getComment() {
        String comment = null;
        if (this.isBaseField()) {
            comment = this.getColumn().comment();
            if (StringUtil.isEmpty(comment)) {
                comment = this.getColumn().title();
            }
            if (StringUtil.isEmpty(comment)) {
                comment = this.getName();
            }
        }
        return comment;
    }

    String getJdbcType();

    default @NotNull JdbcType getMybatisJdbcType() {
        JdbcType mybatisJdbcType;
        switch (this.getModel().getDataBaseType()) {
            case DataBaseType.mysql ->
                    mybatisJdbcType = MysqlTypeMapInfo.getMybatisJdbcTypeByDbColumnType(this.getJdbcType());
            case DataBaseType.oracle, DataBaseType.sqlServer ->
                    throw new IllegalArgumentException("暂无对应数据库类型实现");
            default -> throw new IllegalArgumentException("没有匹配的数据库类型");
        }
        return mybatisJdbcType;
    }

    default List<Condition> getConditions() {
        if (this.isBaseField()) {
            return Arrays.asList(this.getColumn().conditions());
        }
        return new ArrayList<>();
    }

    default boolean isBaseField() {
        return Objects.nonNull(this.getColumn());
    }

    default boolean isPrimaryField() {
        return this.isBaseField() && this.getColumn().primary();
    }

    default boolean isKeywordField() {
        return this.isBaseField() && this.getColumn().keyword();
    }

    Join getJoin();

    default boolean isJoinField() {
        return Objects.nonNull(this.getJoin());
    }

    default boolean isClassJoinField() {
        return this.isJoinField() && !this.isCollectionJoinField();
    }

    default boolean isCollectionJoinField() {
        return this.isJoinField() && this.getJavaTypeName().contains(List.class.getSimpleName());
    }

    default List<String> getLeftSelectFieldNames() {
        List<String> leftSelectFieldNames = new ArrayList<>();
        if (this.isJoinField()) {
            leftSelectFieldNames = Arrays.asList(this.getJoin().leftSelectFields());
        }
        return leftSelectFieldNames;
    }

    default String getLeftJoinField() {
        if (this.isJoinField()) {
            return this.getJoin().leftJoinField();
        }
        return null;
    }

    default String getRightJoinField() {
        if (this.isJoinField()) {
            return this.getJoin().rightJoinField();
        }
        return null;
    }

    default JoinType getJoinType() {
        if (this.isJoinField()) {
            return this.getJoin().joinType();
        }
        return null;
    }

    default JoinCondition getJoinCondition() {
        if (this.isJoinField()) {
            return this.getJoin().joinCondition();
        }
        return null;
    }

    default String getInfix() {
        if (this.isJoinField()) {
            return this.getJoin().infix();
        }
        return null;
    }

}
