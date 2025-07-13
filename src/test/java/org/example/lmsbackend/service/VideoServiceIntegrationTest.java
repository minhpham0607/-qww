package org.example.lmsbackend.service;

import org.example.lmsbackend.dto.VideoDTO;
import org.example.lmsbackend.model.Course;
import org.example.lmsbackend.model.User;
import org.example.lmsbackend.model.Video;
import org.example.lmsbackend.repository.VideoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // Tự động rollback dữ liệu sau mỗi test để không làm thay đổi DB thật
class VideoServiceIntegrationTest {

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoMapper videoMapper;

    private Long testVideoId;

    // Thiết lập dữ liệu trước mỗi test
    @BeforeEach
    void setup() {
        // Tạo video mẫu
        Video video = new Video();
        video.setTitle("Integration Test Video");
        video.setDescription("Integration description");
        video.setFileUrl("/videos/test.mp4");

        // Gán course giả định có sẵn
        Course course = new Course();
        course.setCourseId(1);
        video.setCourse(course);

        // Gán instructor giả định có sẵn
        User instructor = new User();
        instructor.setUserId(1);
        video.setInstructor(instructor);

        // Chèn vào DB
        videoMapper.insertVideo(video);
        testVideoId = video.getVideoId();

        // Đảm bảo video được insert thành công
        assertNotNull(testVideoId);
    }

    @Test
    void getVideoById_ReturnsCorrectData() {
        // Kiểm tra lấy video theo ID có đúng dữ liệu không
        VideoDTO dto = videoService.getVideoById(testVideoId);
        assertNotNull(dto);
        assertEquals("Integration Test Video", dto.getTitle());
    }

    @Test
    void getVideoById_ShouldReturnNullIfNotExists() {
        // Kiểm tra nếu không tồn tại video, sẽ trả về null
        VideoDTO dto = videoService.getVideoById(999999L);
        assertNull(dto);
    }

    @Test
    void isInstructorOfVideo_ShouldReturnFalseForWrongInstructor() {
        // Kiểm tra instructor không đúng sẽ trả false
        boolean result = videoService.isInstructorOfVideo(testVideoId, 999);
        assertFalse(result);
    }

    @Test
    void canStudentAccessVideo_ShouldReturnFalseIfNotEnrolled() {
        // Kiểm tra học viên chưa học sẽ không được truy cập
        boolean canAccess = videoService.canStudentAccessVideo(testVideoId, 999);
        assertFalse(canAccess);
    }

    @Test
    void uploadVideo_Success() {
        // Kiểm thử upload video thành công
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.mp4", "video/mp4", "mock video content".getBytes()
        );

        assertDoesNotThrow(() -> {
            VideoDTO uploaded = videoService.uploadVideo(file, "New Video", "desc", 1, 1);
            assertNotNull(uploaded);
            assertEquals("New Video", uploaded.getTitle());
        });
    }

    @Test
    void deleteVideo_RemovesSuccessfully() {
        // Kiểm thử xóa video thành công
        int deleted = videoService.deleteVideo(testVideoId);
        assertEquals(1, deleted);

        // Sau khi xóa, gọi lại getVideoById phải trả về null
        assertNull(videoService.getVideoById(testVideoId));
    }

    @Test
    void deleteVideo_ShouldReturnZeroIfVideoNotFound() {
        // Kiểm thử xóa video không tồn tại (không bị lỗi, trả 0)
        int result = videoService.deleteVideo(999999L);
        assertEquals(0, result);
    }
}
