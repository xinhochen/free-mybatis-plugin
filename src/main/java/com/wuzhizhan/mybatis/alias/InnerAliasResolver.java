package com.wuzhizhan.mybatis.alias;

import com.google.common.collect.ImmutableSet;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.wuzhizhan.mybatis.util.JavaUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

/**
 * @author yanglin
 */
public class InnerAliasResolver extends AliasResolver {

    private final Set<AliasDesc> innerAliasDescs;

    public InnerAliasResolver(Project project) {
        super(project);
        ImmutableSet.Builder<AliasDesc> builder = ImmutableSet.builder();
        createAliasDesc("java.lang.String", "string", builder);
        createAliasDesc("java.lang.Byte", "byte", builder);
        createAliasDesc("java.lang.Long", "long", builder);
        createAliasDesc("java.lang.Short", "short", builder);
        createAliasDesc("java.lang.Integer", "int", builder);
        createAliasDesc("java.lang.Integer", "int", builder);
        createAliasDesc("java.lang.Double", "double", builder);
        createAliasDesc("java.lang.Float", "float", builder);
        createAliasDesc("java.lang.Boolean", "boolean", builder);
        createAliasDesc("java.lang.Date", "date", builder);
        createAliasDesc("java.lang.BigDecimal", "decimal", builder);
        createAliasDesc("java.lang.Object", "object", builder);
        createAliasDesc("java.lang.Map", "map", builder);
        createAliasDesc("java.lang.HashMap", "hashmap", builder);
        createAliasDesc("java.lang.List", "list", builder);
        createAliasDesc("java.lang.ArrayList", "arraylist", builder);
        createAliasDesc("java.lang.Collection", "collection", builder);
        createAliasDesc("java.lang.Iterator", "iterator", builder);
        innerAliasDescs = builder.build();
    }

    private void createAliasDesc(String clazzName, String alias, ImmutableSet.Builder<AliasDesc> builder) {
        Optional<PsiClass> psiClass = JavaUtils.findClazz(project, clazzName);
        psiClass.ifPresent(clazz -> builder.add(AliasDesc.create(clazz, alias)));
    }

    @NotNull
    @Override
    public Set<AliasDesc> getClassAliasDescriptions(@Nullable PsiElement element) {
        return innerAliasDescs;
    }

}
