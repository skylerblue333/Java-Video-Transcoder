package com.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class Transcoder {
    public enum Profile {
        MP4_720P(List.of("-vf", "scale=-2:720", "-c:v", "libx264", "-preset", "medium", "-crf", "23", "-c:a", "aac", "-b:a", "128k")),
        MP4_1080P(List.of("-vf", "scale=-2:1080", "-c:v", "libx264", "-preset", "medium", "-crf", "23", "-c:a", "aac", "-b:a", "160k")),
        AUDIO_AAC(List.of("-vn", "-c:a", "aac", "-b:a", "160k"));

        private final List<String> args;
        Profile(List<String> args) { this.args = args; }
        List<String> args() { return args; }
    }

    private static final Set<String> INPUT_EXTENSIONS = Set.of(".mp4", ".mov", ".mkv", ".webm", ".avi", ".m4v", ".mp3", ".wav", ".flac");
    private final String ffmpegBinary;
    private final Duration timeout;

    public Transcoder(String ffmpegBinary, Duration timeout) {
        if (ffmpegBinary == null || ffmpegBinary.isBlank()) throw new IllegalArgumentException("ffmpeg binary is required");
        if (timeout == null || timeout.isZero() || timeout.isNegative() || timeout.compareTo(Duration.ofHours(2)) > 0) {
            throw new IllegalArgumentException("timeout must be >0 and <=2 hours");
        }
        this.ffmpegBinary = ffmpegBinary;
        this.timeout = timeout;
    }

    public List<String> command(Path input, Path output, Profile profile) {
        validatePaths(input, output);
        if (profile == null) throw new IllegalArgumentException("profile is required");
        var args = new java.util.ArrayList<String>();
        args.add(ffmpegBinary);
        args.add("-hide_banner");
        args.add("-nostdin");
        args.add("-y");
        args.add("-i");
        args.add(input.toAbsolutePath().normalize().toString());
        args.addAll(profile.args());
        args.add("-movflags");
        args.add("+faststart");
        args.add(output.toAbsolutePath().normalize().toString());
        return List.copyOf(args);
    }

    public Result transcode(Path input, Path output, Profile profile) throws IOException, InterruptedException {
        if (!Files.isRegularFile(input)) throw new IllegalArgumentException("input must be an existing regular file");
        var command = command(input, output, profile);
        var process = new ProcessBuilder(command).redirectErrorStream(true).start();
        boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            process.waitFor();
            return new Result(false, -1, "transcode timed out");
        }
        byte[] outputBytes = process.getInputStream().readNBytes(64 * 1024);
        String log = new String(outputBytes, java.nio.charset.StandardCharsets.UTF_8);
        return new Result(process.exitValue() == 0 && Files.isRegularFile(output), process.exitValue(), log);
    }

    private static void validatePaths(Path input, Path output) {
        if (input == null || output == null) throw new IllegalArgumentException("input and output are required");
        if (input.toAbsolutePath().normalize().equals(output.toAbsolutePath().normalize())) throw new IllegalArgumentException("input and output must differ");
        String inputName = input.getFileName().toString().toLowerCase();
        boolean allowed = INPUT_EXTENSIONS.stream().anyMatch(inputName::endsWith);
        if (!allowed) throw new IllegalArgumentException("unsupported input extension");
        String outputName = output.getFileName().toString().toLowerCase();
        if (!(outputName.endsWith(".mp4") || outputName.endsWith(".m4a"))) throw new IllegalArgumentException("output must be .mp4 or .m4a");
    }

    public record Result(boolean success, int exitCode, String log) {}
}
