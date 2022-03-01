package com.wuzhizhan.mybatis.provider;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author yanglin
 */
public abstract class SimpleLineMarkerProvider<F extends PsiElement, T extends PsiElement> extends MarkerProviderAdaptor {

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements, @NotNull Collection<? super LineMarkerInfo<?>> result) {
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public LineMarkerInfo<F> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!isTheElement(element)) return null;

        Optional<T> processResult = apply((F) element);
        return processResult.map(t -> new LineMarkerInfo<F>(
                (F) element,
                element.getTextRange(),
                getIcon(),
                getTooltipProvider(t),
                getNavigationHandler(t),
                GutterIconRenderer.Alignment.CENTER,
                getAccessibleNameProvider()
        )).orElse(null);
    }

    private Function<F, String> getTooltipProvider(final T target) {
        return from -> getTooltip(from, target);
    }

    private GutterIconNavigationHandler<F> getNavigationHandler(final T target) {
        return (e, from) -> getNavigatable(from, target).navigate(true);
    }

    private Supplier<String> getAccessibleNameProvider() {
        return () -> "marker";
    }

    public abstract boolean isTheElement(@NotNull PsiElement element);

    @NotNull
    public abstract Optional<T> apply(@NotNull F from);

    @NotNull
    public abstract Navigatable getNavigatable(@NotNull F from, @NotNull T target);

    @NotNull
    public abstract String getTooltip(@NotNull F from, @NotNull T target);

    @NotNull
    public abstract Icon getIcon();
}
