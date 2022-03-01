package com.wuzhizhan.mybatis.locator;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.spring.contexts.model.LocalAnnotationModel;
import com.intellij.spring.contexts.model.LocalModel;
import com.intellij.spring.contexts.model.LocalXmlModel;
import com.intellij.spring.model.CommonSpringBean;
import com.intellij.spring.model.extensions.myBatis.SpringMyBatisBeansProvider;
import com.intellij.spring.model.jam.stereotype.CustomSpringComponent;
import com.intellij.spring.model.utils.SpringCommonUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MapperBeanProvider extends SpringMyBatisBeansProvider {
    private static final String MAPPER_SCAN = "org.mybatis.spring.annotation.MapperScan";
    private static final String MAPPER_SCAN_VALUE = "value";
    private static final String MAPPER_SCAN_BASE_PACKAGES = "basePackages";
    private static final String MAPPER_SCAN_BASE_PACKAGE_CLASSES = "basePackageClasses";

    @Override
    public @NotNull Collection<CommonSpringBean> getCustomComponents(@NotNull LocalModel springModel) {
        Module module = springModel.getModule();
        if (module != null && !DumbService.isDumb(module.getProject())) {
            if (springModel instanceof LocalXmlModel) {
                Collection<CommonSpringBean> myBatisMappers = new HashSet<>();
                this.collectMappers((LocalXmlModel)springModel, module, myBatisMappers, "org.mybatis.spring.mapper.MapperFactoryBean");
                this.collectMappers((LocalXmlModel)springModel, module, myBatisMappers, "org.mybatis.spring.mapper.MapperScannerConfigurer");
                return myBatisMappers;
            } else if (springModel instanceof LocalAnnotationModel) {
                Optional<PsiClass> config = searchMapperScanContext((LocalAnnotationModel)springModel, module);
                if (!config.isPresent()) {
                    return Collections.emptyList();
                }

                Collection<CommonSpringBean> myBatisMappers = new HashSet<>();
                Collection<PsiPackage> psiPackages = this.getMapperScanPackages(config.get());
                GlobalSearchScope scope = GlobalSearchScope.projectScope(module.getProject());
                for (PsiPackage psiPackage : psiPackages) {
                    processBasePackage(myBatisMappers, scope, psiPackage);
                }
                return myBatisMappers;
            }
        }
        return Collections.emptyList();
    }

    private Optional<PsiClass> searchMapperScanContext(LocalAnnotationModel springModel, Module module) {
        if (SpringCommonUtils.findLibraryClass(module, MAPPER_SCAN) == null) {
            return Optional.empty();
        }
        PsiClass config = springModel.getConfig();
        PsiAnnotation mapperScan = config.getAnnotation(MAPPER_SCAN);
        return mapperScan == null ? Optional.empty() : Optional.of(config);
    }

    private Collection<PsiPackage> getMapperScanPackages(PsiClass context) {
        PsiAnnotation mapperScanAnnotation = context.getAnnotation(MAPPER_SCAN);
        if (mapperScanAnnotation == null) {
            return Collections.emptyList();
        }
        Collection<PsiPackage> psiPackages = new ArrayList<>();
       Arrays.stream(mapperScanAnnotation.getParameterList().getAttributes())
                .filter((psiNameValuePairx) -> psiNameValuePairx.getAttributeValue() != null)
                .forEach(pair -> {
                    String attrName = pair.getAttributeName();
                    PsiAnnotationMemberValue value = pair.getValue();
                    if (value instanceof PsiArrayInitializerMemberValue) {
                        psiPackages.addAll(handlePsiArrayInitializerMemberValue(attrName, context, (PsiArrayInitializerMemberValue) value));
                    } else if (value instanceof PsiLiteralExpression) {
                        psiPackages.addAll(handlePsiLiteralExpression(context, (PsiLiteralExpression) value));
                    } else if (value instanceof PsiClassObjectAccessExpression) {
                        psiPackages.addAll(handlePsiClassObjectAccessExpression(context, (PsiClassObjectAccessExpression) value));
                    }
                });

        return psiPackages;
    }

    private Collection<PsiPackage> handlePsiArrayInitializerMemberValue(String attrName, PsiClass context, PsiArrayInitializerMemberValue psiArrayInitializerMemberValue) {
        Collection<PsiPackage> psiPackages = new ArrayList<>();
        if (!MAPPER_SCAN_VALUE.equals(attrName) && !MAPPER_SCAN_BASE_PACKAGES.equals(attrName)) {
            if (MAPPER_SCAN_BASE_PACKAGE_CLASSES.equals(attrName)) {
                psiPackages.addAll(this.handleMapperScanClass(context, psiArrayInitializerMemberValue));
            }
        } else {
            psiPackages.addAll(this.handleMapperScanValue(context, psiArrayInitializerMemberValue));
        }

        return psiPackages;
    }

    private Collection<PsiPackage> handlePsiLiteralExpression(PsiClass context, PsiLiteralExpression psiLiteralExpression) {
        Collection<PsiPackage> psiPackages = new ArrayList<>();
        Optional<PsiPackage> psiPackage = getPackageByName(JavaPsiFacade.getInstance(context.getProject()), psiLiteralExpression.getText().replaceAll("\"", ""));
        Objects.requireNonNull(psiPackages);
        psiPackage.ifPresent(psiPackages::add);
        return psiPackages;
    }

    private Collection<PsiPackage> handlePsiClassObjectAccessExpression(PsiClass context, PsiClassObjectAccessExpression psiClassObjectAccessExpression) {
        Collection<PsiPackage> psiPackages = new ArrayList<>();
        PsiClass psiClass = PsiTypesUtil.getPsiClass(psiClassObjectAccessExpression.getOperand().getType());
        if (psiClass != null) {
            String packageName = ((PsiJavaFile)psiClass.getContainingFile()).getPackageName();
            Optional<PsiPackage> psiPackage = this.getPackageByName(JavaPsiFacade.getInstance(context.getProject()), packageName);
            Objects.requireNonNull(psiPackages);
            psiPackage.ifPresent(psiPackages::add);
        }

        return psiPackages;
    }

    private Collection<PsiPackage> handleMapperScanValue(PsiClass context, PsiArrayInitializerMemberValue psiArrayInitializerMemberValue) {
        Collection<PsiPackage> psiPackages = new ArrayList<>();
        PsiAnnotationMemberValue[] psiAnnotationMemberValues = psiArrayInitializerMemberValue.getInitializers();

        for (PsiAnnotationMemberValue value : psiAnnotationMemberValues) {
            Optional<PsiPackage> psiPackage = this.getPackageByName(JavaPsiFacade.getInstance(context.getProject()), value.getText().replaceAll("\"", ""));
            Objects.requireNonNull(psiPackages);
            psiPackage.ifPresent(psiPackages::add);
        }

        return psiPackages;
    }

    private Collection<PsiPackage> handleMapperScanClass(PsiClass context, PsiArrayInitializerMemberValue psiArrayInitializerMemberValue) {
        Collection<PsiPackage> psiPackages = new ArrayList<>();
        PsiAnnotationMemberValue[] psiAnnotationMemberValues = psiArrayInitializerMemberValue.getInitializers();

        for (PsiAnnotationMemberValue value : psiAnnotationMemberValues) {
            if (value instanceof PsiClassObjectAccessExpression) {
                PsiTypeElement psiTypeElement = ((PsiClassObjectAccessExpression) value).getOperand();
                PsiClass psiClass = PsiTypesUtil.getPsiClass(psiTypeElement.getType());
                if (psiClass != null) {
                    String packageName = ((PsiJavaFile) psiClass.getContainingFile()).getPackageName();
                    Optional<PsiPackage> psiPackage = this.getPackageByName(JavaPsiFacade.getInstance(context.getProject()), packageName);
                    Objects.requireNonNull(psiPackages);
                    psiPackage.ifPresent(psiPackages::add);
                }
            }
        }

        return psiPackages;
    }

    private Optional<PsiPackage> getPackageByName(JavaPsiFacade javaPsiFacade, String packageName) {
        return packageName == null ? Optional.empty() : Optional.ofNullable(javaPsiFacade.findPackage(packageName));
    }

    private void processBasePackage(Collection<CommonSpringBean> myBatisMappers, GlobalSearchScope scope, PsiPackage psiPackage) {
        PsiClass[] psiClasses = psiPackage.getClasses(scope);
        for (PsiClass mapperClass : psiClasses) {
            if (!mapperClass.isInterface()) {
                continue;
            }
            myBatisMappers.add(new CustomSpringComponent(mapperClass));
        }
        PsiPackage[] subPackages = psiPackage.getSubPackages();
        for (PsiPackage subPackage : subPackages) {
            processBasePackage(myBatisMappers, scope, subPackage);
        }
    }
}
