package com.example.demo.controller;

import java.net.URI;
import java.util.Collections;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.demo.controller.command.request.LoginRequest;
import com.example.demo.controller.command.request.SignUpRequest;
import com.example.demo.controller.command.response.ApiResponse;
import com.example.demo.controller.command.response.JwtAuthenticationResponse;
import com.example.demo.domain.Role;
import com.example.demo.domain.RoleName;
import com.example.demo.domain.Users;
import com.example.demo.exception.AppException;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UsersRepository;
import com.example.demo.security.JwtTokenProvider;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private UsersRepository usersRepository;
	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private KafkaTemplate< String, String> kafkaTemplate;
	
	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest){
		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtTokenProvider.generateToken(authentication);
		sendMessage(loginRequest.getUsernameOrEmail());
		return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
		
	}
	
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest ){
		if(usersRepository.existsByUsername(signUpRequest.getUsername())) {
			return new ResponseEntity(new ApiResponse(false, "Username is already taken!"),
                    HttpStatus.BAD_REQUEST);
		}
		else if(usersRepository.existsByEmail(signUpRequest.getEmail())){
			return new ResponseEntity(new ApiResponse(false, "Email is already taken!"),
                    HttpStatus.BAD_REQUEST);
		}
		Users user = new Users();
		user.setEmail(signUpRequest.getEmail());
		user.setFirstName(signUpRequest.getName());
		user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
		user.setUserName(signUpRequest.getUsername());
		Role userRole = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow(() -> new AppException("User role not found"));
		user.setRoles(Collections.singleton(userRole));
		Users result = usersRepository.save(user);
		URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/users/{username}").buildAndExpand(result.getUserName()).toUri();
		return  ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
	}
	
	public void sendMessage(String msg) {
		   kafkaTemplate.send("test", msg);
		}  
@KafkaListener(topics = "test" , groupId = "group_id")
public void listen (String msg) {
	System.out.println("logged user is : "+ msg);
}
}
