package com.ws.generate.java;

import cn.hutool.core.io.FileUtil;
import com.squareup.javapoet.*;
import com.ws.annotation.Column;
import com.ws.annotation.Data;
import com.ws.annotation.Join;
import com.ws.base.controller.AbstractBaseDataControllerString;
import com.ws.base.controller.BaseDataController;
import com.ws.base.mapper.BaseDataMapper;
import com.ws.base.model.BaseModel;
import com.ws.base.service.AbstractBaseDataService;
import com.ws.enu.Condition;
import com.ws.enu.JoinCondition;
import com.ws.enu.JoinType;
import com.ws.exception.MessageException;
import com.ws.generate.metadata.field.ColumnInfo;
import com.ws.generate.metadata.model.ModelInfo;
import com.ws.tool.GenerateJavaUtil;
import com.ws.tool.StringUtil;
import jakarta.annotation.Resource;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.ws.tool.CommonStaticField.*;

@EqualsAndHashCode(callSuper = true)
@lombok.Data
public class GenerateJavaMMSC<T extends ModelInfo<?, F>, F extends ColumnInfo<?, T>> extends GenerateJava {

    private T model;
    private Consumer<MessageException> message;
    private Class<? extends BaseDataController> controllerSuperClazz = AbstractBaseDataControllerString.class;

    private String modelCode;
    private String mapperCode;
    private String serviceCode;
    private String controllerCode;

    public GenerateJavaMMSC(T model, Consumer<MessageException> message) {
        this.model = model;
        this.message = message;
    }

    public GenerateJavaMMSC(T model) {
        this.model = model;
    }

    public TypeSpec generateModelClass() {
        TypeSpec.Builder typeSpec = TypeSpec.classBuilder(this.getModel().getModelName()).addModifiers(Modifier.PUBLIC).superclass(BaseModel.class);

        AnnotationSpec dataAnnotation = GenerateJavaUtil.generateAnnotationSpec(Data.class);
        typeSpec.addAnnotation(dataAnnotation);

        AnnotationSpec lombokDataAnnotation = GenerateJavaUtil.generateAnnotationSpec(lombok.Data.class);
        typeSpec.addAnnotation(lombokDataAnnotation);

        AnnotationSpec.Builder accessorsAnnotation = GenerateJavaUtil.generateAnnotationBuilder(Accessors.class);
        accessorsAnnotation.addMember("chain", "$L", true);

        typeSpec.addAnnotation(accessorsAnnotation.build());

        AnnotationSpec.Builder equalsAndHashCodeAnnotation = GenerateJavaUtil.generateAnnotationBuilder(EqualsAndHashCode.class);
        equalsAndHashCodeAnnotation.addMember("callSuper", "$L", true);

        typeSpec.addAnnotation(equalsAndHashCodeAnnotation.build());

        for (F item : this.getModel().getFields()) {
            FieldSpec.Builder fieldSpec = null;
            if (item.isBaseField()) {
                try {
                    Class<?> clazz = Class.forName(item.getJavaTypeName());
                    fieldSpec = GenerateJavaUtil.generateFieldBuilder(TypeName.get(clazz), item.getName(), Modifier.PRIVATE);
                    AnnotationSpec.Builder columnAnnotation = GenerateJavaUtil.generateAnnotationBuilder(Column.class);
                    columnAnnotation.addMember("title", "$S", item.getTitle());
                    columnAnnotation.addMember("comment", "$S", item.getComment());
                    columnAnnotation.addMember("conditions", "$T.all", Condition.class);
                    columnAnnotation.addMember("primary", "$L", item.isPrimaryField());
                    fieldSpec.addAnnotation(columnAnnotation.build());
                } catch (ClassNotFoundException e) {
                    this.printError(StringUtil.concat("加载类: ", item.getJavaTypeName(), " 失败"));
                }
            } else if (item.isCollectionJoinField()) {
                ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(List.class, BaseModel.class);
                fieldSpec = GenerateJavaUtil.generateFieldBuilder(parameterizedTypeName, item.getName(), Modifier.PRIVATE);
                AnnotationSpec.Builder joinAnnotation = GenerateJavaUtil.generateAnnotationBuilder(Join.class);
                joinAnnotation.addMember("leftTable", "$S.class", item.getLeftModel().getModelName());
                joinAnnotation.addMember("leftJoinField", "$S", item.getLeftJoinField());
                joinAnnotation.addMember("leftSelectFields", "{$S}", "*");
                joinAnnotation.addMember("rightTable", "$S.class", item.getRightModel().getModelName());
                joinAnnotation.addMember("rightJoinField", "$S", item.getRightJoinField());
                joinAnnotation.addMember("joinType", "$T.$L", JoinType.class, item.getJoinType().name());
                joinAnnotation.addMember("joinCondition", "$T.$L", JoinCondition.class, item.getJoinCondition().name());
                joinAnnotation.addMember("infix", "$S", item.getInfix());
                fieldSpec.addAnnotation(joinAnnotation.build());
            } else {
                fieldSpec = GenerateJavaUtil.generateFieldBuilder(TypeName.get(BaseModel.class), item.getName(), Modifier.PRIVATE);
                AnnotationSpec.Builder joinAnnotation = GenerateJavaUtil.generateAnnotationBuilder(Join.class);
                joinAnnotation.addMember("leftTable", "$S.class", item.getLeftModel().getModelName());
                joinAnnotation.addMember("leftJoinField", "$S", item.getLeftJoinField());
                joinAnnotation.addMember("leftSelectFields", "{$S}", "*");
                joinAnnotation.addMember("rightTable", "$S.class", item.getRightModel().getModelName());
                joinAnnotation.addMember("rightJoinField", "$S", item.getRightJoinField());
                joinAnnotation.addMember("joinType", "$T.$L", JoinType.class, item.getJoinType().name());
                joinAnnotation.addMember("joinCondition", "$T.$L", JoinCondition.class, item.getJoinCondition().name());
                joinAnnotation.addMember("infix", "$S", item.getInfix());
                fieldSpec.addAnnotation(joinAnnotation.build());
            }
            if (Objects.nonNull(fieldSpec)) {
                typeSpec.addField(fieldSpec.build());
            }
        }

        return typeSpec.build();
    }

    public TypeSpec generateMapperInterface() {
        TypeSpec.Builder typeSpec = TypeSpec.interfaceBuilder(this.getModel().getMapperName()).addModifiers(Modifier.PUBLIC);

        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(BaseDataMapper.class, BaseModel.class);
        typeSpec.addSuperinterface(parameterizedTypeName);

        AnnotationSpec mapperAnnotation = GenerateJavaUtil.generateAnnotationSpec(Mapper.class);
        typeSpec.addAnnotation(mapperAnnotation);

        return typeSpec.build();
    }

    @SneakyThrows
    public TypeSpec generateServiceClass() {
        Class<?> primaryFieldClazz = Class.forName(this.getModel().getPrimaryField().getJavaTypeName());

        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(AbstractBaseDataService.class, primaryFieldClazz, BaseDataMapper.class, BaseModel.class);
        TypeSpec.Builder typeSpec = TypeSpec.classBuilder(this.getModel().getServiceName()).addModifiers(Modifier.PUBLIC).superclass(parameterizedTypeName);

        String mapper = StringUtil.concat(this.getModel().getMapperName().substring(0, 1).toLowerCase(), this.getModel().getMapperName().substring(1));

        FieldSpec clazzDeclare = GenerateJavaUtil.generateFieldBuilder(TypeName.get(BaseDataMapper.class), mapper, Modifier.PUBLIC).addAnnotation(Resource.class).build();
        typeSpec.addField(clazzDeclare);

        MethodSpec getMapperMethod = GenerateJavaUtil.generateMethodBuilder("getMapper", TypeName.get(BaseDataMapper.class), Override.class, Modifier.PUBLIC).addCode(StringUtil.concat("return ", mapper, ";")).build();
        typeSpec.addMethod(getMapperMethod);

        AnnotationSpec serviceAnnotation = GenerateJavaUtil.generateAnnotationSpec(Service.class);
        typeSpec.addAnnotation(serviceAnnotation);

//        AnnotationSpec.Builder transactionalAnnotation = GenerateJavaUtil.generateAnnotationBuilder(Transactional.class);
//        transactionalAnnotation.addMember("rollbackFor", "$T.class", Exception.class);
//        typeSpec.addAnnotation(transactionalAnnotation.build());

        return typeSpec.build();
    }

    public TypeSpec generateControllerClass() {
        TypeSpec.Builder typeSpec = TypeSpec.classBuilder(this.getModel().getControllerName()).addModifiers(Modifier.PUBLIC).superclass(ParameterizedTypeName.get(controllerSuperClazz, AbstractBaseDataService.class, BaseModel.class));

        String service = StringUtil.concat(this.getModel().getServiceName().substring(0, 1).toLowerCase(), this.getModel().getServiceName().substring(1));

        FieldSpec clazzDeclare = GenerateJavaUtil.generateFieldBuilder(TypeName.get(AbstractBaseDataService.class), service, Modifier.PUBLIC).addAnnotation(Resource.class).build();
        typeSpec.addField(clazzDeclare);

        MethodSpec getServiceMethod = GenerateJavaUtil.generateMethodBuilder("getService", TypeName.get(AbstractBaseDataService.class), Override.class, Modifier.PUBLIC).addCode(StringUtil.concat("return ", service, ";")).build();
        typeSpec.addMethod(getServiceMethod);

        MethodSpec getModelMethod = GenerateJavaUtil.generateMethodBuilder("getModel", TypeName.get(BaseModel.class), Override.class, Modifier.PUBLIC).addCode(StringUtil.concat("return new ", BASE_MODEL_CLAZZ_SIMPLE_NAME, "();")).build();
        typeSpec.addMethod(getModelMethod);

//        AnnotationSpec.Builder transactionalAnnotation = GenerateJavaUtil.generateAnnotationBuilder(Transactional.class);
//        transactionalAnnotation.addMember("rollbackFor", "{$T.$L}", Exception.class, "class");
//        typeSpec.addAnnotation(transactionalAnnotation.build());

        AnnotationSpec restControllerAnnotation = GenerateJavaUtil.generateAnnotationSpec(RestController.class);
        typeSpec.addAnnotation(restControllerAnnotation);

        AnnotationSpec.Builder requestMappingAnnotation = GenerateJavaUtil.generateAnnotationBuilder(RequestMapping.class);
        requestMappingAnnotation.addMember("value", "$S", StringUtil.concat("/", this.getModel().getModelName()));
        typeSpec.addAnnotation(requestMappingAnnotation.build());

        return typeSpec.build();
    }

    @Override
    public Consumer<MessageException> getMessage() {
        return this.message;
    }

    public boolean writeModel(String path) {
        if (StringUtil.isEmpty(this.getModelCode())) {
            this.generateModelCode();
        }
        if (StringUtil.isEmpty(this.getModelCode())) {
            return false;
        }
        File file = FileUtil.touch(path);
        file.deleteOnExit();
        FileUtil.writeString(this.getModelCode(), file, StandardCharsets.UTF_8);
        return true;
    }

    public boolean writeMapper(String path) {
        if (StringUtil.isEmpty(this.getMapperCode())) {
            this.generateMapperCode();
        }
        if (StringUtil.isEmpty(this.getMapperCode())) {
            return false;
        }
        File file = FileUtil.touch(path);
        file.deleteOnExit();
        FileUtil.writeString(this.getMapperCode(), file, StandardCharsets.UTF_8);
        return true;
    }

    public boolean writeService(String path) {
        if (StringUtil.isEmpty(this.getServiceCode())) {
            this.generateServiceCode();
        }
        if (StringUtil.isEmpty(this.getServiceCode())) {
            return false;
        }
        File file = FileUtil.touch(path);
        file.deleteOnExit();
        FileUtil.writeString(this.getServiceCode(), file, StandardCharsets.UTF_8);
        return true;
    }

    public boolean writeController(String path) {
        if (StringUtil.isEmpty(this.getControllerCode())) {
            this.generateControllerCode();
        }
        if (StringUtil.isEmpty(this.getControllerCode())) {
            return false;
        }
        File file = FileUtil.touch(path);
        file.deleteOnExit();
        FileUtil.writeString(this.getControllerCode(), file, StandardCharsets.UTF_8);
        return true;
    }

    public String generateModelCode() {
        TypeSpec model = this.generateModelClass();
        try {
            String modelCode = GenerateJavaUtil.getJavaCode(this.getModel().getModelFullName(), model).replaceAll(StringUtil.concat("package ", this.getModel().getModelFullName()), StringUtil.concat("package ", this.getModel().getModelPackageName()));
            for (F item : this.getModel().getFields()) {
                if (item.isCollectionJoinField()) {
                    modelCode = modelCode.replace(StringUtil.concat("\"", item.getLeftModel().getModelName(), "\""), item.getLeftModel().getModelName());
                    modelCode = modelCode.replace(StringUtil.concat("\"", item.getRightModel().getModelName(), "\""), item.getRightModel().getModelName());
                    modelCode = modelCode.replace(StringUtil.concat("List<BaseModel> ", item.getName()), StringUtil.concat("List<", item.getLeftModel().getModelName(), "> ", item.getName()));
                } else if (item.isClassJoinField()) {
                    modelCode = modelCode.replace(StringUtil.concat("\"", item.getLeftModel().getModelName(), "\""), item.getLeftModel().getModelName());
                    modelCode = modelCode.replace(StringUtil.concat("\"", item.getRightModel().getModelName(), "\""), item.getRightModel().getModelName());
                    modelCode = modelCode.replace(StringUtil.concat("BaseModel ", item.getName()), StringUtil.concat(item.getLeftModel().getModelName(), " ", item.getName()));
                }
            }
            this.setModelCode(modelCode);
            return modelCode;
        } catch (IOException e) {
            this.printError(StringUtil.concat("写入model失败:", this.getModel().getModelName(), ",失败原因: ", e.getMessage()), e);
        }
        return null;
    }

    public String generateMapperCode() {
        TypeSpec mapper = this.generateMapperInterface();
        try {
            String mapperCode = GenerateJavaUtil.getJavaCode(this.getModel().getMapperFullName(), mapper).replaceAll(StringUtil.concat("package ", this.getModel().getMapperFullName()), StringUtil.concat("package ", this.getModel().getMapperPackageName())).replaceAll(BASE_MODEL_PACKAGE_NAME, this.getModel().getModelFullName()).replaceAll(BASE_MODEL_CLAZZ_SIMPLE_NAME, this.getModel().getModelName());
            this.setMapperCode(mapperCode);
            return mapperCode;
        } catch (IOException e) {
            this.printError(StringUtil.concat("获取mapper失败,对应的model类是:", this.getModel().getModelName(), ",失败原因: ", e.getMessage()), e);
        }
        return null;
    }

    public String generateServiceCode() {
        TypeSpec service = this.generateServiceClass();
        try {
            String serviceCode = GenerateJavaUtil.getJavaCode(this.getModel().getServiceFullName(), service).replaceAll(StringUtil.concat("package ", this.getModel().getServiceFullName()), StringUtil.concat("package ", this.getModel().getServicePackageName())).replaceAll(BASE_MAPPER_PACKAGE_NAME, this.getModel().getMapperFullName()).replaceAll(BASE_MAPPER_CLAZZ_SIMPLE_NAME, this.getModel().getMapperName()).replaceAll(BASE_MODEL_PACKAGE_NAME, this.getModel().getModelFullName()).replaceAll(BASE_MODEL_CLAZZ_SIMPLE_NAME, this.getModel().getModelName());
            this.setServiceCode(serviceCode);
            return serviceCode;
        } catch (IOException e) {
            this.printError(StringUtil.concat("获取service失败,对应的model类是:", this.getModel().getModelName(), ",失败原因: ", e.getMessage()), e);
        }
        return null;
    }

    public String generateControllerCode() {
        TypeSpec controller = this.generateControllerClass();
        try {
            String controllerCode = GenerateJavaUtil.getJavaCode(this.getModel().getControllerFullName(), controller).replaceAll(StringUtil.concat("package ", this.getModel().getControllerFullName()), StringUtil.concat("package ", this.getModel().getControllerPackageName())).replaceAll(BASE_MODEL_PACKAGE_NAME, this.getModel().getModelFullName()).replaceAll(BASE_DATA_SERVICE_IMPL_PACKAGE_NAME, this.getModel().getServiceFullName()).replaceAll(BASE_DATA_SERVICE_IMPL_CLAZZ_SIMPLE_NAME, this.getModel().getServiceName()).replaceAll(BASE_MODEL_CLAZZ_SIMPLE_NAME, this.getModel().getModelName());
            this.setControllerCode(controllerCode);
            return controllerCode;
        } catch (IOException e) {
            this.printWarn(StringUtil.concat("写入controller失败,对应的model类是:", this.getModel().getModelName(), ",失败原因: ", e.getMessage()));
        }
        return null;
    }

    @Override
    public boolean writeModel() {
        return this.writeModel(this.getModel().getGenerateModelFilePath());
    }

    @Override
    public boolean writeMapper() {
        return this.writeMapper(this.getModel().getGenerateMapperFilePath());
    }

    @Override
    public boolean writeService() {
        return this.writeService(this.getModel().getGenerateServiceFilePath());
    }

    @Override
    public boolean writeController() {
        return this.writeController(this.getModel().getGenerateControllerFilePath());
    }

}
