package com.sp2.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.sp2.model.User;
import com.sp2.repository.UserRepository;

@Service("userService")
public class UserService {

	private UserRepository userRepository;

	@Autowired
	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	
	/*public User findByConfirmationToken(String confirmationToken) {
		return userRepository.findByConfirmationToken(confirmationToken);
	}*/
	
	public void saveUser(User user) {
		userRepository.save(user);
	}

	public List<User> splitlist() {
		// TODO Auto-generated method stub
		return userRepository.splitlist();
	}

}