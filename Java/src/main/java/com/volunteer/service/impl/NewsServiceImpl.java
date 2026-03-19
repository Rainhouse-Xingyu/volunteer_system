package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.News;
import com.volunteer.mapper.NewsMapper;
import com.volunteer.service.NewsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 资讯服务实现类
 */
@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements NewsService {

    @Override
    public List<News> getLatestNews(int count) {
        LambdaQueryWrapper<News> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(News::getStatus, 1) // 1为已发布
                .orderByDesc(News::getCreatedAt)
                .last("LIMIT " + count);
        return list(queryWrapper);
    }
}
