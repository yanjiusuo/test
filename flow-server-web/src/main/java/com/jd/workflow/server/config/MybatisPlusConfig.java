package com.jd.workflow.server.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.github.pagehelper.PageInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * @author caozhilong1
 * @date 2022/02/18 14:33
 **/
@Configuration
@MapperScan( basePackages = "com.jd.workflow.server.dao",sqlSessionFactoryRef="mybatisSqlSessionFactory")
public class MybatisPlusConfig {
	@Autowired
	MybatisPlusProperties properties;
	/**
	 * 创建工厂
	 *
	 * @param dataSource
	 * @return SqlSessionFactory
	 * @throws Exception
	 */
	@Bean(name = "mybatisSqlSessionFactory")
	public SqlSessionFactory taskSqlSessionFactory(@Qualifier("masterDataSource") DataSource dataSource) throws Exception {
      	MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
//		properties.getGlobalConfig().setMetaObjectHandler(metaObjectHandler);
		bean.setGlobalConfig(properties.getGlobalConfig());
		bean.setDataSource(dataSource);
		bean.setPlugins(new Interceptor[]{paginationInterceptor()});
		bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:plusmapper/*.xml"));
		return bean.getObject();
	}

	/**
	 * 分页插件。如果你不配置，分页插件将不生效
	 */
	@Bean
	public MybatisPlusInterceptor paginationInterceptor() {
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
