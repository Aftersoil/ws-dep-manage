package com.ws.generate.xml;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.ws.enu.Condition;
import com.ws.enu.JoinCondition;
import com.ws.enu.JoinType;
import com.ws.exception.MessageException;
import com.ws.generate.GenerateInfo;
import com.ws.generate.metadata.field.ColumnInfo;
import com.ws.generate.metadata.model.ModelInfo;
import com.ws.tool.CommonStaticField;
import com.ws.tool.StringUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@lombok.Data
public abstract class GenerateXml<T extends ModelInfo<?, F>, F extends ColumnInfo<?, T>> implements GenerateInfo {

    private T model;
    private Document mapperDocument;
    private Consumer<MessageException> message;

    public GenerateXml(@NotNull T model) {
        this.model = model;
        this.message = null;
    }

    public GenerateXml(@NotNull T model, Consumer<MessageException> message) {
        this.model = model;
        this.message = message;
    }

    public boolean writeXml() {
        return writeXml(this.getModel().getGenerateXmlFilePath());
    }

    public boolean writeXml(String path) {
        if (Objects.isNull(this.getMapperDocument())) {
            this.generateMapperDocument();
        }
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setTrimText(false);
            format.setSuppressDeclaration(true);
            format.setEncoding(this.getMapperDocument().getXMLEncoding());
            File file = FileUtil.touch(path);
            file.deleteOnExit();
            XMLWriter writer = new XMLWriter(new FileWriter(file), format);
            writer.write(this.getMapperDocument());
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            this.printWarn("导出xml出现异常", e);
        }
        return false;
    }

    public Document generateMapperDocument() {
        Document document = DocumentHelper.createDocument();
        document.setXMLEncoding("UTF-8");
        document.setName(this.getModel().getMapperName());
        document.addDocType("mapper", CommonStaticField.MYBATIS_XML_DOCTYPE, null);
        org.dom4j.Element rootElement = this.generateXmlElement("mapper");
        rootElement.addAttribute("namespace", this.model.getMapperFullName());
        rootElement.addText(CommonStaticField.WRAP);
        org.dom4j.Element generateResultMap = this.generateResultMap();
        rootElement.add(generateResultMap);
        rootElement.addText(CommonStaticField.WRAP);
        org.dom4j.Element insertElement = this.generateSave();
        rootElement.add(insertElement);
        rootElement.addText(CommonStaticField.WRAP);
        org.dom4j.Element batchInsertElement = this.generateBatchSave();
        rootElement.add(batchInsertElement);
        rootElement.addText(CommonStaticField.WRAP);
        org.dom4j.Element deleteElement = this.generateDelete();
        rootElement.add(deleteElement);
        rootElement.addText(CommonStaticField.WRAP);
        org.dom4j.Element updateElement = this.generateUpdate();
        rootElement.add(updateElement);
        rootElement.addText(CommonStaticField.WRAP);
        org.dom4j.Element selectElement = this.generateSelect();
        rootElement.add(selectElement);
        rootElement.addText(CommonStaticField.WRAP);
        org.dom4j.Element getListElement = this.generateGetList();
        rootElement.add(getListElement);
        rootElement.addText(CommonStaticField.WRAP);
        org.dom4j.Element getNestListElement = this.generateGetNestList();
        rootElement.add(getNestListElement);
        rootElement.addText(CommonStaticField.WRAP);
        org.dom4j.Element getTotalElement = this.generateGetListTotal();
        rootElement.add(getTotalElement);
        rootElement.addText(CommonStaticField.WRAP);
        Element getNestListTotalElement = this.generateGetNestListTotal();
        rootElement.add(getNestListTotalElement);
        rootElement.addText(CommonStaticField.WRAP);
        document.setRootElement(rootElement);
        this.setMapperDocument(document);
        return this.getMapperDocument();
    }

    public org.dom4j.Element generateXmlElement(String elementName) {
        return DocumentHelper.createElement(elementName);
    }

    abstract org.dom4j.Element generateResultMap();

    abstract org.dom4j.Element generateSave();

    abstract org.dom4j.Element generateBatchSave();

    abstract org.dom4j.Element generateDelete();

    abstract org.dom4j.Element generateUpdate();

    abstract org.dom4j.Element generateSelect();

    abstract org.dom4j.Element generateGetList();

    abstract org.dom4j.Element generateGetNestList();

    abstract org.dom4j.Element generateGetListTotal();

    abstract org.dom4j.Element generateGetNestListTotal();

    public @NotNull List<org.dom4j.Element> getFieldsResultMapResults(@NotNull List<F> fields) {
        List<org.dom4j.Element> elements = new ArrayList<>();
        List<org.dom4j.Element> tempElements = new ArrayList<>();
        fields.forEach(field -> {
            if (field.isPrimaryField()) {
                elements.add(this.getFieldResultMap(field));
            } else {
                tempElements.add(this.getFieldResultMap(field));
            }
        });
        elements.addAll(tempElements);
        return elements;
    }

    public @Nullable org.dom4j.Element getFieldResultMap(@NotNull F field) {
        if (field.isBaseField()) {
            return this.getResultMapElement(field);
        } else if (field.isClassJoinField() || field.isCollectionJoinField()) {
            T leftModel = field.getLeftModel();
            List<F> baseFields = leftModel.getBaseFields();
            List<String> leftSelectFieldNames = field.getLeftSelectFieldNames();
            if (!StrUtil.equals(leftSelectFieldNames.getFirst(), "*")) {
                baseFields = baseFields.stream().filter(item -> leftSelectFieldNames.contains(item.getName())).toList();
            }
            org.dom4j.Element collectionElement = this.getCollectionElement(field.getName(), leftModel.getModelFullName());
            if (field.isCollectionJoinField()) {
                collectionElement.addAttribute("javaType", List.class.getTypeName());
            }
            Optional<F> primaryField = baseFields.stream().filter(item -> item.isPrimaryField()).findFirst();
            if (primaryField.isPresent()) {
                String property = primaryField.get().getName();
                String column = StringUtil.concat(field.getName(), field.getInfix(), StringUtils.capitalize(property));
                String jdbcType = primaryField.get().getMybatisJdbcType();
                Element idResultElement = this.getResultMapElement(column, jdbcType, property, primaryField.get().isPrimaryField());
                collectionElement.add(idResultElement);
            }
            baseFields = baseFields.stream().filter(item -> !item.isPrimaryField()).toList();
            for (F baseField : baseFields) {
                String property = baseField.getName();
                String column = StringUtil.concat(field.getName(), field.getInfix(), StringUtils.capitalize(property));
                String jdbcType = baseField.getMybatisJdbcType();
                Element idResultElement = this.getResultMapElement(column, jdbcType, property, baseField.isPrimaryField());
                collectionElement.add(idResultElement);
            }
            return collectionElement;
        }
        return null;
    }

    public String getFieldsSelectText(@NotNull List<F> fields) {
        List<String> fieldsSelectTextList = this.getFieldsSelectTextList(fields);
        return String.join(",\n", fieldsSelectTextList);
    }

    public @NotNull List<String> getFieldsSelectTextList(@NotNull List<F> fields) {
        List<String> fieldsSelectText = new ArrayList<>();
        for (F field : fields) {
            if (field.isBaseField()) {
                String table = this.getModel().getTableName();
                String columnName = field.getName();
                fieldsSelectText.add(this.getSelectText(table, columnName));
            } else if (field.isClassJoinField() || field.isCollectionJoinField()) {
                T leftModel = field.getLeftModel();
                List<F> baseFields = leftModel.getBaseFields();
                List<String> leftSelectFields = field.getLeftSelectFieldNames();
                if (!StrUtil.equals(leftSelectFields.getFirst(), "*")) {
                    baseFields = baseFields.stream().filter(item -> leftSelectFields.contains(item.getName())).toList();
                }
                baseFields = baseFields.stream().filter(item -> !item.isPrimaryField()).toList();
                for (F baseField : baseFields) {
                    String table = getJoinLeftTableAsName(field);
                    String columnName = baseField.getName();
                    String columnAsName = StringUtil.concat(field.getName(), field.getInfix(), StringUtils.capitalize(columnName));
                    fieldsSelectText.add(this.getSelectText(table, columnName, columnAsName));
                }
            }
        }
        return fieldsSelectText;
    }

    public String getJoinLeftTableAsName(@NotNull F joinField) {
        T leftModel = joinField.getLeftModel();
        T rightModel = joinField.getRightModel();
        return joinField.getName();
    }

    public String getJoinRightTableAsName(@NotNull F joinField) {
        T leftModel = joinField.getLeftModel();
        T rightModel = joinField.getRightModel();
//        rightTable是当前类
        if (StrUtil.equals(this.getModel().getModelName(), rightModel.getModelName())) {
            return rightModel.getTableName();
        }
//        rightTable不是当前类,查找当前类中关联字段的关联类型和rightTable相同的字段
        List<F> list = this.getModel().getClazzJoinFields().stream().filter(item -> StrUtil.equals(item.getLeftModel().getModelName(), rightModel.getModelName())).toList();
        if (list.isEmpty()) {
            list = this.getModel().getCollectionJoinFields().stream().filter(item -> StrUtil.equals(item.getLeftModel().getModelName(), item.getLeftModel().getModelFullName())).toList();
        }
        return this.getJoinLeftTableAsName(list.getFirst());
    }

    public String getFieldsJoinText(@NotNull List<F> fields) {
        return String.join("\n", this.getFieldsJoinTextList(fields));
    }

    public @NotNull List<String> getFieldsJoinTextList(@NotNull List<F> fields) {
        List<String> fieldsJoinText = new ArrayList<>();
        for (F field : fields) {
            if (field.isClassJoinField() || field.isCollectionJoinField()) {
                T leftModel = field.getLeftModel();
                String leftJoinField = field.getLeftJoinField();
                String leftTable = leftModel.getTableName();
                String leftTableAs = this.getJoinLeftTableAsName(field);

                T rightModel = field.getRightModel();
                String rightJoinField = field.getRightJoinField();
                String rightTable = rightModel.getTableName();
                String rightTableAs = this.getJoinRightTableAsName(field);

                JoinType joinType = field.getJoinType();
                JoinCondition joinCondition = field.getJoinCondition();

                switch (joinCondition) {
                    case equal -> {
                        fieldsJoinText.add(StringUtil.concat(joinType.name(), " join ", leftTable, " as ", leftTableAs, " on ", leftTableAs, ".", leftJoinField, " = ", rightTableAs, ".", rightJoinField));
                    }
                    case great -> {
                        fieldsJoinText.add(StringUtil.concat(joinType.name(), " join ", leftTable, " as ", leftTableAs, " on ", leftTableAs, ".", leftJoinField, " > ", rightTableAs, ".", rightJoinField));
                    }
                    case less -> {
                        fieldsJoinText.add(StringUtil.concat(joinType.name(), " join ", leftTable, " as ", leftTableAs, " on ", leftTableAs, ".", leftJoinField, " < ", rightTableAs, ".", rightJoinField));
                    }
                    case like -> {
                        fieldsJoinText.add(StringUtil.concat(joinType.name(), " join ", leftTable, " as ", leftTableAs, " on instr(", leftTableAs, ".", leftJoinField, ",", rightTableAs, ".", rightJoinField, ")"));
                    }
                }
            }
        }
        return fieldsJoinText;
    }

    public void getIf(@NotNull List<F> fields, @NotNull Consumer<org.dom4j.Element> elementConsumer) {
        List<Element> orElements = new ArrayList<>();
        for (F field : fields) {
            if (field.isBaseField()) {
                List<Condition> conditions = field.getConditions();
                if (conditions.getFirst().equals(Condition.all)) {
                    conditions = Condition.getEntries();
                }
                conditions.forEach((condition -> {
                    if (!condition.equals(Condition.all)) {
                        if (condition.name().contains("or")) {
                            orElements.add(this.getBaseFieldIfElement(field, condition));
                        } else {
                            elementConsumer.accept(this.getBaseFieldIfElement(field, condition));
                        }
                    }
                }));
            } else if (field.isClassJoinField() || field.isCollectionJoinField()) {
                T leftModel = field.getLeftModel();
                List<F> baseFields = leftModel.getBaseFields();
                List<String> leftSelectFieldNames = field.getLeftSelectFieldNames();
                if (!StrUtil.equals(leftSelectFieldNames.getFirst(), "*")) {
                    baseFields = baseFields.stream().filter(item -> leftSelectFieldNames.contains(item.getName())).toList();
                }
                baseFields.forEach(item -> {
                    List<Condition> conditions = item.getConditions();
                    if (Objects.nonNull(conditions)) {
                        if (conditions.getFirst().equals(Condition.all)) {
                            conditions = Condition.getEntries();
                        }
                        conditions.forEach((condition -> {
                            if (!condition.equals(Condition.all)) {
                                if (condition.name().contains("or")) {
                                    orElements.add(this.getJoinFieldIfElement(field, leftModel, item, condition));
                                } else {
                                    elementConsumer.accept(this.getJoinFieldIfElement(field, leftModel, item, condition));
                                }
                            }
                        }));
                    }
                });
            }
        }
        Element enableOrElement = DocumentHelper.createElement("if").addAttribute("test", "enableOr != null").addText("and (0 = 1");
        orElements.forEach(enableOrElement::add);
        enableOrElement.addText(")");
        elementConsumer.accept(enableOrElement);
    }

    public org.dom4j.Element getBaseFieldIfElement(@NotNull F field, @NotNull Condition condition) {
        String testConditionName = StringUtil.concat(field.getName(), StringUtils.capitalize(condition.equals(Condition.equal) ? "" : condition.name()));
        org.dom4j.Element ifElement = this.getIfNotNullElement(testConditionName);
        this.getBaseFieldIfText(ifElement, field, condition, testConditionName);
        return ifElement;
    }

    public void getBaseFieldIfText(org.dom4j.Element ifElement, @NotNull F field, @NotNull Condition condition, String testConditionName) {
        boolean isAndStr = condition.name().indexOf("or") != 0;
        this.getIfText(ifElement, isAndStr, this.getModel().getTableName(), field.getName(), condition, testConditionName);
    }

    /**
     * @param joinField   当前model连接其他类的属性
     * @param leftModel   field对应join的类
     * @param selectField leftModel类的基本属性
     * @param condition   本次条件
     * @return org.dom4j.Element ifElement
     **/
    public org.dom4j.Element getJoinFieldIfElement(@NotNull F joinField, T leftModel, @NotNull F selectField, @NotNull Condition condition) {
        String testConditionName = StringUtil.concat(joinField.getName(), joinField.getInfix(), StringUtils.capitalize(selectField.getName()), StringUtils.capitalize(condition.equals(Condition.equal) ? "" : condition.name()));
        org.dom4j.Element ifElement = this.getIfNotNullElement(testConditionName);
        this.getJoinFieldIfText(ifElement, joinField, leftModel, selectField, condition, testConditionName);
        return ifElement;
    }

    public void getJoinFieldIfText(org.dom4j.Element ifElement, F joinField, T leftModel, @NotNull F selectField, @NotNull Condition condition, String testConditionName) {
        boolean isAndStr = condition.name().indexOf("or") != 0;
        String table = this.getJoinLeftTableAsName(joinField);
        String columnName = selectField.getName();
        this.getIfText(ifElement, isAndStr, table, columnName, condition, testConditionName);
    }

    public void getIfText(org.dom4j.Element ifElement, boolean isAndStr, String table, String columnName, Condition condition, String testConditionName) {
        String ifText = null;
        if (List.of(Condition.equal, Condition.orEqual, Condition.less, Condition.orLess, Condition.great, Condition.orGreat).contains(condition)) {
            ifText = this.getNormalIfText(table, columnName, this.getConditionStr(condition), testConditionName, isAndStr);
        } else if (List.of(Condition.like, Condition.orLike).contains(condition)) {
            ifText = this.getLikeIfText(table, columnName, testConditionName, isAndStr);
        } else if (List.of(Condition.in, Condition.orIn).contains(condition)) {
            String orAndStr = condition.name().indexOf("or") == 0 ? "or " : "and ";
            String itemText = StringUtil.concat(testConditionName, "Item");
            org.dom4j.Element forEachElement = this.getForEachElement(testConditionName, itemText, null, "(", ")", null);
            forEachElement.addText(CommonStaticField.WRAP);
            forEachElement.addText(this.getPreCompileStr(itemText));
            forEachElement.addText(CommonStaticField.WRAP);
            ifElement.addText(StringUtil.concat(CommonStaticField.WRAP, orAndStr, this.getBackQuoteStr(table), ".", this.getBackQuoteStr(columnName), " in", CommonStaticField.WRAP));
            ifElement.add(forEachElement);
            return;
        } else if (List.of(Condition.isNull, Condition.orIsNull, Condition.isNotNull, Condition.orIsNotNull).contains(condition)) {
            boolean isNull = List.of(Condition.isNull, Condition.orIsNull).contains(condition);
            ifText = this.getNullIfText(table, columnName, isAndStr, isNull);
        }
        ifElement.addText(ifText);
    }

    public String getLimit() {
        return StringUtil.concat("limit ", this.getConcatStr("pageIndex"), ",", this.getConcatStr("pageSize"));
    }

    public org.dom4j.Element getOrder() {
        org.dom4j.Element ifElement = this.generateXmlElement("if");
        ifElement.addAttribute("test", "orderColumn != null");
        ifElement.addText(StringUtil.concat("order by ", this.getBackQuoteStr(this.getConcatStr("orderColumn")), " ", this.getConcatStr("order")));
        return ifElement;
    }

    public org.dom4j.Element getResultMapElement(@NotNull F field) {
        return this.getResultMapElement(field.getName(), field.getMybatisJdbcType(), field.getName(), field.isPrimaryField());
    }

    public org.dom4j.Element getResultMapElement(String column, String jdbcType, String property, boolean isPrimary) {
        org.dom4j.Element resultElement = isPrimary ? this.generateXmlElement("id") : this.generateXmlElement("result");
        resultElement.addAttribute("column", column);
        resultElement.addAttribute("jdbcType", jdbcType);
        resultElement.addAttribute("property", property);
        return resultElement;
    }

    public org.dom4j.Element getCollectionElement(String property, String ofType) {
        org.dom4j.Element collectionElement = this.generateXmlElement("collection");
        collectionElement.addAttribute("property", property);
        collectionElement.addAttribute("ofType", ofType);
        return collectionElement;
    }

    public String getSelectText(String tableName, String columnName) {
        return StringUtil.concat(this.getBackQuoteStr(tableName), ".", this.getBackQuoteStr(columnName));
    }

    public String getSelectText(String tableName, String columnName, String columnAsName) {
        return StringUtil.concat(this.getBackQuoteStr(tableName), ".", this.getBackQuoteStr(columnName), " as ", columnAsName);
    }

    public org.dom4j.Element getIfNotNullElement(String testName) {
        org.dom4j.Element ifElement = this.generateXmlElement("if");
        ifElement.addAttribute("test", StringUtil.concat(testName, " != null"));
        return ifElement;
    }

    public org.dom4j.Element getIfSetNullElement(String testName) {
        org.dom4j.Element ifElement = this.generateXmlElement("if");
        ifElement.addAttribute("test", StringUtil.concat("set", StrUtil.upperFirst(testName), "Null", " != null"));
        return ifElement;
    }

    public String getNormalIfText(String tableName, String columnName, String conditionSeparator, String testConditionName, boolean isAndStr) {
        String orAndStr = isAndStr ? "and " : "or ";
        return StringUtil.concat(orAndStr, this.getBackQuoteStr(tableName), ".", this.getBackQuoteStr(columnName), conditionSeparator, this.getPreCompileStr(testConditionName));
    }

    public String getLikeIfText(String tableName, String columnName, String testConditionName, boolean isAndStr) {
        String orAndStr = isAndStr ? "and " : "or ";
        return StringUtil.concat(orAndStr, "instr(", this.getBackQuoteStr(tableName), ".", this.getBackQuoteStr(columnName), ",", this.getPreCompileStr(testConditionName), ")");
    }

    public String getNullIfText(String tableName, String columnName, boolean isAndStr, boolean isNull) {
        String orAndStr = isAndStr ? "and " : "or ";
        String endStr = isNull ? " is null" : " is not null";
        return StringUtil.concat(orAndStr, this.getBackQuoteStr(tableName), ".", this.getBackQuoteStr(columnName), endStr);
    }

    public org.dom4j.Element getForEachElement(String collection, String item, String index, String open, String close, String separator) {
        if (StringUtil.isEmpty(item)) {
            item = "item";
        }
        if (StringUtil.isEmpty(index)) {
            index = "index";
        }
        if (StringUtil.isEmpty(separator)) {
            separator = ",";
        }
        org.dom4j.Element forEachElement = this.generateXmlElement("foreach");
        forEachElement.addAttribute("collection", collection);
        forEachElement.addAttribute("item", item);
        forEachElement.addAttribute("index", index);
        if (StringUtil.isNotEmpty(open)) {
            forEachElement.addAttribute("open", open);
        }
        if (StringUtil.isNotEmpty(close)) {
            forEachElement.addAttribute("close", close);
        }
        forEachElement.addAttribute("separator", separator);
        return forEachElement;
    }

    public String getConditionStr(@NotNull Condition condition) {
        if (condition.equals(Condition.equal) || condition.equals(Condition.orEqual)) {
            return " = ";
        } else if (condition.equals(Condition.less) || condition.equals(Condition.orLess)) {
            return " < ";
        } else if (condition.equals(Condition.great) || condition.equals(Condition.orGreat)) {
            return " > ";
        }
        return null;
    }

    public String getBackQuoteStr(String str) {
        return StringUtil.concat("`", str, "`");
    }

    public String getPreCompileStr(String str) {
        return StringUtil.concat("#{", str, "}");
    }

    public String getConcatStr(String str) {
        return StringUtil.concat("${", str, "}");
    }
}
