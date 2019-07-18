package com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.Users;
import com.example.demo.repository.UsersRepository;
/*
 * This class is used to return UserDetails object by calling loadUserByUsername method.
 * This is used by spring security to perform authentication and various role based validation
 * the object is also contains userPrincipal object that will be used in the UserPrincipal class we defined 
 * */
@Service
public class CustomUserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

	@Autowired
	UsersRepository usersRepo;
	
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
		
		Users user = usersRepo.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
				.orElseThrow(() -> new UsernameNotFoundException("User not found for user name or email : "+ usernameOrEmail));
		return UserPrincipal.create(user);
	}
	
	//This method is used by JWTAuthenticationFilter
	
	@Transactional
	public UserDetails loadUserById(Long id) {
		
		Users user = usersRepo.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found for the ID : " + id));
		return UserPrincipal.create(user);
	}

}
