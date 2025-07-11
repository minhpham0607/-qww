package org.example.lmsbackend.repository;

import org.example.lmsbackend.model.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiscussionRepository extends JpaRepository<Discussion, Integer> {
    List<Discussion> findByCourse_CourseId(Integer courseId);
    List<Discussion> findByUser_UserId(Integer userId);
}
