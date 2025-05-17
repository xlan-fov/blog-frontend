package com.blog.mapper;

import com.blog.entity.SensitiveWords;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 郭钰冉
 * @since 2025-05-05
 */
public interface SensitiveWordsMapper extends BaseMapper<SensitiveWords> {

    //查询敏感词是否存在
    @Select("SELECT COUNT(*) FROM sensitive_words WHERE word = #{word}")
    int countByWord(String word);

    @Select("SELECT * FROM sensitive_words WHERE word = #{word}")
    SensitiveWords selectByWord(String word);
}
