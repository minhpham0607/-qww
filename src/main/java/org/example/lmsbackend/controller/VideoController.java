package org.example.lmsbackend.controller;

import org.example.lmsbackend.dto.VideoDTO;
import org.example.lmsbackend.model.Video;
import org.example.lmsbackend.service.VideoService;
import org.example.lmsbackend.service.CourseService;
import org.example.lmsbackend.service.EnrollmentsService;
import org.example.lmsbackend.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private EnrollmentsService enrollmentsService;

    // Lấy danh sách video theo khóa học - có phân quyền
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('admin', 'instructor', 'student')")
    public ResponseEntity<List<VideoDTO>> getVideosByCourse(@PathVariable Integer courseId,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Kiểm tra quyền truy cập khóa học
        if (userDetails.hasRole("instructor")) {
            if (!courseService.isInstructorOfCourse(userDetails.getUserId(), courseId)) {
                return ResponseEntity.status(403).build();
            }
        }
        if (userDetails.hasRole("student")) {
            if (!enrollmentsService.isStudentEnrolled(userDetails.getUserId(), courseId)) {
                return ResponseEntity.status(403).build();
            }
        }
        
        List<VideoDTO> videos = videoService.getVideosByCourse(courseId);
        return ResponseEntity.ok(videos);
    }

    // Upload video - chỉ instructor của khóa học
    @PostMapping("/upload")
    @PreAuthorize("hasRole('instructor')")
    public ResponseEntity<VideoDTO> uploadVideo(@RequestParam("file") MultipartFile file,
                                              @RequestParam("title") String title,
                                              @RequestParam("description") String description,
                                              @RequestParam("courseId") Integer courseId,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Kiểm tra giảng viên có dạy khóa này không
        if (!courseService.isInstructorOfCourse(userDetails.getUserId(), courseId)) {
            return ResponseEntity.status(403).build();
        }
        
        VideoDTO videoDTO = videoService.uploadVideo(file, title, description, courseId, userDetails.getUserId());
        if (videoDTO == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(videoDTO);
    }

    // Stream video - có phân quyền xem
    @GetMapping("/stream/{videoId}")
    @PreAuthorize("hasAnyRole('admin', 'instructor', 'student')")
    public ResponseEntity<Resource> streamVideo(@PathVariable Long videoId,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Kiểm tra quyền xem video
        if (userDetails.hasRole("instructor") && !videoService.isInstructorOfVideo(videoId, userDetails.getUserId())) {
            return ResponseEntity.status(403).build();
        }
        if (userDetails.hasRole("student") && !videoService.canStudentAccessVideo(videoId, userDetails.getUserId())) {
            return ResponseEntity.status(403).build();
        }
        
        Resource videoResource = videoService.getVideoResource(videoId);
        if (videoResource == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(videoResource);
    }

    // Xem chi tiết video - có phân quyền
    @GetMapping("/{videoId}")
    @PreAuthorize("hasAnyRole('admin', 'instructor', 'student')")
    public ResponseEntity<VideoDTO> getVideoById(@PathVariable Long videoId, 
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        VideoDTO video = videoService.getVideoById(videoId);
        if (video == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Kiểm tra quyền xem video
        if (userDetails.hasRole("instructor") && !videoService.isInstructorOfVideo(videoId, userDetails.getUserId())) {
            return ResponseEntity.status(403).build();
        }
        if (userDetails.hasRole("student") && !videoService.canStudentAccessVideo(videoId, userDetails.getUserId())) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(video);
    }

    // Cập nhật video - chỉ instructor của video
    @PutMapping("/{videoId}")
    @PreAuthorize("hasRole('instructor')")
    public ResponseEntity<String> updateVideo(@PathVariable Long videoId, 
                                            @RequestBody Video video, 
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Kiểm tra instructor có quyền sửa video này không
        if (!videoService.isInstructorOfVideo(videoId, userDetails.getUserId())) {
            return ResponseEntity.status(403).body("Bạn không có quyền sửa video này");
        }
        
        video.setVideoId(videoId);
        int updated = videoService.updateVideo(video);
        return updated > 0 ? ResponseEntity.ok("Video updated successfully")
                : ResponseEntity.notFound().build();
    }

    // Xóa video - chỉ instructor của video
    @DeleteMapping("/{videoId}")
    @PreAuthorize("hasRole('instructor')")
    public ResponseEntity<String> deleteVideo(@PathVariable Long videoId,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Chỉ giảng viên của video mới được xóa
        if (!videoService.isInstructorOfVideo(videoId, userDetails.getUserId())) {
            return ResponseEntity.status(403).body("Bạn không có quyền xóa video này");
        }
        
        int deleted = videoService.deleteVideo(videoId);
        return deleted > 0 ? ResponseEntity.ok("Video deleted successfully") : ResponseEntity.notFound().build();
    }

    // API tương thích với code cũ (không có phân quyền - chỉ dùng để test)
    @GetMapping
    public ResponseEntity<List<VideoDTO>> getAllVideos(@RequestParam(required = false) String title) {
        return ResponseEntity.ok(videoService.getAllVideos(title));
    }
}