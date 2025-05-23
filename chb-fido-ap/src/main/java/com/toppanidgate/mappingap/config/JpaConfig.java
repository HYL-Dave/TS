package com.toppanidgate.mappingap.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.toppanidgate.fidouaf.common.model.Log4j;

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
	private String jndiName;
	
	private String maximum_pool_size;
	private String connection_timeout;
	private String max_wait;
	private String initial_size;
	
//	public static String fidoJNDI;
//	
//	@Value("${config_file_path}")
//	private String config_file_path;
	
    /**
     * For JAR
     * @return
     */
    @Bean
    public DataSource getDataSource()
    {
    	Log4j.log.info("*** jndiName:" + this.jndiName);
    	Log4j.log.info("*** username:" + this.username);
    	Log4j.log.info("*** password:" + this.password);
    	Log4j.log.info("*** driverClassName:" + this.driverClassName);
    	Log4j.log.info("*** url:" + this.url);

        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(this.driverClassName);
        dataSourceBuilder.url(this.url);
        try {
//        	dataSourceBuilder.username(EncryptHelper.Decrypt(this.username));
//        	dataSourceBuilder.password(EncryptHelper.Decrypt(this.password));
        	dataSourceBuilder.username(this.username);
			dataSourceBuilder.password(this.password);
		} catch (Exception e) {
			Log4j.log.error(e.getMessage());
		}
        return dataSourceBuilder.build();
    }
    
    /**
     * For WAR
     * @return
     */
//    @Bean
//	public DataSource dataSource() {
//		DataSource dataSource = null;
//		try {
////			Log4j.log.info("[{}][Version: {}][{}] *** FIDO JNDI@{}: {}", IDGateConfig.sessionId, IDGateConfig.svVerNo, thisClassName, "getDataSource", fidoJNDI);
////			Log4j.log.info("*** JNDI_Name@{}: {}", "getDataSource", Cfg.getExternalCfgValue("JNDI_Name"));
////	    dataSource = (DataSource) new InitialContext().lookup(Cfg.getExternalCfgValue("JNDI_Name"));
//			Context initialContex = new InitialContext();
////			dataSource = (DataSource) (initialContex.lookup(fidoJNDI));
//			dataSource = (DataSource) (initialContex.lookup(fidoJNDI));
//
////			if (dataSource != null) {
////				dataSource.getConnection();
////			}
//
//		} catch (Exception e) {
//			Log4j.log.error(e.getMessage());
//		}
//
//		return dataSource;
//	}
    
//	@PostConstruct
//	void init() {
////		try {
////			new Log4j(log4j_file_path);
////			new Log4jAP(log4j_file_path);
////			new Log4jInbound(log4j_file_path);
////			new Log4jSQL(log4j_file_path);
////		} catch (IOException e) {
////			Log4j.log.error(e);
////		}
//		Log4j.log.info("========================== Setting ==========================");
//		Properties properties = null;
//		File exCfgFile = new File(config_file_path);
//		try (InputStream is = new FileInputStream(exCfgFile);) {
//			properties = new Properties();
//			properties.load(is);
//			fidoJNDI = properties.getProperty("JNDI_Name", "java:jboss/datasource/CHB_FIDO_AP");
////			Log4j.log.info("[{}][Version: {}][{}.{}] JNDI: {}", IDGateConfig.sessionId, IDGateConfig.svVerNo, thisClassName, "init", fidoJNDI);
//			Log4j.log.info("[{}][Version: {}][{}] JNDI: {}", MappingConfig.sessionId, BaseServlet.svVerNo, "init", fidoJNDI);
//
//		} catch (IOException e) {
//			Log4j.log.fatal("[IDGateConfig][Version: " + BaseServlet.svVerNo + "][init] Unable to read config from ["
//					+ config_file_path + "]. " + e);
//			return;
//		} finally {
//			if (properties != null) {
//				properties.clear();
//			}
//		}
//	}
    
}