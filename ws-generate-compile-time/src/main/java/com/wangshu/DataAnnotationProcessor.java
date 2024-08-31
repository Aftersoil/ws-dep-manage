package com.wangshu;

import com.google.auto.service.AutoService;
import com.wangshu.annotation.Data;
import com.wangshu.enu.MessageType;
import com.wangshu.exception.MessageException;
import com.wangshu.generate.java.GenerateJavaMMSC;
import com.wangshu.generate.metadata.field.ColumnElementInfo;
import com.wangshu.generate.metadata.model.ModelElementInfo;
import com.wangshu.generate.metadata.module.ModuleTemplateInfo;
import com.wangshu.generate.xml.GenerateXml;
import com.wangshu.generate.xml.GenerateXmlMysql;
import com.wangshu.tool.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author GSF
 */
@SupportedAnnotationTypes("com.wangshu.annotation.Data")
@AutoService(Processor.class)
public class DataAnnotationProcessor extends AbstractProcessor {

    private Messager messager;
    private Filer filer;

    private final URL path = DataAnnotationProcessor.class.getClassLoader().getResource("");

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        if (SourceVersion.latest().compareTo(SourceVersion.RELEASE_8) > 0) {
            return SourceVersion.latest();
        }
        return SourceVersion.RELEASE_8;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, @NotNull RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return true;
        }
        try {
            Consumer<MessageException> messageExceptionConsumer = (MessageException message) -> {
                if (message.getMessageType().equals(MessageType.ERROR)) {
                    this.printError(message.getMessage());
                } else if (message.getMessageType().equals(MessageType.WARN)) {
                    this.printWarn(message.getMessage());
                } else if (message.getMessageType().equals(MessageType.NOTE)) {
                    this.printNote(message.getMessage());
                }
            };
            assert this.path != null;
            String classesPath = this.path.getPath();
            String modulePath = classesPath.replace("target/classes/", "");
            String[] dirNamePath = modulePath.replaceFirst("/", "").split("/");
            String moduleName = dirNamePath[dirNamePath.length - 1];
            ModuleTemplateInfo moduleTemplateInfo = null;
            for (Element element : roundEnv.getElementsAnnotatedWith(Data.class)) {
                if (Objects.isNull(moduleTemplateInfo)) {
                    String modulePackageName = element.asType().toString().replace(StringUtil.concat(".model.", element.getSimpleName().toString()), "");
                    moduleTemplateInfo = new ModuleTemplateInfo(moduleName, modulePackageName, modulePath);
                    cn.hutool.core.io.FileUtil.del(moduleTemplateInfo.getModuleGeneratePath());
                }
                Data data = element.getAnnotation(Data.class);
                if (Objects.nonNull(data)) {
                    ModelElementInfo modelElementInfo = new ModelElementInfo(moduleTemplateInfo, element);
                    GenerateJavaMMSC<ModelElementInfo, ColumnElementInfo> generateJava = new GenerateJavaMMSC<>(modelElementInfo, messageExceptionConsumer);
                    GenerateXml<ModelElementInfo, ColumnElementInfo> generateXml = new GenerateXmlMysql<>(modelElementInfo, messageExceptionConsumer);
                    generateJava.writeMapper();
                    if (!cn.hutool.core.io.FileUtil.exist(modelElementInfo.getMapperFilePath())) {
                        this.writeJavaSourceFile(modelElementInfo.getMapperFullName(), generateJava.getMapperCode());
                    }
                    generateJava.writeService();
                    if (!cn.hutool.core.io.FileUtil.exist(modelElementInfo.getServiceFilePath())) {
                        this.writeJavaSourceFile(modelElementInfo.getServiceFullName(), generateJava.getServiceCode());
                    }
                    generateJava.writeController();
                    if (!cn.hutool.core.io.FileUtil.exist(modelElementInfo.getControllerFilePath())) {
                        this.writeJavaSourceFile(modelElementInfo.getControllerFullName(), generateJava.getControllerCode());
                    }
                    generateXml.writeXml();
                }
            }
            if (Objects.nonNull(moduleTemplateInfo)) {
                copyFolderToFolder(new File(moduleTemplateInfo.getModuleGeneratePath()).getAbsolutePath(), new File(moduleTemplateInfo.getModulePath()).getAbsolutePath(), false);
                if (!cn.hutool.core.io.FileUtil.exist(moduleTemplateInfo.getModuleCompileClassesXmlPath())) {
                    copyFolderToFolder(new File(moduleTemplateInfo.getModuleGenerateXmlPath()).getAbsolutePath(), new File(moduleTemplateInfo.getModuleCompileClassesXmlPath()).getAbsolutePath(), false);
                }
            }
        } catch (Exception e) {
            this.printError("生成失败");
            this.printError(e.getMessage());
            return false;
        }
        return true;
    }

    private void copyFolderToFolder(String folder1, String folder2, boolean coverExistFile) throws IOException {
        new File(folder2).mkdirs();
        File fileList = new File(folder1);
        String[] fileNameArr = fileList.list();
        File temp;
        if (Objects.isNull(fileNameArr)) {
            return;
        }
        for (String fileName : fileNameArr) {
            if (folder1.endsWith(File.separator)) {
                temp = new File(StringUtil.concat(folder1, fileName));
            } else {
                temp = new File(StringUtil.concat(folder1, File.separator, fileName));
            }
            if (temp.isFile()) {
                File file = new File(StringUtil.concat(folder2, File.separator, temp.getName()));
                if (!file.exists() || coverExistFile) {
                    Files.copy(Path.of(temp.getAbsolutePath()), new FileOutputStream(file));
                }
            } else if (temp.isDirectory()) {
                copyFolderToFolder(StringUtil.concat(folder1, File.separator, fileName), StringUtil.concat(folder2, File.separator, fileName), coverExistFile);
            }
        }
    }

    private void writeJavaSourceFile(String classFullName, String code) throws IOException {
        JavaFileObject sourceFile = this.filer.createSourceFile(classFullName);
        try (Writer writer = sourceFile.openWriter()) {
            writer.write(code);
        }
    }

    private void printNote(String message) {
        this.printMessage(Diagnostic.Kind.NOTE, message);
    }

    private void printWarn(String message) {
        this.printMessage(Diagnostic.Kind.WARNING, message);
    }

    private void printError(String message) {
        this.printMessage(Diagnostic.Kind.ERROR, message);
    }

    private void printMessage(Diagnostic.Kind kind, String message) {
        if (Objects.nonNull(message)) {
            this.messager.printMessage(kind, message);
        }
    }

}
