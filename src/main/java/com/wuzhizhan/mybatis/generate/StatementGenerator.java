package com.wuzhizhan.mybatis.generate;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.CommonProcessors.CollectProcessor;
import com.wuzhizhan.mybatis.dom.model.GroupTwo;
import com.wuzhizhan.mybatis.dom.model.Mapper;
import com.wuzhizhan.mybatis.service.EditorService;
import com.wuzhizhan.mybatis.service.JavaService;
import com.wuzhizhan.mybatis.ui.ListSelectionListener;
import com.wuzhizhan.mybatis.ui.UiComponentFacade;
import com.wuzhizhan.mybatis.util.JavaUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * @author yanglin
 */
public abstract class StatementGenerator {

    private static final Function<Mapper, String> FUN = mapper -> {
        XmlTag xmlTag = mapper.getXmlTag();
        if (xmlTag == null) {
            return "";
        }
        VirtualFile vf = xmlTag.getContainingFile().getVirtualFile();
        if (null == vf) {
            return "";
        }
        return vf.getCanonicalPath();
    };

    public static Optional<PsiClass> getSelectResultType(@Nullable PsiMethod method) {
        if (null == method) {
            return Optional.empty();
        }
        PsiType returnType = method.getReturnType();
        if (returnType instanceof PsiPrimitiveType && !PsiType.VOID.equals(returnType)) {
            String boxedTypeName = ((PsiPrimitiveType) returnType).getBoxedTypeName();
            if (boxedTypeName == null) {
                return Optional.empty();
            }
            return JavaUtils.findClazz(method.getProject(), boxedTypeName);
        } else if (returnType instanceof PsiClassReferenceType) {
            PsiClassReferenceType type = (PsiClassReferenceType) returnType;
            if (type.hasParameters()) {
                PsiType[] parameters = type.getParameters();
                if (parameters.length == 1) {
                    type = (PsiClassReferenceType) parameters[0];
                }
            }
            return Optional.ofNullable(type.resolve());
        }
        return Optional.empty();
    }

    private Set<String> patterns;

    public StatementGenerator(@NotNull String... patterns) {
        this.patterns = Sets.newHashSet(patterns);
    }

    public void execute(@NotNull final PsiMethod method) {
        PsiClass psiClass = method.getContainingClass();
        if (null == psiClass) return;
        CollectProcessor<Mapper> processor = new CollectProcessor<>();
        JavaService.getInstance(method.getProject()).processClass(psiClass, processor);
        final List<Mapper> mappers = Lists.newArrayList(processor.getResults());
        if (1 == mappers.size()) {
            setupTag(method, (Mapper) Iterables.getOnlyElement(mappers, (Object) null));
        } else if (mappers.size() > 1) {
            UiComponentFacade.getInstance(method.getProject()).showListPopup("Choose target mapper xml to generate", new ListSelectionListener() {
                @Override
                public void selected(int index) {
                    setupTag(method, mappers.get(index));
                }

                @Override
                public boolean isWriteAction() {
                    return true;
                }
            }, mappers.stream().map(FUN).toArray(String[]::new));
        }
    }

    private void setupTag(PsiMethod method, Mapper mapper) {
        GroupTwo target = getTarget(mapper, method);
        target.getId().setStringValue(method.getName());
        target.setValue(" ");
        XmlTag tag = target.getXmlTag();
        if (tag == null) {
            return;
        }
        int offset = tag.getTextOffset() + tag.getTextLength() - tag.getName().length() + 1;
        EditorService editorService = EditorService.getInstance(method.getProject());
        editorService.format(tag.getContainingFile(), tag);
        editorService.scrollTo(tag, offset);
    }

    @Override
    public String toString() {
        return this.getDisplayText();
    }

    @NotNull
    protected abstract GroupTwo getTarget(@NotNull Mapper mapper, @NotNull PsiMethod method);

    @NotNull
    public abstract String getId();

    @NotNull
    public abstract String getDisplayText();

    public Set<String> getPatterns() {
        return patterns;
    }

    public void setPatterns(Set<String> patterns) {
        this.patterns = patterns;
    }

}
