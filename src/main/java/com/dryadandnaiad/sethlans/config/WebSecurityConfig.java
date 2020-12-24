/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans.config;

import com.dryadandnaiad.sethlans.services.SethlansUserDetailsService;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * File created by Mario Estrella on 5/26/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final SethlansUserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final DaoAuthenticationProvider authenticationProvider;

    public WebSecurityConfig(
            SethlansUserDetailsService userDetailsService,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            DaoAuthenticationProvider authenticationProvider) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authenticationProvider = authenticationProvider;
    }

    @Autowired
    public void configureAuthManager(AuthenticationManagerBuilder authenticationManagerBuilder) {
        authenticationManagerBuilder.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (!PropertiesUtils.isFirstTime()) {
            http
                    .authorizeRequests()
                    .antMatchers("/", "/home").permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .formLogin()
                    .loginPage("/login")
                    .permitAll()
                    .and()
                    .authorizeRequests()
                    .antMatchers("/api/v1/info/node_info")
                    .permitAll()
                    .and()
                    .logout()
                    .permitAll();
        } else {
            http.authorizeRequests().antMatchers("/*").permitAll();
        }

    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }


    @Override
    public void configure(WebSecurity web) {
        if (!PropertiesUtils.isFirstTime()) {
            web.ignoring()
                    .antMatchers(HttpMethod.OPTIONS, "/**")
                    .antMatchers("/*.js")
                    .antMatchers("/*.css")
                    .antMatchers("/color.*.png")
                    .antMatchers("/*.woff*")
                    .antMatchers("/*.ttf")
                    .antMatchers("/*.eot")
                    .antMatchers("/*.svg")
                    .antMatchers("/line.*.gif")
                    .antMatchers("/api/nodeactivate/**")
                    .antMatchers("/api/benchmark/**")
                    .antMatchers("/api/benchmark_files/**")
                    .antMatchers("/api/project/**")
                    .antMatchers("/api/render/**")
                    .antMatchers("/api/update/**")
                    .antMatchers("/assets/images/**");
        } else {
            web.ignoring().antMatchers("/**");
        }

    }
}
