package org.example.lmsbackend.service;

import org.example.lmsbackend.dto.ModulesDTO;
import org.example.lmsbackend.repository.ModulesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModulesService {

    private final ModulesMapper modulesMapper;

    public ModulesService(ModulesMapper modulesMapper) {
        this.modulesMapper = modulesMapper;
    }
    // Thêm module — dùng ContentsDTO thay vì ModuleRequest
    public void createModule(ModulesDTO module) {
        modulesMapper.insertModule(module);
    }

    // Lấy danh sách modules theo courseId
    public List<ModulesDTO> getModulesByCourseId(int courseId) {
        return modulesMapper.getModulesByCourseId(courseId);
    }

    // Cập nhật module
    public void updateModule(ModulesDTO module) {
        modulesMapper.updateModule(module);
    }

    // Xóa module
    public void deleteModule(int moduleId) {
        modulesMapper.deleteModule(moduleId);
    }
    public int getCourseIdByModuleId(int moduleId) {
        return modulesMapper.getCourseIdByModuleId(moduleId);
    }



}
