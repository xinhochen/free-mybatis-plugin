package com.wuzhizhan.mybatis.inspection;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.wuzhizhan.mybatis.generate.StatementGenerator;
import org.jetbrains.annotations.NotNull;

/**
 * @author yanglin
 */
public class StatementNotExistsQuickFix extends GenericQuickFix {

    private PsiMethod method;

    public StatementNotExistsQuickFix(@NotNull PsiMethod method) {
        this.method = method;
    }

    @NotNull
    @Override
    public String getName() {
        return "MybatisGenerator statement";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        StatementGenerator.applyGenerate(method);
    }

    @NotNull
    public PsiMethod getMethod() {
        return method;
    }

    public void setMethod(@NotNull PsiMethod method) {
        this.method = method;
    }

}
