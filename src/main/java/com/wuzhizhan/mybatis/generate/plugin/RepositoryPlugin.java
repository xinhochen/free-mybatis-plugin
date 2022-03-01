package com.wuzhizhan.mybatis.generate.plugin;

import java.util.List;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;

public class RepositoryPlugin extends PluginAdapter {
    private final FullyQualifiedJavaType annotationRepository;

    private String annotation = "@Repository";

    public RepositoryPlugin() {
        this.annotationRepository = new FullyQualifiedJavaType("org.springframework.stereotype.Repository");
    }

    public boolean validate(List<String> list) {
        return true;
    }

    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        interfaze.addImportedType(this.annotationRepository);
        interfaze.addAnnotation(this.annotation);
        return true;
    }
}
