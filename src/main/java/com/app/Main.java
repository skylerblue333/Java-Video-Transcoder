package com.app;

import java.nio.file.Path;
import java.time.Duration;

public final class Main {
    private Main() {}

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || "--health".equals(args[0])) {
            System.out.println("{\"service\":\"sky-video-transcoder\",\"status\":\"ready\"}");
            return;
        }
        if (args.length != 3) {
            System.err.println("usage: <input> <output> <MP4_720P|MP4_1080P|AUDIO_AAC>");
            System.exit(2);
        }
        var profile = Transcoder.Profile.valueOf(args[2]);
        var transcoder = new Transcoder(System.getenv().getOrDefault("FFMPEG_BIN", "ffmpeg"), Duration.ofMinutes(30));
        var result = transcoder.transcode(Path.of(args[0]), Path.of(args[1]), profile);
        System.out.println("{\"success\":" + result.success() + ",\"exitCode\":" + result.exitCode() + "}");
        if (!result.success()) System.exit(1);
    }
}
