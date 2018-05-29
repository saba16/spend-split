package com.sp2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.sp2.model.User;

@Repository("userRepository")
public interface UserRepository extends CrudRepository<User, Long> {
	 User findByEmail(String email);
	/* User findByConfirmationToken(String confirmationToken);*/
	 
	 @Query("SELECT u FROM User u")
	    public List<User> splitlist();
}