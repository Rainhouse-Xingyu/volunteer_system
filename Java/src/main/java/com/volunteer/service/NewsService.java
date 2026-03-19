package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.News;
import java.util.List;

/**
 * 资讯服务类
 */
public interface NewsService extends IService<News> {

    /**
     * 获取最新资讯
     */
    List<News> getLatestNews(int count);
}
