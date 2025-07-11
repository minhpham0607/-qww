package org.example.lmsbackend.repository;

import org.apache.ibatis.annotations.*;
import org.example.lmsbackend.dto.ModulesDTO;


import java.util.List;

@Mapper
public interface ModulesMapper {

    // Thêm module
    @Insert("INSERT INTO modules (course_id, title, description, order_number) " +
            "VALUES (#{courseId}, #{title}, #{description}, #{orderNumber})")
    void insertModule(ModulesDTO module);  // ✅ Đổi từ ModuleRequest → ContentsDTO
    // Lấy danh sách modules theo courseId
    @Select("SELECT module_id AS moduleId, course_id AS courseId, title, description, order_number AS orderNumber " +
            "FROM modules WHERE course_id = #{courseId} ORDER BY order_number")
    List<ModulesDTO> getModulesByCourseId(@Param("courseId") int courseId);

    // Cập nhật module
    @Update("UPDATE modules SET title = #{title}, description = #{description}, order_number = #{orderNumber} " +
            "WHERE module_id = #{moduleId}")
    void updateModule(ModulesDTO module);

    // Xóa module theo ID
    @Delete("DELETE FROM modules WHERE module_id = #{moduleId}")
    void deleteModule(@Param("moduleId") int moduleId);
    @Select("SELECT course_id FROM modules WHERE module_id = #{moduleId}")
    int getCourseIdByModuleId(@Param("moduleId") int moduleId);
}
