package com.honglin.service.impl;

import com.honglin.dao.UserRep;
import com.honglin.entity.User;
import com.honglin.service.UserService;
import com.honglin.vo.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
public class UserServiceImpl implements UserDetailsService, UserService {
    @Autowired
    private UserRep userRep;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = userRep.findByUsername(s);
        if (user == null) {
            // log.warn("Invalid username or password.");
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ)
    public void save(UserDto user) {
        User newUser = new User();
        BeanUtils.copyProperties(user, newUser);
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setAuthorities(user.getAuthorities());
        userRep.save(newUser);
        log.info(user.getUsername() + " added to db successfully");
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class, isolation = Isolation.REPEATABLE_READ)
    public void delete(UserDto userDto) {
        userRep.deleteByUsername(userDto.getUsername());
        log.info(userDto.getUsername() + " remove from db successfully");
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class, isolation = Isolation.SERIALIZABLE)
    public void updatePassword(User user) {
        User updateUser = userRep.findByUsername(user.getUsername());
        updateUser.setPassword(user.getPassword());
        userRep.save(updateUser);
    }

}
