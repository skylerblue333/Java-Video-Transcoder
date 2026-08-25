package com.app;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class TranscoderTest {
    @Test
    void buildsArgumentVectorWithoutShellInterpolation() {
        var transcoder = new Transcoder("ffmpeg", Duration.ofMinutes(1));
        var command = transcoder.command(Path.of("input file.mov"), Path.of("output file.mp4"), Transcoder.Profile.MP4_720P);
        assertEquals("ffmpeg", command.get(0));
        assertTrue(command.contains("input file.mov") || command.stream().anyMatch(v -> v.endsWith("input file.mov")));
        assertTrue(command.stream().anyMatch(v -> v.endsWith("output file.mp4")));
        assertFalse(command.contains("sh"));
        assertFalse(command.contains("-c"));
    }

    @Test
    void rejectsUnsupportedExtensionsAndSamePath() {
        var transcoder = new Transcoder("ffmpeg", Duration.ofMinutes(1));
        assertThrows(IllegalArgumentException.class, () -> transcoder.command(Path.of("input.txt"), Path.of("out.mp4"), Transcoder.Profile.MP4_720P));
        assertThrows(IllegalArgumentException.class, () -> transcoder.command(Path.of("same.mp4"), Path.of("same.mp4"), Transcoder.Profile.MP4_720P));
        assertThrows(IllegalArgumentException.class, () -> transcoder.command(Path.of("input.mov"), Path.of("out.exe"), Transcoder.Profile.MP4_720P));
    }

    @Test
    void validatesTimeout() {
        assertThrows(IllegalArgumentException.class, () -> new Transcoder("ffmpeg", Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new Transcoder("ffmpeg", Duration.ofHours(3)));
    }
}
