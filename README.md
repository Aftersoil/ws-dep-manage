<center>

![Aftersoil](https://avatars.githubusercontent.com/u/64077718)

## [Aftersoil](https://github.com/Aftersoil)

## [https://github.com/wangshu-g/ws-dep-manage](https://github.com/wangshu-g/ws-dep-manage)

</center>

### 介绍

根据 java 实体类相关信息，生成相关的 mybatis-xml（主要是生成连表、结果集映射、一些默认的条件参数）、mapper、service、controller 等代码

内置保存、修改、关联查询、列表、嵌套列表查询、表格数据导入/导出等api服务，自由选择继承、重载。项目基本功能实现，简化开发流程

### 使用

相关功能、使用参考 [该模板仓库代码](https://github.com/wangshu-g/ws-generate-test-example)

可以使用ws-generate模块手动生成、[该网站](https://www.望舒.com)网站上传生成、[该IDEA插件](https://plugins.jetbrains.com/embeddable/install/23060) 插件上传生成

网站可以配置一些前端表单、页面模板代码，对前端做了一些基本扩展

### 其他

不再使用 github packages，迁移到 maven 中央门户

### maven 依赖

``` xml
<!--基本功能-->
<dependency>
    <groupId>io.github.wangshu-g</groupId>
    <artifactId>ws-base</artifactId>
    <version>1.2.0</version>
</dependency>

<!--可选-->
<dependency>
    <groupId>io.github.wangshu-g</groupId>
    <artifactId>ws-spring-boot-starter</artifactId>
    <version>1.2.0</version>
</dependency>

<!--可选-->
<dependency>
    <groupId>io.github.wangshu-g</groupId>
    <artifactId>ws-generate</artifactId>
    <version>1.2.0</version>
</dependency>
```