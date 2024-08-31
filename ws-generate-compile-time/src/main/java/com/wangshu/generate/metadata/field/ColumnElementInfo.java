package com.wangshu.generate.metadata.field;

import cn.hutool.core.util.StrUtil;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.wangshu.annotation.Column;
import com.wangshu.annotation.Join;
import com.wangshu.base.model.BaseModel;
import com.wangshu.generate.metadata.model.ModelElementInfo;
import com.wangshu.tool.MysqlTypeMapInfo;
import com.wangshu.tool.StringUtil;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypeException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@lombok.Data
public class ColumnElementInfo extends AbstractColumnInfo<Element, ModelElementInfo> {

    public ColumnElementInfo(Element metaData, ModelElementInfo model) {
        this.setMetaData(metaData);
        this.setModel(model);
        this.initBaseInfo(metaData, model);
        this.initColumnInfo(metaData, model);
        this.initJoinInfo(metaData, model);
    }

    public void initBaseInfo(@NotNull Element metaData, ModelElementInfo model) {
        this.setName(metaData.getSimpleName().toString());
        this.setJavaTypeName(metaData.asType().toString());
    }

    @Override
    public void initColumnInfo(@NotNull Element metaData, ModelElementInfo model) {
        Column column = metaData.getAnnotation(Column.class);
        if (Objects.nonNull(column)) {
            this.setColumn(column);
            this.setJdbcType(this.initJdbcType(metaData, column));
            this.setTitle(column.title());
            this.setComment(column.comment());
            this.setConditions(Arrays.asList(column.conditions()));
            this.setBaseField(true);
            this.setPrimaryField(column.primary());
            this.setKeywordField(column.keyword());
        }
    }

    @Override
    public void initJoinInfo(@NotNull Element metaData, ModelElementInfo model) {
        Join join = metaData.getAnnotation(Join.class);
        if (Objects.nonNull(join)) {
            this.setJoin(join);
            this.setLeftModel(this.initLeftModel(metaData));
            this.setRightModel(this.initRightModel(join));
            this.setJoinField(true);
            this.setClassJoinField(this.initIsClassJoinField(metaData));
            this.setCollectionJoinField(this.initIsCollectionJoinField(metaData));
            this.setLeftSelectFieldNames(Arrays.asList(this.getJoin().leftSelectFields()));
            this.setLeftJoinField(this.getJoin().leftJoinField());
            this.setRightJoinField(this.getJoin().rightJoinField());
            this.setJoinType(this.getJoin().joinType());
            this.setJoinCondition(this.getJoin().joinCondition());
            this.setInfix(this.getJoin().infix());
        }
    }

    private boolean initIsClassJoinField(@NotNull Element field) {
        return !this.initIsCollectionJoinField(field);
    }

    private boolean initIsCollectionJoinField(@NotNull Element field) {
        return StrUtil.equals(((Symbol.VarSymbol) field).type.tsym.toString(), List.class.getTypeName());
    }

    @Nullable
    private ModelElementInfo initLeftModel(@NotNull Element field) {
        if (this.initIsCollectionJoinField(field)) {
            List<Type> typeArguments = ((Symbol.VarSymbol) field).type.getTypeArguments();
            if (Objects.nonNull(typeArguments) && !typeArguments.isEmpty()) {
                return new ModelElementInfo(this.getModel().getModuleInfo(), (Element) typeArguments.getFirst().tsym, true);
            }
            return null;
        } else {
            try {
                Class<? extends BaseModel> unused = this.getJoin().leftTable();
            } catch (MirroredTypeException e) {
                Type.ClassType typeMirror = (Type.ClassType) e.getTypeMirror();
                if (Objects.isNull(typeMirror.supertype_field) && StrUtil.equals(String.valueOf(typeMirror.tsym.name), BaseModel.class.getSimpleName())) {
//                    默认值,未指定leftTable,则为当前属性类型或泛型
                    Symbol.TypeSymbol fieldTsym = ((Symbol.VarSymbol) field).type.tsym;
                    return new ModelElementInfo(this.getModel().getModuleInfo(), (Element) fieldTsym, true);
                } else if (StrUtil.equals(String.valueOf(typeMirror.supertype_field.tsym.name), BaseModel.class.getSimpleName())) {
                    Symbol.TypeSymbol fieldTsym = typeMirror.tsym;
                    return new ModelElementInfo(this.getModel().getModuleInfo(), (Element) fieldTsym, true);
                }
            }
        }
        return null;
    }

    @Nullable
    private ModelElementInfo initRightModel(@NotNull Join join) {
        try {
            Class<? extends BaseModel> unused = join.rightTable();
        } catch (MirroredTypeException e) {
            Type.ClassType typeMirror = (Type.ClassType) e.getTypeMirror();
            if (Objects.isNull(typeMirror.supertype_field) && StrUtil.equals(String.valueOf(typeMirror.tsym.name), BaseModel.class.getSimpleName())) {
//                    默认值,未指定rightTable,则为当前类
                return this.getModel();
            } else if (StrUtil.equals(String.valueOf(typeMirror.supertype_field.tsym.name), BaseModel.class.getSimpleName())) {
                Symbol.TypeSymbol fieldTsym = typeMirror.tsym;
                return new ModelElementInfo(this.getModel().getModuleInfo(), (Element) fieldTsym, true);
            }
        }
        return null;
    }

    @NotNull
    private String initJdbcType(@NotNull Element field, @NotNull Column column) {
        if (StringUtil.isEmpty(column.jdbcType())) {
            String type = field.asType().toString();
            String jdbcType = MysqlTypeMapInfo.getDbColumnTypeByJavaTypeName(type);
            if (StringUtil.isEmpty(jdbcType)) {
                throw new IllegalArgumentException("Unsupported element: " + field);
            }
            return jdbcType;
        }
        return column.jdbcType();
    }

}
