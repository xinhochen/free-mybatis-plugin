package com.wuzhizhan.mybatis.ui;


import com.google.common.base.Joiner;
import com.intellij.database.psi.DbTable;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPackage;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.wuzhizhan.mybatis.generate.MybatisGenerator;
import com.wuzhizhan.mybatis.model.Config;
import com.wuzhizhan.mybatis.model.TableInfo;
import com.wuzhizhan.mybatis.setting.PersistentConfig;
import com.wuzhizhan.mybatis.util.JTextFieldHintListener;
import com.wuzhizhan.mybatis.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件主界面
 * Created by kangtian on 2018/8/1.
 */
public class MybatisGeneratorMainUI extends JFrame {
    private final AnActionEvent anActionEvent;
    private final Project project;
    private final PsiElement[] psiElements;
    private Map<String, Config> historyConfigList;
    private Config config;

    private final JTextField tableNameField = new JTextField(10);
    private final TextFieldWithPackageButton modelPackageField = new TextFieldWithPackageButton();
    private final TextFieldWithPackageButton daoPackageField = new TextFieldWithPackageButton();
    private final JTextField daoPostfixField = new JTextField(10);
    private final JBTextField xmlPackageField = new JBTextField(12);
    private final JTextField daoNameField = new JTextField(10);
    private final JTextField modelNameField = new JTextField(10);
    private final JTextField keyField = new JTextField(10);

    private final TextFieldWithBrowseButton projectFolderBtn = new TextFieldWithBrowseButton();
    private final JTextField modelMvnField = new JBTextField(15);
    private final JTextField daoMvnField = new JBTextField(15);
    private final JTextField xmlMvnField = new JBTextField(15);

    private final JCheckBox offsetLimitBox = new JCheckBox("Page(分页)");
    private final JCheckBox commentBox = new JCheckBox("comment(实体注释)");
    private final JCheckBox overrideXMLBox = new JCheckBox("Overwrite-Xml");
    private final JCheckBox overrideJavaBox = new JCheckBox("Overwrite-Java");
    private final JCheckBox needToStringHashcodeEqualsBox = new JCheckBox("toString/hashCode/equals");
    private final JCheckBox useSchemaPrefixBox = new JCheckBox("Use-Schema(使用Schema前缀)");
    private final JCheckBox needForUpdateBox = new JCheckBox("Add-ForUpdate(select增加ForUpdate)");
    private final JCheckBox annotationDAOBox = new JCheckBox("Repository-Annotation(Repository注解)");
    private final JCheckBox useDAOExtendStyleBox = new JCheckBox("Parent-Interface(公共父接口)");
    private final JCheckBox jsr310SupportBox = new JCheckBox("JSR310: Date and Time API");
    private final JCheckBox annotationBox = new JCheckBox("JPA-Annotation(JPA注解)");
    private final JCheckBox useActualColumnNamesBox = new JCheckBox("Actual-Column(实际的列名)");
    private final JCheckBox useTableNameAliasBox = new JCheckBox("Use-Alias(启用别名查询)");
    private final JCheckBox useExampleBox = new JCheckBox("Use-Example");
    private final JCheckBox useLombokBox = new JCheckBox("Use-Lombox");
    private final JCheckBox useSwaggerBox = new JCheckBox("Use-Swagger");


    public MybatisGeneratorMainUI(AnActionEvent anActionEvent) throws HeadlessException {
        this.anActionEvent = anActionEvent;
        this.project = anActionEvent.getProject();
        PersistentConfig persistentConfig = PersistentConfig.getInstance(project);
        this.psiElements = anActionEvent.getData(LangDataKeys.PSI_ELEMENT_ARRAY);

        Map<String, Config> initConfigMap = persistentConfig.getInitConfig();
        historyConfigList = persistentConfig.getHistoryConfigList();


        setTitle("Mybatis Generate Tool");
        setPreferredSize(new Dimension(1100, 800));//设置大小
        setMinimumSize(new Dimension(1100, 800));
        setLocation(120, 100);
        pack();
        setVisible(true);
        JButton buttonOK = new JButton("ok");
        getRootPane().setDefaultButton(buttonOK);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        PsiElement psiElement = psiElements[0];
        TableInfo tableInfo = new TableInfo((DbTable) psiElement);
        String tableName = tableInfo.getTableName();
        String modelName = StringUtils.dbStringToCamelStyle(tableName);
        String primaryKey = "";
        if (tableInfo.getPrimaryKeys().size() > 0) {
            primaryKey = tableInfo.getPrimaryKeys().get(0);
        }
        String projectFolder = project.getBasePath();

        boolean multiTable;
        if (psiElements.length > 1) {//多表时，只使用默认配置
            multiTable = true;
            if (initConfigMap != null) {
                config = initConfigMap.get("initConfig");
            }
        } else {
            multiTable = false;
            if (initConfigMap != null) {//单表时，优先使用已经存在的配置
                config = initConfigMap.get("initConfig");
            }
            if (historyConfigList == null) {
                historyConfigList = new HashMap<>();
            } else {
                if (historyConfigList.containsKey(tableName)) {
                    config = historyConfigList.get(tableName);
                }
            }
        }


        /*
          table setting
         */
        JPanel tablePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tablePanel.setBorder(IdeBorderFactory.createTitledBorder("Table Setting"));
        tablePanel.add(new JLabel("Table:"));
        if (psiElements.length > 1) {
            tableNameField.addFocusListener(new JTextFieldHintListener(tableNameField, "eg:db_table"));
        } else {
            tableNameField.setText(tableName);
        }
        tablePanel.add(tableNameField);

        tablePanel.add(new JLabel("主键(选填):"));
        if (psiElements.length > 1) {
            keyField.addFocusListener(new JTextFieldHintListener(keyField, "eg:primary key"));
        } else {
            keyField.setText(primaryKey);
        }
        tablePanel.add(keyField);



        /*
          project panel
         */
        JPanel projectFolderPanel = new JPanel();
        projectFolderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel projectLabel = new JLabel("Project Folder:");
        projectFolderPanel.add(projectLabel);
        projectFolderBtn.setTextFieldPreferredWidth(45);
        if (config != null && !StringUtils.isEmpty(config.getProjectFolder())) {
            projectFolderBtn.setText(config.getProjectFolder());
        } else {
            projectFolderBtn.setText(projectFolder);
        }
        projectFolderBtn.addBrowseFolderListener(new TextBrowseFolderListener(
                FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);
                projectFolderBtn.setText(projectFolderBtn.getText().replaceAll("\\\\", "/"));
            }
        });
        projectFolderPanel.add(projectFolderBtn);


        /*
          model setting
         */
        JPanel modelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modelPanel.setBorder(IdeBorderFactory.createTitledBorder("Model Setting"));
        if (!multiTable) {
            modelPanel.add(new JLabel("File:"));
            modelNameField.setText(modelName);
            modelPanel.add(modelNameField);
        }
        modelPanel.add(new JBLabel("Package:"));
        if (config != null && !StringUtils.isEmpty(config.getModelPackage())) {
            modelPackageField.setText(config.getModelPackage());
        } else {
            modelPackageField.setText("generate");
        }
        modelPanel.add(modelPackageField);
        modelPackageField.addActionListener(actionEvent -> {
            final PackageChooserDialog chooser = new PackageChooserDialog("Chooser Model Package", project);
            chooser.selectPackage(modelPackageField.getText());
            chooser.show();
            final PsiPackage psiPackage = chooser.getSelectedPackage();
            String packageName = psiPackage == null ? null : psiPackage.getQualifiedName();
            modelPackageField.setText(packageName);
            MybatisGeneratorMainUI.this.toFront();
        });
        modelPanel.add(new JLabel("Path:"));
        modelMvnField.setText("src/main/java");
        modelPanel.add(modelMvnField);


        /*
          dao setting
         */
        JPanel daoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        daoPanel.setBorder(IdeBorderFactory.createTitledBorder("DAO Setting"));


        if (multiTable) { //多表
            if (config != null && !StringUtils.isEmpty(config.getDaoPostfix())) {
                daoPostfixField.setText(config.getDaoPostfix());
            } else {
                daoPostfixField.setText("DAO");
            }
            daoPanel.add(new JLabel("DAO postfix:"));
            daoPanel.add(daoPostfixField);
        } else {//单表
            if (config != null && !StringUtils.isEmpty(config.getDaoPostfix())) {
                daoNameField.setText(modelName + config.getDaoPostfix());
            } else {
                daoNameField.setText(modelName + "DAO");
            }
            daoPanel.add(new JLabel("Name:"));
            daoPanel.add(daoNameField);
        }


        daoPanel.add(new JLabel("Package:"));
        if (config != null && !StringUtils.isEmpty(config.getDaoPackage())) {
            daoPackageField.setText(config.getDaoPackage());
        } else {
            daoPackageField.setText("generate");
        }
        daoPanel.add(daoPackageField);
        daoPackageField.addActionListener(actionEvent -> {
            final PackageChooserDialog chooser = new PackageChooserDialog("Choose Dao Package", project);
            chooser.selectPackage(daoPackageField.getText());
            chooser.show();
            final PsiPackage psiPackage = chooser.getSelectedPackage();
            String packageName = psiPackage == null ? null : psiPackage.getQualifiedName();
            daoPackageField.setText(packageName);
            MybatisGeneratorMainUI.this.toFront();
        });
        daoPanel.add(new JLabel("Path:"));
        daoMvnField.setText("src/main/java");
        daoPanel.add(daoMvnField);


        /*
          xml mapper setting
         */
        JPanel xmlMapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        xmlMapperPanel.setBorder(IdeBorderFactory.createTitledBorder("XML Mapper Setting"));
        JLabel labelLeft6 = new JLabel("Package:");
        xmlMapperPanel.add(labelLeft6);
        if (config != null && !StringUtils.isEmpty(config.getXmlPackage())) {
            xmlPackageField.setText(config.getXmlPackage());
        } else {
            xmlPackageField.setText("generator");
        }
        xmlMapperPanel.add(xmlPackageField);
        xmlMapperPanel.add(new JLabel("Path:"));
        xmlMvnField.setText("src/main/resources");
        xmlMapperPanel.add(xmlMvnField);

        /*
          options
         */
        JPanel optionsPanel = new JBPanel<>(new GridLayout(8, 2, 5, 5));
        optionsPanel.setBorder(IdeBorderFactory.createTitledBorder("Options"));
        if (config == null) {
            commentBox.setSelected(true);
            overrideXMLBox.setSelected(true);
            overrideJavaBox.setSelected(true);
            useSchemaPrefixBox.setSelected(true);
            useLombokBox.setSelected(true);

        } else {
            if (config.isOffsetLimit()) {
                offsetLimitBox.setSelected(true);
            }
            if (config.isComment()) {
                commentBox.setSelected(true);
            }

            if (config.isOverrideXML()) {
                overrideXMLBox.setSelected(true);
            }
            if (config.isOverrideJava()) {
                overrideJavaBox.setSelected(true);
            }
            if (config.isNeedToStringHashcodeEquals()) {
                needToStringHashcodeEqualsBox.setSelected(true);
            }
            if (config.isUseSchemaPrefix()) {
                useSchemaPrefixBox.setSelected(true);
            }
            if (config.isNeedForUpdate()) {
                needForUpdateBox.setSelected(true);
            }
            if (config.isAnnotationDAO()) {
                annotationDAOBox.setSelected(true);
            }
            if (config.isUseDAOExtendStyle()) {
                useDAOExtendStyleBox.setSelected(true);
            }
            if (config.isJsr310Support()) {
                jsr310SupportBox.setSelected(true);
            }
            if (config.isAnnotation()) {
                annotationBox.setSelected(true);
            }
            if (config.isUseActualColumnNames()) {
                useActualColumnNamesBox.setSelected(true);
            }
            if (config.isUseTableNameAlias()) {
                useTableNameAliasBox.setSelected(true);
            }
            if (config.isUseExample()) {
                useExampleBox.setSelected(true);
            }
            if (config.isUseLombokPlugin()) {
                useLombokBox.setSelected(true);
            }
            if (config.isUseSwaggerPlugin()) {
                useSwaggerBox.setSelected(true);
            }
        }
        optionsPanel.add(offsetLimitBox);
        optionsPanel.add(commentBox);
        optionsPanel.add(overrideXMLBox);
        optionsPanel.add(overrideJavaBox);
        optionsPanel.add(needToStringHashcodeEqualsBox);
        optionsPanel.add(useSchemaPrefixBox);
        optionsPanel.add(needForUpdateBox);
        optionsPanel.add(annotationDAOBox);
        optionsPanel.add(useDAOExtendStyleBox);
        optionsPanel.add(jsr310SupportBox);
        optionsPanel.add(annotationBox);
        optionsPanel.add(useActualColumnNamesBox);
        optionsPanel.add(useTableNameAliasBox);
        optionsPanel.add(useExampleBox);
        optionsPanel.add(useLombokBox);
        optionsPanel.add(useSwaggerBox);
        optionsPanel.setAutoscrolls(true);

        JPanel mainPanel = new JPanel(new VerticalFlowLayout());
        // mainPanel.setBorder(JBUI.Borders.empty(10, 30, 5, 40));
        mainPanel.add(projectFolderPanel);
        if (!multiTable) {
            mainPanel.add(tablePanel);
        }
        mainPanel.add(modelPanel);
        mainPanel.add(daoPanel);
        mainPanel.add(xmlMapperPanel);
        mainPanel.add(optionsPanel);


        JPanel paneBottom = new JPanel();//确认和取消按钮
        paneBottom.setLayout(new FlowLayout(FlowLayout.RIGHT));
        paneBottom.add(buttonOK);
        JButton buttonCancel = new JButton("cancel");
        paneBottom.add(buttonCancel);


        /*
          historyConfig panel
         */
        this.getContentPane().add(Box.createVerticalStrut(10)); //采用x布局时，添加固定宽度组件隔开
        final DefaultListModel<String> defaultListModel = new DefaultListModel<>();

        if (historyConfigList == null) {
            historyConfigList = new HashMap<>();
        }
        for (String historyConfigName : historyConfigList.keySet()) {
            defaultListModel.addElement(historyConfigName);
        }
        Map<String, Config> finalHistoryConfigList = historyConfigList;

        final JBList<String> configJBList = new JBList<>(defaultListModel);
        configJBList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configJBList.setSelectedIndex(0);
        configJBList.setVisibleRowCount(25);
        JBScrollPane ScrollPane = new JBScrollPane(configJBList);


        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
        btnPanel.add(new JLabel("      "));//用来占位置
        //    private JButton selectConfigBtn = new JButton("SELECT");
        JButton deleteConfigBtn = new JButton("DELETE");
        btnPanel.add(deleteConfigBtn);
        configJBList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (configJBList.getSelectedIndex() != -1) {
                    if (e.getClickCount() == 2) { //双击事件
                        String configName = configJBList.getSelectedValue();
                        Config selectedConfig = finalHistoryConfigList.get(configName);
                        modelPackageField.setText(selectedConfig.getModelPackage());
                        daoPackageField.setText(selectedConfig.getDaoPackage());
                        xmlPackageField.setText(selectedConfig.getXmlPackage());
                        projectFolderBtn.setText(selectedConfig.getProjectFolder());
                        offsetLimitBox.setSelected(selectedConfig.isOffsetLimit());
                        commentBox.setSelected(selectedConfig.isComment());
                        overrideXMLBox.setSelected(selectedConfig.isOverrideXML());
                        overrideJavaBox.setSelected(selectedConfig.isOverrideJava());
                        needToStringHashcodeEqualsBox.setSelected(selectedConfig.isNeedToStringHashcodeEquals());
                        useSchemaPrefixBox.setSelected(selectedConfig.isUseSchemaPrefix());
                        needForUpdateBox.setSelected(selectedConfig.isNeedForUpdate());
                        annotationDAOBox.setSelected(selectedConfig.isAnnotationDAO());
                        useDAOExtendStyleBox.setSelected(selectedConfig.isUseDAOExtendStyle());
                        jsr310SupportBox.setSelected(selectedConfig.isJsr310Support());
                        annotationBox.setSelected(selectedConfig.isAnnotation());
                        useActualColumnNamesBox.setSelected(selectedConfig.isUseActualColumnNames());
                        useTableNameAliasBox.setSelected(selectedConfig.isUseTableNameAlias());
                        useExampleBox.setSelected(selectedConfig.isUseExample());
                        useLombokBox.setSelected(selectedConfig.isUseLombokPlugin());
                        useSwaggerBox.setSelected(selectedConfig.isUseSwaggerPlugin());
                    }
                }
            }
        });

        deleteConfigBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finalHistoryConfigList.remove(configJBList.getSelectedValue());
                defaultListModel.removeAllElements();
                for (String historyConfigName : finalHistoryConfigList.keySet()) {
                    defaultListModel.addElement(historyConfigName);
                }
            }
        });

        JPanel historyConfigPanel = new JPanel();
        historyConfigPanel.setLayout(new BoxLayout(historyConfigPanel, BoxLayout.Y_AXIS));
        historyConfigPanel.setBorder(BorderFactory.createTitledBorder("config history"));
        historyConfigPanel.add(ScrollPane);
        historyConfigPanel.add(btnPanel);


        JPanel contentPane = new JBPanel<>();
        contentPane.setBorder(JBUI.Borders.empty(5));
        contentPane.setLayout(new BorderLayout());
        contentPane.add(mainPanel, BorderLayout.CENTER);
        contentPane.add(paneBottom, BorderLayout.SOUTH);
        contentPane.add(historyConfigPanel, BorderLayout.WEST);
        contentPane.setAutoscrolls(true);
        setContentPane(contentPane);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        try {
            dispose();
            List<String> result = new ArrayList<>();
            if (psiElements.length == 1) {
                Config generator_config = new Config();
                generator_config.setName(tableNameField.getText());
                generator_config.setTableName(tableNameField.getText());
                generator_config.setProjectFolder(projectFolderBtn.getText());

                generator_config.setModelPackage(modelPackageField.getText());
                generator_config.setDaoPackage(daoPackageField.getText());
                generator_config.setXmlPackage(xmlPackageField.getText());
                generator_config.setDaoName(daoNameField.getText());
                generator_config.setModelName(modelNameField.getText());
                generator_config.setPrimaryKey(keyField.getText());

                generator_config.setOffsetLimit(offsetLimitBox.getSelectedObjects() != null);
                generator_config.setComment(commentBox.getSelectedObjects() != null);
                generator_config.setOverrideXML(overrideXMLBox.getSelectedObjects() != null);
                generator_config.setOverrideJava(overrideJavaBox.getSelectedObjects() != null);
                generator_config.setNeedToStringHashcodeEquals(needToStringHashcodeEqualsBox.getSelectedObjects() != null);
                generator_config.setUseSchemaPrefix(useSchemaPrefixBox.getSelectedObjects() != null);
                generator_config.setNeedForUpdate(needForUpdateBox.getSelectedObjects() != null);
                generator_config.setAnnotationDAO(annotationDAOBox.getSelectedObjects() != null);
                generator_config.setUseDAOExtendStyle(useDAOExtendStyleBox.getSelectedObjects() != null);
                generator_config.setJsr310Support(jsr310SupportBox.getSelectedObjects() != null);
                generator_config.setAnnotation(annotationBox.getSelectedObjects() != null);
                generator_config.setUseActualColumnNames(useActualColumnNamesBox.getSelectedObjects() != null);
                generator_config.setUseTableNameAlias(useTableNameAliasBox.getSelectedObjects() != null);
                generator_config.setUseExample(useExampleBox.getSelectedObjects() != null);
                generator_config.setUseLombokPlugin(useLombokBox.getSelectedObjects() != null);
                generator_config.setUseSwaggerPlugin(useSwaggerBox.getSelectedObjects() != null);

                generator_config.setModelMvnPath(modelMvnField.getText());
                generator_config.setDaoMvnPath(daoMvnField.getText());
                generator_config.setXmlMvnPath(xmlMvnField.getText());


                result = new MybatisGenerator(generator_config).execute(anActionEvent, true);
            } else {

                for (PsiElement psiElement : psiElements) {
                    TableInfo tableInfo = new TableInfo((DbTable) psiElement);
                    String tableName = tableInfo.getTableName();
                    String modelName = StringUtils.dbStringToCamelStyle(tableName);
                    String primaryKey = "";
                    if (tableInfo.getPrimaryKeys() != null && tableInfo.getPrimaryKeys().size() != 0) {
                        primaryKey = tableInfo.getPrimaryKeys().get(0);
                    }
                    Config generator_config = new Config();
                    generator_config.setName(tableName);
                    generator_config.setTableName(tableName);
                    generator_config.setProjectFolder(projectFolderBtn.getText());

                    generator_config.setModelPackage(modelPackageField.getText());
                    generator_config.setDaoPackage(daoPackageField.getText());
                    generator_config.setXmlPackage(xmlPackageField.getText());
                    generator_config.setDaoName(modelName + daoPostfixField.getText());
                    generator_config.setModelName(modelName);
                    generator_config.setPrimaryKey(primaryKey);

                    generator_config.setOffsetLimit(offsetLimitBox.getSelectedObjects() != null);
                    generator_config.setComment(commentBox.getSelectedObjects() != null);
                    generator_config.setOverrideXML(overrideXMLBox.getSelectedObjects() != null);
                    generator_config.setOverrideJava(overrideJavaBox.getSelectedObjects() != null);
                    generator_config.setNeedToStringHashcodeEquals(needToStringHashcodeEqualsBox.getSelectedObjects() != null);
                    generator_config.setUseSchemaPrefix(useSchemaPrefixBox.getSelectedObjects() != null);
                    generator_config.setNeedForUpdate(needForUpdateBox.getSelectedObjects() != null);
                    generator_config.setAnnotationDAO(annotationDAOBox.getSelectedObjects() != null);
                    generator_config.setUseDAOExtendStyle(useDAOExtendStyleBox.getSelectedObjects() != null);
                    generator_config.setJsr310Support(jsr310SupportBox.getSelectedObjects() != null);
                    generator_config.setAnnotation(annotationBox.getSelectedObjects() != null);
                    generator_config.setUseActualColumnNames(useActualColumnNamesBox.getSelectedObjects() != null);
                    generator_config.setUseTableNameAlias(useTableNameAliasBox.getSelectedObjects() != null);
                    generator_config.setUseExample(useExampleBox.getSelectedObjects() != null);
                    generator_config.setUseLombokPlugin(useLombokBox.getSelectedObjects() != null);
                    generator_config.setUseSwaggerPlugin(useSwaggerBox.getSelectedObjects() != null);

                    generator_config.setModelMvnPath(modelMvnField.getText());
                    generator_config.setDaoMvnPath(daoMvnField.getText());
                    generator_config.setXmlMvnPath(xmlMvnField.getText());
                    boolean needSaveConfig = historyConfigList == null || !historyConfigList.containsKey(tableName);
                    result = new MybatisGenerator(generator_config).execute(anActionEvent, needSaveConfig);
                }

            }
            if (!result.isEmpty()) {
                Messages.showMessageDialog(Joiner.on("\n").join(result), "Warnning", Messages.getWarningIcon());
            }

        } catch (Exception e1) {
            Messages.showMessageDialog(e1.getMessage(), "Error", Messages.getErrorIcon());
        } finally {
            dispose();
        }
    }

    private void onCancel() {
        dispose();
    }
}
