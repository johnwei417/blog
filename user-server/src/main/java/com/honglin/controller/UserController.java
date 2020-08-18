package com.honglin.controller;

import com.honglin.common.CommonResponse;
import com.honglin.entity.Roles;
import com.honglin.exceptions.DuplicateUserException;
import com.honglin.httpclient.blogClient;
import com.honglin.service.RoleService;
import com.honglin.service.impl.UserServiceImpl;
import com.honglin.vo.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class UserController {

    private final UserServiceImpl userService;

    private final RoleService roleService;

    private final blogClient blogClient;

    public UserController(UserServiceImpl userService, RoleService roleService, blogClient blogClient) {
        this.userService = userService;
        this.roleService = roleService;
        this.blogClient = blogClient;
    }


    @PostMapping("/register")
    public CommonResponse<UserDto> createUser(@RequestBody @Valid UserDto user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            if (bindingResult.getErrorCount() > 0) {
                List<FieldError> errorFields = bindingResult.getFieldErrors();
                for (FieldError fieldError : errorFields) {
                    return new CommonResponse<>(HttpStatus.SC_BAD_REQUEST, fieldError.getDefaultMessage());
                }
            }
        }

        try {
            userService.loadUserByUsername(user.getUsername());
            log.warn("Username already exist!");
            throw new DuplicateUserException("Username already exist!");
        } catch (UsernameNotFoundException e) {
            List<Roles> authorities = new ArrayList<>();
            authorities.add(roleService.getAuthorityById(1));
            user.setAuthorities(authorities);
            userService.save(user);
        }

        //call another service: blogClient to create another user table in blog2 database;
//        ExecutorService executorService = Executors.newFixedThreadPool(1);
//        Future<CommonResponse> future = executorService.submit(() -> blogClient.sync(user));
//        if (future.isDone()) {
//            try {
//                if (future.get().getCode() != 200) {
//                    User user1 = new User();
//                    BeanUtils.copyProperties(user, user1);
//                    userService.delete(user1);
//                    log.error("transaction error happens at blog server");
//                    return new CommonResponse<>(HttpStatus.SC_METHOD_FAILURE, "transaction at blog server failed");
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//        }
//        if (!executorService.isShutdown()) {
//            executorService.shutdownNow();
//        }
        log.info("User: " + user.getUsername() + " register success!");
        return new CommonResponse<>(200, user.getUsername() + " register success!");
    }
}