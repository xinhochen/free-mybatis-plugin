package com.wuzhizhan.mybatis.alias;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.wuzhizhan.mybatis.annotation.Annotation;
import com.wuzhizhan.mybatis.util.JavaUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * @author yanglin
 */
public class AnnotationAliasResolver extends AliasResolver {

    private static final Function<PsiClass, AliasDesc> FUN = psiClass -> {
        Optional<String> txt = JavaUtils.getAnnotationValueText(psiClass, Annotation.ALIAS);
        if (txt.isEmpty()) return null;
        AliasDesc ad = new AliasDesc();
        ad.setAlias(txt.get());
        ad.setClazz(psiClass);
        return ad;
    };

    public AnnotationAliasResolver(Project project) {
        super(project);
    }

    @NotNull
    @Override
    public Set<AliasDesc> getClassAliasDescriptions(@Nullable PsiElement element) {
        Optional<PsiClass> clazz = Annotation.ALIAS.toPsiClass(project);
        if (clazz.isPresent()) {
            Collection<PsiClass> res = AnnotatedElementsSearch.searchPsiClasses(clazz.get(), GlobalSearchScope.allScope(project)).findAll();
            return Sets.newHashSet(Collections2.transform(res, FUN));
        }
        return Collections.emptySet();
    }

}
