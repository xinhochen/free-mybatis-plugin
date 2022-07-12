package com.wuzhizhan.mybatis.generate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.PsiMethod;
import com.wuzhizhan.mybatis.setting.MybatisSetting;
import com.wuzhizhan.mybatis.util.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class StatementGenerators {
    public static final StatementGenerator UPDATE_GENERATOR = new UpdateGenerator("update", "modify", "set");

    public static final StatementGenerator SELECT_GENERATOR = new SelectGenerator("select", "get", "look", "find", "list", "search", "count", "query");

    public static final StatementGenerator DELETE_GENERATOR = new DeleteGenerator("del", "cancel");

    public static final StatementGenerator INSERT_GENERATOR = new InsertGenerator("insert", "add", "new");

    public static final Set<StatementGenerator> ALL = ImmutableSet.of(UPDATE_GENERATOR, SELECT_GENERATOR, DELETE_GENERATOR, INSERT_GENERATOR);

    public static void applyGenerate(@Nullable final PsiMethod method) {
        if (null == method) return;
        final Project project = method.getProject();
        final Object[] generators = getGenerators(method);
        if (1 == generators.length) {
            ((StatementGenerator) generators[0]).execute(method);
        } else {
            JBPopupFactory.getInstance().createListPopup(
                    new BaseListPopupStep<Object>("[ Statement type for method: " + method.getName() + "]", generators) {
                        @Override
                        public PopupStep<?> onChosen(Object selectedValue, boolean finalChoice) {
                            return this.doFinalStep(() -> WriteCommandAction.runWriteCommandAction(project, () ->
                                    doGenerate((StatementGenerator) selectedValue, method)));
                        }
                    }
            ).showInFocusCenter();
        }
    }

    @NotNull
    public static StatementGenerator[] getGenerators(@NotNull PsiMethod method) {
        GenerateModel model = MybatisSetting.getInstance().getStatementGenerateModel();
        String target = method.getName();
        List<StatementGenerator> result = Lists.newArrayList();
        for (StatementGenerator generator : ALL) {
            if (model.matchesAny(generator.getPatterns(), target)) {
                result.add(generator);
            }
        }
        return CollectionUtils.isNotEmpty(result) ? result.toArray(new StatementGenerator[0]) : ALL.toArray(new StatementGenerator[0]);
    }

    private static void doGenerate(@NotNull final StatementGenerator generator, @NotNull final PsiMethod method) {
        WriteCommandAction.writeCommandAction(method.getProject(), method.getContainingFile()).run(() -> {
            generator.execute(method);
        });
    }
}
