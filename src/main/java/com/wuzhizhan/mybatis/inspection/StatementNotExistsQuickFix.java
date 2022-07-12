package com.wuzhizhan.mybatis.inspection;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.wuzhizhan.mybatis.generate.StatementGenerators;
import org.jetbrains.annotations.NotNull;

/**
 * @author yanglin
 */
public class StatementNotExistsQuickFix extends GenericQuickFix {

    @SafeFieldForPreview
    private final SmartPsiElementPointer<PsiMethod> method;

    public StatementNotExistsQuickFix(@NotNull PsiMethod method) {
        PsiFile containingFile = method.getContainingFile();
        Project project = containingFile == null ? method.getProject() : containingFile.getProject();
        this.method = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(method, containingFile);
    }

    @NotNull
    @Override
    public String getName() {
        return "MybatisGenerator statement";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        StatementGenerators.applyGenerate(method.getElement());
    }

}
