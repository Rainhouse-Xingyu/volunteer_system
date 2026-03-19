package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.News;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资讯 Mapper 接口
 */
@Mapper
public interface NewsMapper extends BaseMapper<News> {

}
