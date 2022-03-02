package com.wuzhizhan.mybatis.intention;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.wuzhizhan.mybatis.service.EditorService;
import com.wuzhizhan.mybatis.template.MybatisFileTemplateDescriptorFactory;
import com.wuzhizhan.mybatis.ui.ClickableListener;
import com.wuzhizhan.mybatis.ui.ListSelectionListener;
import com.wuzhizhan.mybatis.ui.UiComponentFacade;
import com.wuzhizhan.mybatis.util.CollectionUtils;
import com.wuzhizhan.mybatis.util.MapperUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * @author yanglin
 */
public class GenerateMapperIntention extends GenericIntention {

    public GenerateMapperIntention() {
        super(GenerateMapperChooser.INSTANCE);
    }

    @NotNull
    @Override
    public String getText() {
        return "[Mybatis] MybatisGenerator mapper of xml";
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public void invoke(@NotNull final Project project, final Editor editor, PsiFile file) throws IncorrectOperationException {
        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
        PsiClass clazz = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        Collection<PsiDirectory> directories = MapperUtils.findMapperDirectories(project);
        if (CollectionUtils.isEmpty(directories)) {
            handleChooseNewFolder(project, editor, clazz);
        } else {
            handleMutilDirectories(project, editor, clazz, directories);
        }
    }

    private void handleMutilDirectories(Project project,
                                        final Editor editor,
                                        final PsiClass clazz,
                                        Collection<PsiDirectory> directories) {
        final Map<String, PsiDirectory> pathMap = getPathMap(directories);
        final ArrayList<String> keys = Lists.newArrayList(pathMap.keySet());
        ListSelectionListener popupListener = new ListSelectionListener() {
            @Override
            public void selected(int index) {
                processGenerate(editor, clazz, pathMap.get(keys.get(index)));
            }

            @Override
            public boolean isWriteAction() {
                return true;
            }
        };
        UiComponentFacade uiComponentFacade = UiComponentFacade.getInstance(project);
        uiComponentFacade.showListPopupWithSingleClickable("Choose folder",
                popupListener,
                "Choose another",
                getChooseFolderListener(editor, clazz),
                getPathTextForShown(project, keys, pathMap));
    }

    private ClickableListener getChooseFolderListener(final Editor editor, final PsiClass clazz) {
        final Project project = clazz.getProject();
        return new ClickableListener() {
            @Override
            public void clicked() {
                handleChooseNewFolder(project, editor, clazz);
            }

            @Override
            public boolean isWriteAction() {
                return false;
            }
        };
    }

    private void handleChooseNewFolder(Project project, Editor editor, PsiClass clazz) {
        UiComponentFacade uiComponentFacade = UiComponentFacade.getInstance(project);
        VirtualFile baseDir = ProjectUtil.guessProjectDir(project);
        VirtualFile vf = uiComponentFacade.showSingleFolderSelectionDialog("Select target folder", baseDir, baseDir);
        if (null != vf) {
            processGenerate(editor, clazz, PsiManager.getInstance(project).findDirectory(vf));
        }
    }

    private String[] getPathTextForShown(Project project, List<String> paths, final Map<String, PsiDirectory> pathMap) {
        Collections.sort(paths);
        final String projectBasePath = project.getBasePath();
        Collection<String> result = Lists.newArrayList(Collections2.transform(paths, new Function<String, String>() {
            @Override
            public String apply(String input) {
                String relativePath = FileUtil.getRelativePath(projectBasePath, input, File.separatorChar);
                Module module = ModuleUtil.findModuleForPsiElement(pathMap.get(input));
                return null == module ? relativePath : ("[" + module.getName() + "] " + relativePath);
            }
        }));
        return result.toArray(new String[0]);
    }

    private Map<String, PsiDirectory> getPathMap(Collection<PsiDirectory> directories) {
        Map<String, PsiDirectory> result = Maps.newHashMap();
        for (PsiDirectory directory : directories) {
            String presentableUrl = directory.getVirtualFile().getPresentableUrl();
            result.put(presentableUrl, directory);
        }
        return result;
    }

    private void processGenerate(Editor editor, PsiClass clazz, PsiDirectory directory) {
        if (null == directory) {
            return;
        }
        if (!directory.isWritable()) {
            HintManager.getInstance().showErrorHint(editor, "Target directory is not writable");
            return;
        }
        try {
            Properties properties = new Properties();
            properties.setProperty("NAMESPACE", clazz.getQualifiedName());
            PsiElement psiFile = MapperUtils.createMapperFromFileTemplate(MybatisFileTemplateDescriptorFactory.MYBATIS_MAPPER_XML_TEMPLATE,
                    clazz.getName(), directory, properties);
            EditorService.getInstance(clazz.getProject()).scrollTo(psiFile, 0);
        } catch (Exception e) {
            HintManager.getInstance().showErrorHint(editor, "Failed: " + e.getCause());
        }
    }

}