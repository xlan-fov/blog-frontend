package com.blog.mapper;

import com.blog.entity.BlogsDraft;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: kai.hu
 * @Date: 2025-05-14-20:51
 * @Description:
 */
@Mapper
public interface BlogsDraftMapper {

    /*
     * @Author: kai.hu
     * @Date: 2025-5-14
     * @Description: 添加草稿信息并返回主键id
     */
    int addBlog(BlogsDraft blogsDraft);

    /*
     * @Author: kai.hu
     * @Date: 2025-5-14
     * @Description: 更新草稿信息
     */
    void updateBlog(BlogsDraft blogsDraft);
}
