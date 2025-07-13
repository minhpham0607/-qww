package org.example.lmsbackend.service;

import org.example.lmsbackend.dto.DiscussionDTO;
import org.example.lmsbackend.model.Course;
import org.example.lmsbackend.model.Discussion;
import org.example.lmsbackend.model.User;
import org.example.lmsbackend.repository.CourseRepository;
import org.example.lmsbackend.repository.DiscussionRepository;
import org.example.lmsbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DiscussionService {
    private final DiscussionRepository discussionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public DiscussionService(
            DiscussionRepository discussionRepository,
            CourseRepository courseRepository,
            UserRepository userRepository) {
        this.discussionRepository = discussionRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public List<DiscussionDTO> getAllDiscussions() {
        return discussionRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<DiscussionDTO> getDiscussionsByCourse(Integer courseId) {
        return discussionRepository.findByCourse_CourseId(courseId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public DiscussionDTO createDiscussion(DiscussionDTO dto) {
        Optional<Course> courseOpt = courseRepository.findById(dto.getCourseId());
        Optional<User> userOpt = userRepository.findById(dto.getUserId());
        if (courseOpt.isEmpty() || userOpt.isEmpty()) return null;
        Discussion discussion = new Discussion();
        discussion.setCourse(courseOpt.get());
        discussion.setUser(userOpt.get());
        discussion.setTitle(dto.getTitle());
        discussion.setContent(dto.getContent());
        Discussion saved = discussionRepository.save(discussion);
        return toDTO(saved);
    }

    public DiscussionDTO updateDiscussion(Integer id, DiscussionDTO dto) {
        Optional<Discussion> discussionOpt = discussionRepository.findById(id);
        if (discussionOpt.isEmpty()) return null;
        Discussion discussion = discussionOpt.get();
        discussion.setTitle(dto.getTitle());
        discussion.setContent(dto.getContent());
        Discussion saved = discussionRepository.save(discussion);
        return toDTO(saved);
    }

    public boolean deleteDiscussion(Integer id) {
        if (!discussionRepository.existsById(id)) return false;
        discussionRepository.deleteById(id);
        return true;
    }

    public DiscussionDTO toDTO(Discussion discussion) {
        DiscussionDTO dto = new DiscussionDTO();
        dto.setId(discussion.getId());
        dto.setCourseId(discussion.getCourse().getCourseId());
        dto.setUserId(discussion.getUser().getUserId());
        dto.setTitle(discussion.getTitle());
        dto.setContent(discussion.getContent());
        dto.setCreatedAt(discussion.getCreatedAt());
        return dto;
    }
}
