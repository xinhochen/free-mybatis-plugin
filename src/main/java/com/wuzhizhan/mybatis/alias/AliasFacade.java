package com.wuzhizhan.mybatis.alias;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author yanglin
 */
public class AliasFacade {

    private final Project project;

    private final JavaPsiFacade javaPsiFacade;

    private final List<AliasResolver> resolvers;

    public static AliasFacade getInstance(@NotNull Project project) {
        return project.getService(AliasFacade.class);
    }

    public AliasFacade(Project project) {
        this.project = project;
        this.resolvers = Lists.newArrayList();
        this.javaPsiFacade = JavaPsiFacade.getInstance(project);
        initResolvers();
    }

    private void initResolvers() {
        try {
            Class.forName("com.intellij.spring.model.utils.SpringModelUtils");
            this.registerResolver(AliasResolverFactory.createBeanResolver(project));
        } catch (ClassNotFoundException e) {
        }
        this.registerResolver(AliasResolverFactory.createSingleAliasResolver(project));
        this.registerResolver(AliasResolverFactory.createConfigPackageResolver(project));
        this.registerResolver(AliasResolverFactory.createAnnotationResolver(project));
        this.registerResolver(AliasResolverFactory.createInnerAliasResolver(project));
    }

    @NotNull
    public Optional<PsiClass> findPsiClass(@Nullable PsiElement element, @NotNull String shortName) {
        PsiClass clazz = javaPsiFacade.findClass(shortName, GlobalSearchScope.allScope(project));
        if (null != clazz) {
            return Optional.of(clazz);
        }
        for (AliasResolver resolver : resolvers) {
            for (AliasDesc desc : resolver.getClassAliasDescriptions(element)) {
                if (shortName.equalsIgnoreCase(desc.getAlias())) {
                    return Optional.of(desc.getClazz());
                }
            }
        }
        return Optional.empty();
    }

    @NotNull
    public Collection<AliasDesc> getAliasDescs(@Nullable PsiElement element) {
        ArrayList<AliasDesc> result = Lists.newArrayList();
        for (AliasResolver resolver : resolvers) {
            result.addAll(resolver.getClassAliasDescriptions(element));
        }
        return result;
    }

    public Optional<AliasDesc> findAliasDesc(@Nullable PsiClass clazz) {
        if (clazz == null) {
            return Optional.empty();
        }
        for (AliasResolver resolver : resolvers) {
            for (AliasDesc desc : resolver.getClassAliasDescriptions(clazz)) {
                if (clazz.equals(desc.getClazz())) {
                    return Optional.of(desc);
                }
            }
        }
        return Optional.empty();
    }

    public void registerResolver(@NotNull AliasResolver resolver) {
        this.resolvers.add(resolver);
    }

}
