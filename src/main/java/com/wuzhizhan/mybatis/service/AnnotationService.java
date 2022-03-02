package com.wuzhizhan.mybatis.service;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.wuzhizhan.mybatis.annotation.Annotation;
import com.wuzhizhan.mybatis.util.JavaUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author yanglin
 */
public class AnnotationService {

    private final Project project;

    public AnnotationService(Project project) {
        this.project = project;
    }

    public static AnnotationService getInstance(@NotNull Project project) {
        return project.getService(AnnotationService.class);
    }

    public void addAnnotation(@NotNull PsiModifierListOwner parameter, @NotNull Annotation annotation) {
        PsiModifierList modifierList = parameter.getModifierList();
        if (JavaUtils.isAnnotationPresent(parameter, annotation) || null == modifierList) {
            return;
        }
        JavaService.getInstance(parameter.getProject()).importClazz((PsiJavaFile) parameter.getContainingFile(), annotation.getQualifiedName());

        PsiElementFactory elementFactory = JavaPsiFacade.getInstance(project).getElementFactory();
        PsiAnnotation psiAnnotation = elementFactory.createAnnotationFromText(annotation.toString(), parameter);
        modifierList.add(psiAnnotation);
        JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiAnnotation.getParent());
    }

    public void addAnnotationWithParameterNameForMethodParameters(@NotNull PsiMethod method) {
        PsiParameterList parameterList = method.getParameterList();
        if (null == parameterList) {
            return;
        }
        PsiParameter[] parameters = parameterList.getParameters();
        for (PsiParameter param : parameters) {
            addAnnotationWithParameterName(param);
        }
    }

    public void addAnnotationWithParameterName(@NotNull PsiParameter parameter) {
        String name = parameter.getName();
        if (null != name) {
            AnnotationService.getInstance(parameter.getProject()).addAnnotation(parameter, Annotation.PARAM.withValue(new Annotation.StringValue(name)));
        }
    }
}
