package com.toppanidgate.mappingap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{
//	@Override
//	protected void configure(HttpSecurity security) throws Exception {
//		security.csrf().requireCsrfProtectionMatcher(AnyRequestMatcher.INSTANCE);
//		security.headers().xssProtection().and().contentSecurityPolicy("script-src 'self'");
//		security.httpBasic().disable();
////		security.authorizeRequests()
////		.antMatchers("/swagger-ui/**", "/openapi/**").permitAll()
////		.anyRequest().authenticated()
////		.and()
////		.httpBasic();
//	}
	
	  @Override
	    protected void configure(HttpSecurity http) throws Exception {
	        http.authorizeRequests()
	                .antMatchers("/swagger-ui/**", "/openapi/**", "/**").permitAll()
	                .anyRequest().authenticated()
	                .and()
	                .httpBasic();
	    }
	  

//	    @Bean
//	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//	        http.authorizeRequests()
//            .antMatchers("/**","/swagger-ui/**", "/openapi/**").permitAll()
//            .anyRequest().authenticated()
//            .and()
//            .httpBasic();
////	        http
////	            .authorizeHttpRequests((authz) -> authz
//////	            		.antMatchers("/**","/swagger-ui/**", "/openapi/**", "/COM_FIDO_AP/**").permitAll()
////	            		.anyRequest().permitAll()
//////	                .anyRequest().authenticated()
////	            )
////	            .httpBasic();
//	        return http.build();
//	    }

//	    @Override
//	    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//	        auth.inMemoryAuthentication()
//	                .withUser("idgate_user")
//	                .password(passwordEncoder().encode("idgate_user"))
//	                .authorities("ADMIN");
//	    }
//
//	    @Bean
//	    public PasswordEncoder passwordEncoder() {
//	        return new BCryptPasswordEncoder();
//	    }

		@Override
		public void configure(WebSecurity web) throws Exception {
			web.ignoring()
//		.antMatchers("/GW/svfCreateTxn/**")
//		.antMatchers("/GW/svfGetAuthRequest/**")
//		.antMatchers("/GW/svfGetDeregRequest/**")
//		.antMatchers("/GW/svfGetDeviceAll/**")
//		.antMatchers("/GW/svfGetRegRequest/**")
//		.antMatchers("/GW/svfSendAuthResponse/**")
//		.antMatchers("/GW/svfSendRegResponse/**")
//		.antMatchers("/swagger-ui/**")
//		.antMatchers("/GW/**")	// hello3
					.antMatchers("/**") // hello
//		.antMatchers("/GW/WSM/**")	// WSMServlet
			;
		}
		
//		 @Bean
//		    public WebSecurityCustomizer webSecurityCustomizer() {
//		        return (web) -> web.ignoring().antMatchers(
//		        		"/**"
//		        		, "/COM_FIDO_AP/**"
//		        		);
//		    }
}


