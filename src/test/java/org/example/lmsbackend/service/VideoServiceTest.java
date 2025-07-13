package org.example.lmsbackend.service;

import org.example.lmsbackend.dto.VideoDTO;
import org.example.lmsbackend.model.Video;
import org.example.lmsbackend.repository.VideoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoServiceTest {

    private VideoMapper videoMapper;
    private VideoService videoService;

    @BeforeEach
    void setUp() {
        videoMapper = mock(VideoMapper.class);
        videoService = new VideoService(videoMapper);
    }

    @Test
    void testCreateVideo() {
        Video video = new Video();
        when(videoMapper.insertVideo(video)).thenReturn(1);
        assertEquals(1, videoService.createVideo(video));
    }

    @Test
    void testGetAllVideos() {
        Video video = new Video();
        video.setTitle("Test Video");
        when(videoMapper.findVideos(null)).thenReturn(Arrays.asList(video));

        List<VideoDTO> results = videoService.getAllVideos(null);
        assertEquals(1, results.size());
        assertEquals("Test Video", results.get(0).getTitle());
    }

    @Test
    void testGetVideoById() {
        Video video = new Video();
        video.setVideoId(1L);
        video.setTitle("Sample");

        when(videoMapper.findById(1L)).thenReturn(video);

        VideoDTO dto = videoService.getVideoById(1L);
        assertNotNull(dto);
        assertEquals("Sample", dto.getTitle());
    }

    @Test
    void testDeleteVideo() {
        when(videoMapper.deleteVideo(1L)).thenReturn(1);
        assertEquals(1, videoService.deleteVideo(1L));
    }

    @Test
    void testGetVideosByCourse() {
        Video video = new Video();
        video.setTitle("By Course");
        when(videoMapper.findVideosByCourseId(10)).thenReturn(Arrays.asList(video));

        List<VideoDTO> results = videoService.getVideosByCourse(10);
        assertEquals(1, results.size());
    }

    @Test
    void testUploadVideo_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("video", "test.mp4", "video/mp4", "data".getBytes());

        when(videoMapper.insertVideo(any())).thenReturn(1);

        VideoDTO dto = videoService.uploadVideo(file, "Test", "Desc", 1, 2);

        assertNotNull(dto);
        assertEquals("Test", dto.getTitle());
    }

    @Test
    void testUpdateVideo() {
        Video video = new Video();
        when(videoMapper.updateVideo(video)).thenReturn(1);
        assertEquals(1, videoService.updateVideo(video));
    }

    @Test
    void testIsInstructorOfVideo() {
        when(videoMapper.isInstructorOfVideo(1L, 2)).thenReturn(true);
        assertTrue(videoService.isInstructorOfVideo(1L, 2));
    }

    @Test
    void testCanStudentAccessVideo() {
        when(videoMapper.canStudentAccessVideo(1L, 3)).thenReturn(true);
        assertTrue(videoService.canStudentAccessVideo(1L, 3));
    }

    @Test
    void testGetVideoResource_FileNotFound() {
        when(videoMapper.findById(999L)).thenReturn(null);
        Resource resource = videoService.getVideoResource(999L);
        assertNull(resource);
    }

    @Test
    void testSaveFile_ReturnsPath() throws Exception {
        InputStream input = getClass().getResourceAsStream("/test.txt");
        if (input == null) {
            input = new java.io.ByteArrayInputStream("demo".getBytes());
        }

        MockMultipartFile file = new MockMultipartFile("file", "sample.txt", "text/plain", input);
        String result = videoService.saveFile(file);

        assertNotNull(result);
        assertTrue(result.contains("/videos/"));
    }
}
