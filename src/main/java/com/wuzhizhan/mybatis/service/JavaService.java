package com.wuzhizhan.mybatis.service;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import com.intellij.util.xml.DomElement;
import com.wuzhizhan.mybatis.dom.model.IdDomElement;
import com.wuzhizhan.mybatis.dom.model.Mapper;
import com.wuzhizhan.mybatis.util.JavaUtils;
import com.wuzhizhan.mybatis.util.MapperUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @author yanglin
 */
public class JavaService {

    private final Project project;

    private final JavaPsiFacade javaPsiFacade;

    private final EditorService editorService;

    public JavaService(Project project) {
        this.project = project;
        this.javaPsiFacade = JavaPsiFacade.getInstance(project);
        this.editorService = EditorService.getInstance(project);
    }

    public static JavaService getInstance(@NotNull Project project) {
        return project.getService(JavaService.class);
    }

    public Optional<PsiClass> getReferenceClazzOfPsiField(@NotNull PsiElement field) {
        if (!(field instanceof PsiField)) {
            return Optional.empty();
        }
        PsiType type = ((PsiField) field).getType();
        return type instanceof PsiClassReferenceType ? Optional.ofNullable(((PsiClassReferenceType) type).resolve()) : Optional.empty();
    }

    public Optional<DomElement> findStatement(@Nullable PsiMethod method) {
        if (method == null) {
            return Optional.empty();
        }
        CommonProcessors.FindFirstProcessor<DomElement> processor = new CommonProcessors.FindFirstProcessor<>();
        process(method, processor);
        return processor.isFound() ? Optional.ofNullable(processor.getFoundValue()) : Optional.empty();
    }

    public void processMethod(@NotNull PsiMethod psiMethod, @NotNull Processor<IdDomElement> processor) {
        PsiClass psiClass = psiMethod.getContainingClass();
        if (null == psiClass) return;
        String id = psiClass.getQualifiedName() + "." + psiMethod.getName();
        for (Mapper mapper : MapperUtils.findMappers(psiMethod.getProject())) {
            for (IdDomElement idDomElement : mapper.getDaoElements()) {
                if (MapperUtils.getIdSignature(idDomElement).equals(id)) {
                    processor.process(idDomElement);
                }
            }
        }
    }

    public void processClass(@NotNull PsiClass clazz, @NotNull Processor<Mapper> processor) {
        String ns = clazz.getQualifiedName();
        for (Mapper mapper : MapperUtils.findMappers(clazz.getProject())) {
            if (MapperUtils.getNamespace(mapper).equals(ns)) {
                processor.process(mapper);
            }
        }
    }

    public void process(@NotNull PsiElement target, @NotNull Processor processor) {
        if (target instanceof PsiMethod) {
            processMethod((PsiMethod) target, processor);
        } else if (target instanceof PsiClass) {
            processClass((PsiClass) target, processor);
        }
    }

    public <T> Optional<T> findWithFindFirstProcessor(@NotNull PsiElement target) {
        CommonProcessors.FindFirstProcessor<T> processor = new CommonProcessors.FindFirstProcessor<T>();
        process(target, processor);
        return Optional.ofNullable(processor.getFoundValue());
    }

    public void importClazz(PsiJavaFile file, String clazzName) {
        if (!JavaUtils.hasImportClazz(file, clazzName)) {
            Optional<PsiClass> clazz = JavaUtils.findClazz(project, clazzName);
            PsiImportList importList = file.getImportList();
            if (clazz.isPresent() && null != importList) {
                PsiElementFactory elementFactory = javaPsiFacade.getElementFactory();
                PsiImportStatement statement = elementFactory.createImportStatement(clazz.get());
                importList.add(statement);
                editorService.format(file, statement);
            }
        }
    }
}

