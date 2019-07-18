package com.example.demo.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/*
 * This class is the filter that gets token from the request , validate it , 
 * load the user associated to it and pass it to spring security 
 * */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	
	@Autowired
    private JwtTokenProvider tokenProvider;
	
	@Autowired
    private CustomUserDetailsService customUserDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		try {
			
		String jwt = getJwtFromRequest(request);
		//from the extracted token , get the user Id and the load user detail and set the authentication in the spring security context
		if(StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
			Long userId = tokenProvider.getUserIdFromJwtToken(jwt);
			
			UserDetails userDetails = customUserDetailsService.loadUserById(userId);
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		}catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }
		
		filterChain.doFilter(request, response);
		
	}
	//Extract the token from  request header
	public String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("bearer ")) {
			
            return bearerToken.substring(7, bearerToken.length());
		}
		return null;
	}
	
	

}
