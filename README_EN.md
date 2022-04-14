# Free Enhanced MyBatis

[![IDEA 2022.1](https://img.shields.io/badge/IDEA-2022.1-yellowgreen.svg)](https://plugins.jetbrains.com/plugin/18699-free-enhanced-mybatis)
[![Apache-2.0](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

Based on <https://gitee.com/wuzhizhan/free-mybatis-plugin>, support IDEA 2022.1.

[中文](README.md) | [ENGLISH](README_EN.md)

## Description
A idea plugin for mybatis .
free-mybatis-plugin is an enchange plugin for idea to supoort mybatis,here is the main functions:
- generate mapper xml files
- navigate from the code to mapper and from the mapper back to code
- auto code and error tips

## How To Use

### Generating and jumping with ALT+ENTER and CTRL+B 
- generate mapper files
    - ALT+ENTER<br/>
      ![](https://raw.githubusercontent.com/wuzhizhan/free-idea-mybatis/master/doc/img/create_mapper.png)
      ![](https://raw.githubusercontent.com/wuzhizhan/free-idea-mybatis/master/doc/img/choose_mapper_folder.jpg)
- generate statement
    - ALT+ENTER<br/>
      ![](https://raw.githubusercontent.com/wuzhizhan/free-idea-mybatis/master/doc/img/create_statement.jpg)
- from dao to  mapper(can also use CTRL+B)<br/>
  ![](https://raw.githubusercontent.com/wuzhizhan/free-idea-mybatis/master/doc/img/to_mapper.jpg)
- from mapper to dao(can also use CTRL+B)<br/>
  ![](https://raw.githubusercontent.com/wuzhizhan/free-idea-mybatis/master/doc/img/to_code.jpg)

### MyBatis generator gui usage
- database configuration<br/>
  ![](https://github.com/wuzhizhan/free-idea-mybatis/blob/master/doc/img/mgu_1.png)
  ![](https://github.com/wuzhizhan/free-idea-mybatis/blob/master/doc/img/mgu_2.png)
- select one or more tables, right click and select MyBatis-Generator to open generatoe main UI.<br>
  ![](https://github.com/wuzhizhan/free-idea-mybatis/blob/master/doc/img/mgu_3.png)
- MyBatis generator configuration<br/>
  ![](https://github.com/wuzhizhan/free-idea-mybatis/blob/master/doc/img/mgu_4.png)
- notice：If your database is MySQL 8，please add '?serverTimezone=GMT' and select MySQL 8 option<br>


## Reference
- better-mybatis-generator https://github.com/kmaster/better-mybatis-generator
- mybatis-generator-gui https://github.com/zouzg/mybatis-generator-gui
- MyBatisCodeHelper-Pro https://github.com/gejun123456/MyBatisCodeHelper-Pro