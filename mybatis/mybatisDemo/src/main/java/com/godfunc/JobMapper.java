package com.godfunc;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface JobMapper {

    List<Job> jobs(@Param("uid") Integer id, @Param("name") String name);

    Job select(Long id);
}
