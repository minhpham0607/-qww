package org.example.lmsbackend.repository;

import org.apache.ibatis.annotations.*;
import org.example.lmsbackend.dto.ContentsDTO;

import java.util.List;

@Mapper
public interface ContentsMapper {

    @Insert("INSERT INTO contents (module_id, title, type, content_url, order_number) " +
            "VALUES (#{moduleId}, #{title}, #{type}, #{contentUrl}, #{orderNumber})")
    void insertContent(ContentsDTO content);

    @Select("""
        SELECT 
            c.content_id AS contentId,
            c.module_id AS moduleId,
            c.title,
            c.type,
            c.content_url AS contentUrl,
            c.order_number AS orderNumber
        FROM contents c
        JOIN modules m ON c.module_id = m.module_id
        WHERE m.course_id = #{courseId}
        ORDER BY m.order_number, c.order_number
    """)
    List<ContentsDTO> getContentsByCourseId(@Param("courseId") int courseId);

    @Update("""
        UPDATE contents SET 
            title = #{title},
            type = #{type},
            content_url = #{contentUrl},
            order_number = #{orderNumber}
        WHERE content_id = #{contentId}
    """)
    void updateContent(ContentsDTO content);

    @Delete("DELETE FROM contents WHERE content_id = #{contentId}")
    void deleteContent(@Param("contentId") int contentId);

    // ✅ Thêm để hỗ trợ kiểm tra quyền instructor khi update/delete
    @Select("SELECT module_id FROM contents WHERE content_id = #{contentId}")
    Integer getModuleIdByContentId(@Param("contentId") int contentId);
}

