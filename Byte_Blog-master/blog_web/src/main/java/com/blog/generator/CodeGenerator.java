package com.blog.generator;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.config.rules.DateType;

public class CodeGenerator {
    public static void main(String[] args) {
        // 使用 AutoGenerator 的构建器方法来进行配置
        AutoGenerator generator = new AutoGenerator();

        // 配置全局参数
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setOutputDir(System.getProperty("user.dir") + "/blog_web/src/main/java"); // 设置代码生成输出目录
        globalConfig.setAuthor("郭钰冉"); // 设置作者
        globalConfig.setOpen(false); // 是否打开文件夹
        globalConfig.setFileOverride(true); // 是否覆盖文件
        globalConfig.setDateType(DateType.ONLY_DATE); // 日期格式
        globalConfig.setSwagger2(false); // 是否开启 Swagger2 注解

        // 数据源配置
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setUrl("jdbc:mysql://localhost:3306/blogdb?useSSL=false&serverTimezone=UTC");
        dataSourceConfig.setDriverName("com.mysql.jdbc.Driver");
        dataSourceConfig.setUsername("root");
        dataSourceConfig.setPassword(" ");

        // 包配置
        PackageConfig packageConfig = new PackageConfig();
        packageConfig.setParent("com.blog"); // 设置根包名
        packageConfig.setEntity("entity"); // 实体类包名
        packageConfig.setMapper("mapper"); // Mapper 类包名
        packageConfig.setXml("mapper.xml"); // Mapper XML 包名
        packageConfig.setService("service"); // Service 类包名
        packageConfig.setServiceImpl("service.impl"); // Service 实现包名
        packageConfig.setController("controller");   //  添加 Controller 包名
        generator.setPackageInfo(packageConfig);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setXml(null); // 关闭 XML 文件生成
        generator.setTemplate(templateConfig);

        // 配置策略
        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig.setNaming(NamingStrategy.underline_to_camel); // 表名生成策略：下划线转驼峰
        strategyConfig.setColumnNaming(NamingStrategy.underline_to_camel); // 字段名生成策略
        strategyConfig.setEntityLombokModel(true); // 是否使用 Lombok
        strategyConfig.setRestControllerStyle(true); // 是否生成 RestController 风格
        strategyConfig.setInclude(
                "admin_actions",
                "anomalies",
                "ban_logs",
                "blogs",
                "failed_login_attempts",
                "login_logs",
                "sensitive_words",
                "users",
                "verification_codes"
        ); // 要生成的表名

        // 设置各项配置
        generator.setGlobalConfig(globalConfig);
        generator.setDataSource(dataSourceConfig);
        generator.setPackageInfo(packageConfig);
        generator.setStrategy(strategyConfig);
        generator.setTemplate(templateConfig);

        // 执行生成
        generator.execute();
    }
}
