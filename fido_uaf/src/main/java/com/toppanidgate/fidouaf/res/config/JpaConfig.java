package com.toppanidgate.fidouaf.res.config;

import javax.sql.DataSource;

import com.toppanidgate.fidouaf.Log4j;
import com.toppanidgate.fidouaf.res.util.EncryptHelper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties("spring.datasource")
public class JpaConfig {
	private String url;
	private String driverClassName;
	private String username;
	private String password;
	
    @Bean
    public DataSource getDataSource()
    {
//    	Log4j.log.debug("*** username:" + this.username);
//    	Log4j.log.debug("*** password:" + this.password);
//    	Log4j.log.debug("*** driverClassName:" + this.driverClassName);
//    	Log4j.log.debug("*** url:" + this.url);

        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(this.driverClassName);
        dataSourceBuilder.url(this.url);
        
        try {
        	dataSourceBuilder.username(this.username);
			dataSourceBuilder.password(this.password);
		} catch (Exception e) {
			Log4j.log.error(e.getMessage());
		}
        return dataSourceBuilder.build();
    }
}