package com.wuzhizhan.mybatis.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttributeValue;
import com.wuzhizhan.mybatis.dom.MapperBacktrackingUtils;
import com.wuzhizhan.mybatis.service.JavaService;
import com.wuzhizhan.mybatis.util.JavaUtils;
import com.wuzhizhan.mybatis.util.MybatisConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @author yanglin
 */
public class ContextPsiFieldReference extends PsiReferenceBase<XmlAttributeValue> {

    protected ContextReferenceSetResolver resolver;

    protected int index;

    public ContextPsiFieldReference(XmlAttributeValue element, TextRange range, int index) {
        super(element, range, false);
        this.index = index;
        resolver = ReferenceSetResolverFactory.createPsiFieldResolver(element);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public PsiElement resolve() {
        Optional<PsiElement> resolved = resolver.resolve(index);
        return resolved.orElse(null);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        Optional<PsiClass> clazz = getTargetClazz();
        return clazz.isPresent() ? JavaUtils.findSettablePsiFields(clazz.get()) : PsiReference.EMPTY_ARRAY;
    }

    @SuppressWarnings("unchecked")
    private Optional<PsiClass> getTargetClazz() {
        if (getElement().getValue().contains(MybatisConstants.DOT_SEPARATOR)) {
            int ind = 0 == index ? 0 : index - 1;
            Optional<PsiElement> resolved = resolver.resolve(ind);
            if (resolved.isPresent()) {
                return JavaService.getInstance(myElement.getProject()).getReferenceClazzOfPsiField(resolved.get());
            }
        } else {
            return MapperBacktrackingUtils.getPropertyClazz(myElement);
        }
        return Optional.empty();
    }

    public ContextReferenceSetResolver getResolver() {
        return resolver;
    }

    public void setResolver(ContextReferenceSetResolver resolver) {
        this.resolver = resolver;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
