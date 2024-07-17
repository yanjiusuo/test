package com.jd.workflow.console.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;

import com.github.pagehelper.PageInterceptor;
import com.jd.workflow.console.config.dao.MyMetaObjectHandler;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.sql.DataSource;

/**
 * @author caozhilong1
 * @date 2022/02/18 14:33
 **/
@Configuration
@MapperScan( basePackages = "com.jd.workflow.console.dao",sqlSessionFactoryRef="mybatisSqlSessionFactory")
public class MybatisPlusConfig {
	@Autowired
	MybatisPlusProperties properties;
	@Autowired
	MyMetaObjectHandler metaObjectHandler;
	/**
	 * 创建工厂
	 *
	 * @param
	 * @return SqlSessionFactory
	 * @throws Exception
	 */
	@Bean(name = "mybatisSqlSessionFactory")
	/*public SqlSessionFactory taskSqlSessionFactory(@Qualifier("masterDataSource") DataSource dataSource) throws Exception {
      	MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
		properties.getGlobalConfig().setMetaObjectHandler(metaObjectHandler);
		bean.setGlobalConfig(properties.getGlobalConfig());
		bean.setDataSource(dataSource);
		bean.setPlugins(new Interceptor[]{paginationInterceptor()});
		bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:plusmapper/*.xml"));
		return bean.getObject();
	}*/
	public MapperScannerConfigurer mapperScannerConfigurer(){
		MapperScannerConfigurer scannerConfigurer = new MapperScannerConfigurer();
		//可以通过环境变量获取你的mapper路径,这样mapper扫描可以通过配置文件配置了
		scannerConfigurer.setBasePackage("com.jd.workflow.console.dao.mapper");
		return scannerConfigurer;
	}

	/**
	 * 分页插件。如果你不配置，分页插件将不生效
	 */
	@Bean
	public MybatisPlusInterceptor paginationInterceptor() {
		JacksonTypeHandler.setObjectMapper(JsonUtils.mapper());

		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		// 指定数据库方言为 MYSQL
		interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
		return interceptor;
	}

	@Bean
	public Interceptor[] plugins() {
		return new Interceptor[]{new PageInterceptor()};
	}

}
