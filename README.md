# Free Enhanced MyBatis

[![IDEA 2022.1](https://img.shields.io/badge/IDEA-2022.1-yellowgreen.svg)](https://plugins.jetbrains.com/plugin/18699-free-enhanced-mybatis)
[![Apache-2.0](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

基于<https://gitee.com/wuzhizhan/free-mybatis-plugin>升级，支持IDEA 2022.1版本。

[中文](README.md) | [ENGLISH](README_EN.md)

## Description
A idea plugin for mybatis .
free-mybatis-plugin 是一款增强idea对mybatis支持的插件，主要功能如下：
- 生成mapper xml文件
- 快速从代码跳转到mapper及从mapper返回代码
- mybatis自动补全及语法错误提示

## 使用方法
free-mybatis-plugin是一个提高mybatis编码的插件。实现了dao代码跳转到mapper，mapper跳转回dao，mapper文件、statement查询自动生成功能。

### 灵活使用 ALT+ENTER 和 CTRL+B 实现提示和跳转
- 生成mapper文件
    - ALT+ENTER弹出<br/>
      ![](https://images.gitee.com/uploads/images/2020/0121/151849_26a01dec_131460.png)
      ![](https://images.gitee.com/uploads/images/2020/0121/151849_59d74c18_131460.jpeg)
- 生成statement语句
    - ALT+ENTER弹出<br/>
      ![](https://images.gitee.com/uploads/images/2020/0121/151849_594bfd4d_131460.jpeg)
- dao跳转到mapper（也可以CTRL+B跳入<br/>
  ![](https://images.gitee.com/uploads/images/2020/0121/151850_9821ea07_131460.jpeg)
- mapper跳转回dao（也可以CTRL+B跳入)<br/>
  ![](https://images.gitee.com/uploads/images/2020/0121/151850_6ff9859f_131460.jpeg)

### MyBatis generator gui使用方法
- 配置数据库<br/>
  ![](https://images.gitee.com/uploads/images/2020/0121/151850_7decd93e_131460.png)
  ![](https://images.gitee.com/uploads/images/2020/0121/151849_3b35abd0_131460.png)
- 在需要生成代码的表上右键，选择MyBatis-Generator，打开预览界面。<br/>
  ![](https://images.gitee.com/uploads/images/2020/0121/151849_6552ab20_131460.png)
- 配置生成参数<br/>
  ![](https://images.gitee.com/uploads/images/2020/0121/151849_7fd4ada5_131460.png)
- 注意：当数据库用MySQL 8，在URL上定义时区，推荐使用'?serverTimezone=GMT'，配置中勾选上MySQL 8选项。<br>

## 参考
- better-mybatis-generator https://github.com/kmaster/better-mybatis-generator
- mybatis-generator-gui https://github.com/zouzg/mybatis-generator-gui
- MyBatisCodeHelper-Pro https://github.com/gejun123456/MyBatisCodeHelper-Pro