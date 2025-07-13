//package org.example.lmsbackend;
//
//import org.example.lmsbackend.model.Course;
//import org.example.lmsbackend.model.Discussion;
//import org.example.lmsbackend.model.User;
//import org.junit.jupiter.api.Test;
//
//import java.time.Instant;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class DiscussionTest {
//
//    @Test
//    void testDiscussionSettersAndGetters() {
//        Discussion discussion = new Discussion();
//
//        Course course = new Course();
//        User user = new User();
//        Instant now = Instant.now();
//
//        discussion.setId(1);
//        discussion.setCourse(course);
//        discussion.setUser(user);
//        discussion.setTitle("Test Title");
//        discussion.setContent("Test Content");
//        discussion.setCreatedAt(now);
//
//        assertEquals(1, discussion.getId());
//        assertEquals(course, discussion.getCourse());
//        assertEquals(user, discussion.getUser());
//        assertEquals("Test Title", discussion.getTitle());
//        assertEquals("Test Content", discussion.getContent());
//        assertEquals(now, discussion.getCreatedAt());
//    }
//}
