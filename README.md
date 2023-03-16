# Spring boot 快速实现 mybatis-plus 的功能增强 ，如16位雪花ID ，逻辑删除恢复 ，乐观锁等
## 1.0.4版本

作用：
1. 提供16位雪花ID实现，解决默认19位实现导致的前端精度问题

2. 数据初始化时默认填充，insert时，自动获取cnaworld.mybatis-plus.fill-strategy-field 中的属性进行填充

3. 默认开启乐观锁实现 OptimisticLockerInnerInterceptor

4. 数据更新时对乐观锁字段累增后填充。解决mybatis-plus对自定义方法、逻辑删除方法更新时无法对数据乐观锁字段进行更新问题

5. 扩展逻辑删除相关方法，提供逻辑恢复和直接删除扩展方法

6. 客户端配置

   ```XML
   <dependency>
       <groupId>cn.cnaworld.framework</groupId>
       <artifactId>mybatis-plus</artifactId>
       <version>1.0.4</version>
   </dependency>
   ```

7. application.yml 配置

   ```yaml
   cnaworld:
     mybatis-plus:
       enabled: true #总开关,关闭后同时关闭所有增强功能，默认true开启，
       auto-insert-fill: true #数据初始化时默认填充，insert时，自动获取fill-strategy-field 中的属性进行填充。
       function-extension: true #扩展逻辑删除相关方法，提供逻辑恢复和直接删除扩展方法。默认true开启。
       snow-flake: true #提供16位雪花ID实现。默认true开启。
       optimistic-locker: true #提供乐观锁实现 OptimisticLockerInnerInterceptor。默认true开启。
       update-optimistic-locker-field: true #数据更新时对乐观锁字段累增后填充。默认true开启。
       fill-strategy-field: #数据初始化时默认填充属性集合
         - field-name: "createTimeDb" #需填充的entity字段名称，需要注解 @TableField(value = "create_time_db",fill = FieldFill.INSERT) 中开启fill = FieldFill.INSERT
           field-class: java.time.LocalDateTime #默认填充字段类型，Date 、Timestamp 、 LocalDateTime 默认取当前时间，Long 、Integer 默认取0。
         - field-name: "updateTimeDb" 
           field-processor-class: cn.cnaworld.cnaworldaoptest.api.AutoInsertFillProcessor #填充值处理器，实现FieldProcessor的getFieldValue方法，返回值作为属性填充值。
         - field-name: "deletedDb"
           field-value: false #填充值
   ```

8. 具体使用

   1. 16位雪花ID使用方式

      1. ```java
         //entity 主键注解type 采用IdType.ASSIGN_ID
         @TableId(value = "student_id", type = IdType.ASSIGN_ID)
         private Long studentId;
         ```

   2. 数据初始化时默认填充使用方式

      1. cnaworld.mybatis-plus.fill-strategy-field  添加相关配置 ， 需填充的entity字段名称 field-name 必须填写，field-value 、field-processor-class、field-class 任选其一。若全部配置优先级为 ：field-value > field-processor-class > field-class
         1. field-value 类型为 Object ，可直接配置字段值 ，若和 field-name 的实际类型不匹配则无法进行此值的填充 ，但不会影响其他值的填充
         2. field-processor-class 可配置实现了FieldProcessor接口getFieldValue方法的类的全限定名称，此接口支持客户端进行逻辑处理将结果提供给字段进行填充。getFieldValue方法返回值为 Object若和 field-name 的实际类型不匹配则无法进行此值的填充 ，但不会影响其他值的填充
         3. field-class 支持的类型为：Date 、Timestamp 、 LocalDateTime 默认取当前时间，Long 、Integer 默认取0
      2.  需要entity字段注解中开启 fill = FieldFill.INSERT 如： @TableField(value = "deleted_db",fill = FieldFill.INSERT)

   3. 乐观锁实现使用方式

      1. 乐观锁采用官网推荐的OptimisticLockerInnerInterceptor(true) , 且开启了wrapperMode 模块。

      2. 需要entity字段添加@Version注解

      3. ```java
         @Version
         private LocalDateTime updateTimeDb;
         ```

   4. 数据更新时对乐观锁字段累增后填充

      1. 官网乐观锁插件 OptimisticLockerInnerInterceptor 问题：对于逻辑删除、自定义方法、入参不存在乐观锁字段的方法 ，插件都无法对数据库的乐观锁字段的值进行累加或者更新

      2. 需要entity字段添加@Version注解

      3. ```java
         @Version
         private LocalDateTime updateTimeDb;
         ```

      4. 对所有添加乐观锁注解的字段，在执行Update语句时进行更新，包括逻辑删除、逻辑恢复、自定义方法

   5. 扩展逻辑删除相关方法，提供逻辑恢复和直接删除扩展方法

      1. 若entity字段增加逻辑删除注解支持，则所有的mybatis-plus提供的Service删除方法及ServiceImpl 的 remove()、delete() , 方法都会开启逻辑删除

      2. 需要由继承mybatis-plus 的IService<T>、ServiceImpl<TMapper, T>、BaseMapper<T>，改为继承CnaWorldBaseService<T>、CnaWorldBaseServiceImpl<TMapper, T> 、CnaWorldBaseMapper<T>

      3. 同时提供了CnaWorldBaseEntity 可供使用

         1. 建议使用updateTimeDb同时作为乐观锁字段
         2. 建议使用deletedDb同时作为逻辑删除字段

      4. CnaWorldBaseService  扩展方法提供逻辑恢复 和 直接删除

         ```java
         //逻辑恢复
         default boolean restore(Wrapper<T> queryWrapper)
         //逻辑恢复
         default boolean restoreById(Serializable id)
         //逻辑恢复 
         default boolean restoreByMap(Map<String, Object> columnMap)
         //逻辑恢复 
         default boolean restoreByIds(Collection<? extends Serializable> idList)
         //直接删除
         default boolean directRemove(Wrapper<T> queryWrapper)
         //直接删除
         default boolean directRemoveById(Serializable id)
         //直接删除
         default boolean directRemoveByMap(Map<String, Object> columnMap)
         //直接删除
         default boolean directRemoveByIds(Collection<? extends Serializable> idList) 
         ```

      5. CnaWorldBaseMapper 扩展方法提供逻辑恢复 和 直接删除

         ```java
         //逻辑恢复
         int recover(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
         //逻辑恢复
         int recoverById(Serializable id);
         //逻辑恢复
         int recoverByMap(@Param(Constants.COLUMN_MAP) Map<String, Object> columnMap);
         //逻辑恢复
         int recoverBatchIds(@Param(Constants.COLLECTION) Collection<? extends Serializable> idList);
         //直接删除
         int directDelete(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
         //直接删除
         int directDeleteById(Serializable id);
         //直接删除
         int directDeleteByMap(@Param(Constants.COLUMN_MAP) Map<String, Object> columnMap);
         //直接删除
         int directDeleteBatchIds(@Param(Constants.COLLECTION) Collection<? extends Serializable> idList);
         ```

      6. 自动生成代码类,可直接配置 superClass 继承扩展实现

         ```java
         package cn.cnaworld.cnaworldaoptest;
         
         import cn.cnaworld.framework.infrastructure.component.mybatisplus.baseclass.entity.CnaWorldBaseEntity;
         import cn.cnaworld.framework.infrastructure.component.mybatisplus.baseclass.mapper.CnaWorldBaseMapper;
         import cn.cnaworld.framework.infrastructure.component.mybatisplus.baseclass.service.CnaWorldBaseService;
         import cn.cnaworld.framework.infrastructure.component.mybatisplus.baseclass.service.impl.CnaWorldBaseServiceImpl;
         import com.baomidou.mybatisplus.annotation.IdType;
         import com.baomidou.mybatisplus.generator.AutoGenerator;
         import com.baomidou.mybatisplus.generator.config.*;
         import com.baomidou.mybatisplus.generator.config.builder.GeneratorBuilder;
         import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
         import com.baomidou.mybatisplus.generator.config.querys.MySqlQuery;
         import com.baomidou.mybatisplus.generator.config.rules.DateType;
         import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
         import com.baomidou.mybatisplus.generator.keywords.MySqlKeyWordsHandler;
         
         
         public class CnaWorldBaseCodeGenerator {
         	
         	public static void main(String[] args) {
         		DataSourceConfig mysqlDataSourceConfig = new DataSourceConfig.Builder(
         				"jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&useSSL=false&characterEncoding=utf8","root","root")
         				.typeConvert(new MySqlTypeConvert())
         				.keyWordsHandler(new MySqlKeyWordsHandler())
         				.dbQuery(new MySqlQuery())
         				.build();
         		GlobalConfig globalConfig = GeneratorBuilder.globalConfigBuilder()
         				.fileOverride()//是否覆盖已生成文件 默认值:false
         				.openDir(false)//是否打开生成目录 默认值:true 
         				.outputDir("D:\\CodeRepository\\github\\tntfyf\\cnaworld-aop-test\\src\\main\\java\\")//指定输出目录 默认值: windows:D:// linux or mac : /tmp
         				.author("Lucifer")//作者名 默认值:无
         				//.enableKotlin()//是否生成kotlin 默认值:false
         				.enableSwagger()//是否生成swagger注解 默认值:false
         				.dateType(DateType.TIME_PACK)//时间策略 默认值: DateType.TIME_PACK
         				.commentDate("yyyy-MM-dd")//注释日期 默认值: yyyy-MM-dd
         				.build();
         		PackageConfig packageConfig = new PackageConfig.Builder()
         				.parent("cn.cnaworld.cnaworldaoptest.domain") //父包名 默认值:com.baomidou
         				.moduleName("student") //父包模块名 默认值:无
         				.entity("entity") //Entity包名 默认值:entity
         				.service("service") //Service包名 默认值:service
         				.serviceImpl("service.impl") //Service Impl包名 默认值:service.impl
         				.mapper("mapper") //Mapper包名 默认值:mapper
         				.xml("mapper.xml") //Mapper XML包名 默认值:mapper.xml
         				.controller("controller") //Controller包名 默认值:controller
         				.build();
         		TemplateConfig templateConfig = new TemplateConfig.Builder().build(); // 激活所有默认模板
         		//LikeTable likeTable=new LikeTable("operate_log");
         		StrategyConfig strategyConfig = new StrategyConfig.Builder()
         				.enableCapitalMode()//开启大写命名
         				//.enableSkipView()//开启跳过视图
         				//.disableSqlFilter()//禁用sql过滤
         				//.likeTable(likeTable)//模糊表匹配(sql过滤)
         				//.notLikeTable(null)//模糊表匹配(sql过滤)
         				//.addFieldPrefix("")//增加表字段前缀
         				.addInclude("student")//增加表匹配(内存过滤)
         				//.addInclude("email_account_manage")//增加表匹配(内存过滤)
         				//.addExclude("")//增加表排除匹配(内存过滤)
         				//.addTablePrefix("")//增加表前缀
         				//实体策略配置
         				.entityBuilder()//实体策略配置
         					//.nameConvert(null)//名称转换实现
         					.superClass(CnaWorldBaseEntity.class)//父类W
         					//.enableColumnConstant()//开启生成字段常量W
         					//.enableChainModel()//开启链式模型
         					//.addSuperEntityColumns("")//添加父类公共字段
         					//.addTableFills(new Column(null),new Column(null))//添加属性填充字段
         					//.enableActiveRecord()//开启ActiveRecord模型
         					//.convertFileName(null)//转换文件名称
         					//.formatFileName(null)//格式化文件名称
         					//.enableSerialVersionUID()//开启生成serialVersionUID
         					.enableLombok()//开启lombok模型
         					.addIgnoreColumns("deleted_db","create_by_db","update_by_db","create_time_db","update_time_db")
         					.enableRemoveIsPrefix()//开启Boolean类型字段移除is前缀
         					.enableTableFieldAnnotation()//开启生成实体时生成字段注解
         					.versionColumnName("update_time_db")//乐观锁字段名(数据库)
         					.versionPropertyName("updateTimeDb")//乐观锁属性名(实体)
         					.logicDeleteColumnName("deleted_db")//逻辑删除字段名(数据库)
         					.logicDeletePropertyName("deleteDB")//逻辑删除属性名(实体)
         					.naming(NamingStrategy.underline_to_camel)//数据库表映射到实体的命名策略 默认:NamingStrategy.no_change
         					.columnNaming(NamingStrategy.underline_to_camel)//数据库表字段映射到实体的命名策略
         					.idType(IdType.ASSIGN_ID)//全局主键类型
         				//controller策略配置
         				.controllerBuilder()//controller策略配置
         					//.superClass(CodeGenerator.class)//父类
         					//.convertFileName(null)//转换文件名称
         					//.formatFileName("")//格式化文件名称
         					.enableHyphenStyle()//开启驼峰转连字符
         					.enableRestStyle()//开启生成@RestController控制器
         				//service策略配置
         				.serviceBuilder()//service策略配置
         					.superServiceClass(CnaWorldBaseService.class)//设置service接口父类
         					.superServiceImplClass(CnaWorldBaseServiceImpl.class)//设置service实现类父类
         					//.convertServiceFileName(null)//转换service接口文件名称
         					//.convertServiceImplFileName(null)//转换service实现类文件名称
         					//.formatServiceFileName(null)//格式化service接口文件名称
         					//.formatServiceImplFileName(null)//格式化service实现类文件名称	
         				//mapperBuilder
         				.mapperBuilder()//mapperBuilder
         					.superClass(CnaWorldBaseMapper.class)//设置父类
         					//.cache(null)//设置缓存实现类
         					//.formatMapperFileName("")//格式化mapper文件名称
         					//.formatXmlFileName("")//格式化xml实现类文件名称
         					//.convertMapperFileName(null)//转换mapper类文件名称
         					//.convertXmlFileName(null)//转换xml文件名称
         					.enableBaseResultMap()//启用BaseResultMap生成
         					.enableBaseColumnList()//启用BaseColumnList
         				.build();
         
         		InjectionConfig injectionConfig = new InjectionConfig.Builder().build();
         		// 代码生成器
         		new AutoGenerator(mysqlDataSourceConfig).global(globalConfig).packageInfo(packageConfig)
         				.strategy(strategyConfig)
         				.template(templateConfig)
         				.injection(injectionConfig)
         				.execute();
         	}
         }
         ```
9. 客户端中也加入此基础Entity类 ， 放到客户端方便切换openapi 2.0、3.0 ，mybatisplus.generator 提供的模板默认是2.0的故，此类也默认采用2.0的实现。可采用自定义模板的方式调整为3.0实现。

```java
package cn.cnaworld.base.infrastructure.component.baseclass;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基本实体类
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@ApiModel(value = "CnaWorldBaseEntity对象")
public class CnaWorldBaseEntity implements Serializable {

    /**
     * 逻辑删除
     */
    @ApiModelProperty(value = "逻辑删除")
    @TableField(value = "deleted_db",fill = FieldFill.INSERT)
    @TableLogic
    private Boolean deletedDb;

    /**
     * 创建者
     */
    @ApiModelProperty(value = "创建者")
    @TableField("create_by_db")
    private String createByDb;

    /**
     * 更新者
     */
    @ApiModelProperty(value = "更新者")
    @TableField("update_by_db")
    private String updateByDb;

    /**
     * 创建日期
     */
    @ApiModelProperty(value = "创建日期")
    @TableField(value = "create_time_db",fill = FieldFill.INSERT)
    private LocalDateTime createTimeDb;

    /**
     * 更新日期
     */
    @ApiModelProperty(value = "更新日期")
    @TableField(value = "update_time_db",fill = FieldFill.INSERT)
    @Version
    private LocalDateTime updateTimeDb;

}

```

10. 启动注册

启动时，会打印开启的组件的注册日志。

   ```lua
   2023-03-07 23:24:31.263  INFO 49200 --- [           main] c.c.f.i.c.m.config.MybatisPlusConfig     : cnaworld mybatis-plus optimistic-locker initialized ！
   2023-03-07 23:24:31.265  INFO 49200 --- [           main] c.c.f.i.c.m.config.MybatisPlusConfig     : cnaworld mybatis-plus update-optimistic-locker-field initialized ！
   2023-03-07 23:24:31.286  INFO 49200 --- [           main] c.c.f.i.c.m.config.MybatisPlusConfig     : cnaworld mybatis-plus auto-insert-fill initialized ！
   2023-03-07 23:24:31.300  INFO 49200 --- [           main] c.c.f.i.c.m.config.MybatisPlusConfig     : cnaworld mybatis-plus extend method initialized ！
   2023-03-07 23:24:31.304  INFO 49200 --- [           main] c.c.f.i.c.m.config.MybatisPlusConfig     : cnaworld mybatis-plus 16-snowflake initialized ！
   ```
11. 客户端应用demo
    [应用demo](http://t.csdn.cn/nX3e4)
