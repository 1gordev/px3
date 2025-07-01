package com.id.px3.auth.logic;

import com.id.px3.auth.repo.UserRepo;
import com.id.px3.model.auth.UserDto;
import com.id.px3.model.auth.UserFindFiltered;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UserFinder {

    private final UserRepo userRepo;

    public UserFinder(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public List<UserDto> findFiltered(UserFindFiltered findFilteredReq) {
        boolean findByIds = (findFilteredReq.getIds() != null && !findFilteredReq.getIds().isEmpty());
        boolean findByRoles = (findFilteredReq.getRoles() != null && !findFilteredReq.getRoles().isEmpty());
        boolean findByIndexedProps = (findFilteredReq.getIndexedProps() != null && !findFilteredReq.getIndexedProps().isEmpty());

        if (findByIds && findByRoles && findByIndexedProps) {
            return userRepo.findByIdInAndRolesInAndIndexedProps(findFilteredReq.getIds(), findFilteredReq.getRoles(), findFilteredReq.getIndexedProps());
        } else if (findByIds && !findByRoles && !findByIndexedProps) {
            return userRepo.findByIdIn(findFilteredReq.getIds());
        } else if (!findByIds && findByRoles && !findByIndexedProps) {
            return userRepo.findByRolesIn(findFilteredReq.getRoles());
        } else if (!findByIds && !findByRoles && findByIndexedProps) {
            return userRepo.findByIndexedProps(findFilteredReq.getIndexedProps());
        } else if (findByIds && !findByRoles && findByIndexedProps) {
            return userRepo.findByIdInAndIndexedProps(findFilteredReq.getIds(), findFilteredReq.getIndexedProps());
        } else if (!findByIds && findByRoles && findByIndexedProps) {
            return userRepo.findByRolesInAndIndexedProps(findFilteredReq.getRoles(), findFilteredReq.getIndexedProps());
        } else if (findByIds && findByRoles && !findByIndexedProps) {
            return userRepo.findByIdInAndRolesIn(findFilteredReq.getIds(), findFilteredReq.getRoles());
        }
        return userRepo.findAll();
    }

    public UserDto findById(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        return userRepo.findByIdIn(List.of(userId)).stream().findFirst().orElse(null);
    }
}
