package com.ws.generate.xml;

import cn.hutool.core.util.StrUtil;
import com.ws.exception.MessageException;
import com.ws.generate.metadata.field.ColumnInfo;
import com.ws.generate.metadata.model.ModelInfo;
import com.ws.tool.CommonStaticField;
import com.ws.tool.StringUtil;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GenerateXmlMysql<T extends ModelInfo<?, F>, F extends ColumnInfo<?, T>> extends GenerateXml<T, F> {

    public GenerateXmlMysql(T model) {
        super(model);
    }

    public GenerateXmlMysql(T model, Consumer<MessageException> message) {
        super(model, message);
    }

    @Override
    public org.dom4j.Element generateResultMap() {
        org.dom4j.Element resultMapElement = this.generateXmlElement("resultMap");
        resultMapElement.addAttribute("id", this.getModel().getModelName());
        resultMapElement.addAttribute("type", this.getModel().getModelFullName());

        List<F> tempFields = new ArrayList<>();
        tempFields.addAll(this.getModel().getBaseFields());
        tempFields.addAll(this.getModel().getClazzJoinFields());
        tempFields.addAll(this.getModel().getCollectionJoinFields());

        this.getFieldsResultMapResults(tempFields).forEach(resultMapElement::add);

        return resultMapElement;
    }

    @Override
    public org.dom4j.Element generateSave() {
        F primaryField = this.getModel().getPrimaryField();
        org.dom4j.Element insertElement = this.generateXmlElement("insert");
        insertElement.addAttribute("id", CommonStaticField.SAVE_METHOD_NAME);
        insertElement.addAttribute("parameterType", "Map");
        List<F> baseFields = this.getModel().getBaseFields();
        if (StrUtil.equals(primaryField.getType(), Long.class.getName()) || StrUtil.equals(primaryField.getType(), Integer.class.getName())) {
            insertElement.addAttribute("useGeneratedKeys", "true");
            insertElement.addAttribute("keyProperty", primaryField.getName());
            baseFields = baseFields.stream().filter(item -> !item.equals(primaryField)).toList();
        }
        String text = StringUtil.concat(CommonStaticField.WRAP, "insert into ", this.getModel().getTableName(), "(", baseFields.stream().map(item -> getBackQuoteStr(item.getName())).collect(Collectors.joining(",")), ") values (", String.join(",", baseFields.stream().map(item -> getPreCompileStr(item.getName())).collect(Collectors.joining(","))), ");", CommonStaticField.WRAP);
        insertElement.addText(text);
        return insertElement;
    }

    @Override
    Element generateBatchSave() {
        F primaryField = this.getModel().getPrimaryField();
        org.dom4j.Element batchInsertElement = this.generateXmlElement("insert");
        batchInsertElement.addAttribute("id", CommonStaticField.BATCH_SAVE_METHOD_NAME);
        batchInsertElement.addAttribute("parameterType", "List");
        List<F> baseFields = this.getModel().getBaseFields();
        if (StrUtil.equals(primaryField.getType(), Long.class.getName()) || StrUtil.equals(primaryField.getType(), Integer.class.getName())) {
            batchInsertElement.addAttribute("useGeneratedKeys", "true");
            batchInsertElement.addAttribute("keyProperty", primaryField.getName());
            baseFields = baseFields.stream().filter(item -> !item.equals(primaryField)).toList();
        }
        String text = StringUtil.concat(CommonStaticField.WRAP, "insert into ", this.getModel().getTableName(), "(", baseFields.stream().map(item -> getBackQuoteStr(item.getName())).collect(Collectors.joining(",")), ") values", CommonStaticField.WRAP);
        batchInsertElement.addText(text);
        String forEachText = StringUtil.concat("(", String.join(",", baseFields.stream().map(item -> getPreCompileStr(StringUtil.concat("item.", item.getName()))).collect(Collectors.joining(","))), ")");
        org.dom4j.Element forEachElement = this.getForEachElement("list", null, null, null, null, null);
        forEachElement.addText(forEachText);
        batchInsertElement.add(forEachElement);
        return batchInsertElement;
    }

    @Override
    public org.dom4j.Element generateDelete() {
        org.dom4j.Element deleteElement = this.generateXmlElement("delete");
        deleteElement.addAttribute("id", CommonStaticField.DELETE_METHOD_NAME);
        deleteElement.addAttribute("parameterType", "Map");
        deleteElement.addText(StringUtil.concat(CommonStaticField.WRAP, "delete from ", this.getModel().getTableName()));
        org.dom4j.Element whereElement = this.generateXmlElement("where");

        List<F> tempFields = new ArrayList<>(this.getModel().getBaseFields());
        this.getIf(tempFields, whereElement::add);

        deleteElement.add(whereElement);
        return deleteElement;
    }

    @Override
    public org.dom4j.Element generateUpdate() {
        org.dom4j.Element updateElement = this.generateXmlElement("update");
        updateElement.addAttribute("id", CommonStaticField.UPDATE_METHOD_NAME);
        updateElement.addAttribute("parameterType", "Map");
        String text = StringUtil.concat(CommonStaticField.WRAP, "update ", this.getModel().getTableName(), CommonStaticField.WRAP);
        updateElement.addText(text);
        org.dom4j.Element setElement = this.generateXmlElement("set");
        this.getModel().getBaseFields().forEach(item -> {
            Element ifNotNullElement = this.getIfNotNullElement(StringUtil.concat("new", StrUtil.upperFirst(item.getName())));
            ifNotNullElement.addText(StringUtil.concat(getBackQuoteStr(item.getName()), " = ", getPreCompileStr(StringUtil.concat("new", StrUtil.upperFirst(item.getName()))), ","));
            setElement.add(ifNotNullElement);
            Element ifSetNullElement = this.getIfSetNullElement(item.getName());
            ifSetNullElement.addText(StringUtil.concat(getBackQuoteStr(item.getName()), " = null,"));
            setElement.add(ifSetNullElement);
        });
        updateElement.add(setElement);
        org.dom4j.Element whereElement = this.generateXmlElement("where");

        List<F> tempFields = new ArrayList<>(this.getModel().getBaseFields());
        this.getIf(tempFields, whereElement::add);

        updateElement.add(whereElement);
        return updateElement;
    }

    @Override
    public org.dom4j.Element generateSelect() {
        org.dom4j.Element selectElement = this.generateXmlElement("select");
        selectElement.addAttribute("id", CommonStaticField.SELECT_METHOD_NAME);
        selectElement.addAttribute("parameterType", "Map");
        selectElement.addAttribute("resultType", this.getModel().getModelFullName());
        selectElement.addAttribute("resultMap", this.getModel().getModelName());
        selectElement.addText(StringUtil.concat(CommonStaticField.WRAP, "select ", "\n"));

        List<F> tempFields = new ArrayList<>();
        tempFields.addAll(this.getModel().getBaseFields());
        tempFields.addAll(this.getModel().getClazzJoinFields());
        tempFields.addAll(this.getModel().getCollectionJoinFields());

        selectElement.addText(this.getFieldsSelectText(tempFields));
        selectElement.addText(CommonStaticField.WRAP);

        selectElement.addText(StringUtil.concat("from ", this.getModel().getTableName()));
        selectElement.addText(CommonStaticField.WRAP);

        List<F> joinFields = new ArrayList<>();
        joinFields.addAll(this.getModel().getClazzJoinFields());
        joinFields.addAll(this.getModel().getCollectionJoinFields());

        selectElement.addText(this.getFieldsJoinText(joinFields));

        org.dom4j.Element whereElement = this.generateXmlElement("where");
        this.getIf(tempFields, whereElement::add);

        Element ifForUpdateNotNullElement = this.getIfNotNullElement("enableForUpdate");
        ifForUpdateNotNullElement.addText("for update");
        whereElement.add(ifForUpdateNotNullElement);

        selectElement.add(whereElement);
        return selectElement;
    }

    @Override
    public org.dom4j.Element generateGetList() {
        org.dom4j.Element listElement = this.generateXmlElement("select");
        listElement.addAttribute("id", CommonStaticField.GET_LIST_METHOD_NAME);
        listElement.addAttribute("parameterType", "Map");
        listElement.addAttribute("resultType", "Map");
        listElement.addText(CommonStaticField.WRAP);
        listElement.addText("select ");
        listElement.addText(CommonStaticField.WRAP);

//        List<F> collectionFields = this.getModel().getCollectionFields().stream().filter(item -> {
//            T leftModel = item.getLeftModel();
//            return !StrUtil.equals(leftModel.getModelFullName(), this.getModel().getModelFullName());
//        }).toList();

        List<F> tempFields = new ArrayList<>();
        tempFields.addAll(this.getModel().getBaseFields());
        tempFields.addAll(this.getModel().getClazzJoinFields());
//        tempFields.addAll(collectionFields);

        listElement.addText(this.getFieldsSelectText(tempFields));
        listElement.addText(CommonStaticField.WRAP);

        listElement.addText(StringUtil.concat("from ", this.getModel().getTableName(), CommonStaticField.WRAP));
        List<F> joinFields = new ArrayList<>();
        joinFields.addAll(this.getModel().getClazzJoinFields());
//        joinFields.addAll(collectionFields);

        listElement.addText(this.getFieldsJoinText(joinFields));

        org.dom4j.Element whereElement = this.generateXmlElement("where");

        this.getIf(tempFields, whereElement::add);

        listElement.add(whereElement);
        listElement.addText(CommonStaticField.WRAP);
        listElement.add(this.getOrder());
        listElement.addText(CommonStaticField.WRAP);
        listElement.addText(this.getLimit());
        return listElement;
    }

    @Override
    public org.dom4j.Element generateGetNestList() {
        org.dom4j.Element nestListElement = this.generateXmlElement("select");
        nestListElement.addAttribute("id", CommonStaticField.GET_NEST_LIST_METHOD_NAME);
        nestListElement.addAttribute("parameterType", "Map");
        nestListElement.addAttribute("resultType", this.getModel().getModelFullName());
        nestListElement.addAttribute("resultMap", this.getModel().getModelName());
        nestListElement.addText(CommonStaticField.WRAP);
        nestListElement.addText("select ");
        nestListElement.addText(CommonStaticField.WRAP);

        List<F> tempFields = new ArrayList<>();
        tempFields.addAll(this.getModel().getBaseFields());
        tempFields.addAll(this.getModel().getClazzJoinFields());
        tempFields.addAll(this.getModel().getCollectionJoinFields());

        nestListElement.addText(this.getFieldsSelectText(tempFields));
        nestListElement.addText(CommonStaticField.WRAP);

        nestListElement.addText(StringUtil.concat("from ", this.getModel().getTableName()));
        nestListElement.addText(CommonStaticField.WRAP);

        List<F> joinFields = new ArrayList<>();
        joinFields.addAll(this.getModel().getClazzJoinFields());
        joinFields.addAll(this.getModel().getCollectionJoinFields());

        nestListElement.addText(this.getFieldsJoinText(joinFields));

        org.dom4j.Element whereElement = this.generateXmlElement("where");

        this.getIf(tempFields, whereElement::add);

        nestListElement.add(whereElement);
        nestListElement.addText(CommonStaticField.WRAP);
        nestListElement.add(this.getOrder());
        nestListElement.addText(CommonStaticField.WRAP);
        nestListElement.addText(this.getLimit());
        return nestListElement;
    }

    @Override
    public org.dom4j.Element generateGetTotal() {
        org.dom4j.Element totalElement = this.generateXmlElement("select");
        totalElement.addAttribute("id", CommonStaticField.GET_TOTAL_METHOD_NAME);
        totalElement.addAttribute("parameterType", "Map");
        totalElement.addAttribute("resultType", Integer.class.getSimpleName());
        totalElement.addText(CommonStaticField.WRAP);
        totalElement.addText(StringUtil.concat("select count(", this.getBackQuoteStr(this.getModel().getTableName()), ".", this.getBackQuoteStr("id"), ") from ", this.getModel().getTableName()));
        totalElement.addText(CommonStaticField.WRAP);

        List<F> joinFields = new ArrayList<>();
        joinFields.addAll(this.getModel().getClazzJoinFields());
        joinFields.addAll(this.getModel().getCollectionJoinFields());

        totalElement.addText(this.getFieldsJoinText(joinFields));

        List<F> tempFields = new ArrayList<>();
        tempFields.addAll(this.getModel().getBaseFields());
        tempFields.addAll(this.getModel().getClazzJoinFields());
        tempFields.addAll(this.getModel().getCollectionJoinFields());

        org.dom4j.Element whereElement = this.generateXmlElement("where");

        this.getIf(tempFields, whereElement::add);

        totalElement.add(whereElement);

        return totalElement;
    }

}
