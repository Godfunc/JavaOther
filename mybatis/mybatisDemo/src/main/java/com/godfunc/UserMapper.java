package com.godfunc;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface UserMapper {
    User selectUser(Long id);

    void insert(User user);

    void update(@Param("name") String name, @Param("age") Integer age, @Param("id") Long id);

    void updateMap(Map map);

    @MapKey("id")
    Map<Long, User> selectResultMap();


    User selectUserWithJob(Long id);

    UserJobs selectUserWithJobs(Long id);

    List<User> selectByUserName(@Param("name") String name);
}
