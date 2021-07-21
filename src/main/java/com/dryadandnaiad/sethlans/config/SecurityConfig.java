package com.dryadandnaiad.sethlans.config;

import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    protected void configure(HttpSecurity http) throws Exception {
        log.info("Configure");
        if (!PropertiesUtils.isFirstTime()) {
            log.info("Not First Time");
            http.authorizeRequests(authorize -> {
                authorize.antMatchers("/api/v1/info/version").permitAll();
            });
            http.authorizeRequests((requests) -> requests
                    .antMatchers("/api/v1/management/**").hasAnyAuthority(Role.ADMINISTRATOR.toString(), Role.SUPER_ADMINISTRATOR.toString())
                    .anyRequest().authenticated());
            http.formLogin();
            http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
        } else {
            log.info("first time");
            http.authorizeRequests().antMatchers("/*").permitAll();
            http.csrf().disable();
        }


    }

}
