package com.wuzhizhan.mybatis.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.CommonProcessors;
import com.intellij.util.xml.DomElement;
import com.wuzhizhan.mybatis.service.JavaService;
import com.wuzhizhan.mybatis.util.Icons;
import com.wuzhizhan.mybatis.util.JavaUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UastUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yanglin
 */
public class MapperLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        UElement uElement = UastUtils.getUParentForIdentifier(element);
        if (uElement == null) {
            return;
        }

        PsiElement identifier = uElement.getJavaPsi();
        if (identifier == null) {
            return;
        }
        if (identifier instanceof PsiNameIdentifierOwner && JavaUtils.isElementWithinInterface(identifier)) {
            CommonProcessors.CollectProcessor<DomElement> processor = new CommonProcessors.CollectProcessor<>();
            JavaService.getInstance(identifier.getProject()).process(identifier, processor);
            Collection<DomElement> results = processor.getResults();
            if (results.isEmpty()) {
                return;
            }
            List<XmlTag> targets = results.stream().map(DomElement::getXmlTag).collect(Collectors.toList());
            NavigationGutterIconBuilder<PsiElement> builder =
                    NavigationGutterIconBuilder.create(Icons.MAPPER_LINE_MARKER_ICON)
                            .setAlignment(GutterIconRenderer.Alignment.CENTER)
                            .setTargets(targets)
                            .setTooltipTitle("Navigation to Target in Mapper Xml");
            result.add(builder.createLineMarkerInfo(((PsiNameIdentifierOwner) identifier).getNameIdentifier()));
        }
    }
}
