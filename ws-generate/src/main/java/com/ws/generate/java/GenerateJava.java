package com.ws.generate.java;

import com.ws.generate.GenerateInfo;

public abstract class GenerateJava implements GenerateInfo {

    public abstract String getModelCode();

    public abstract String getMapperCode();

    public abstract String getServiceCode();

    public abstract String getControllerCode();

    public abstract boolean writeModel();

    public abstract boolean writeMapper();

    public abstract boolean writeService();

    public abstract boolean writeController();

}
