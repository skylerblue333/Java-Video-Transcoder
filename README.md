# Sky Video Transcoder (Java)

**Status: engineering beta.** This repository wraps the local FFmpeg binary from Java 21 using `ProcessBuilder` argument vectors rather than shell command strings. It provides a small set of bounded media profiles and explicit timeout/path validation.

## Implemented

- real Java 21 implementation
- FFmpeg execution without shell interpolation
- input extension allowlist
- output restricted to `.mp4` or `.m4a`
- input/output self-overwrite rejection
- 30-minute default CLI timeout; library timeout bounded to 2 hours
- profiles: H.264/AAC 720p, H.264/AAC 1080p, audio-only AAC
- capped FFmpeg log capture
- JUnit command/validation tests
- CI-generated media fixture and actual FFmpeg transcode smoke test
- container image with FFmpeg and non-root execution

## Build and test

```bash
mvn clean verify
```

Local FFmpeg is required for actual transcoding:

```bash
java -jar target/sky-video-transcoder-0.1.0.jar input.mov output.mp4 MP4_720P
```

Health-only smoke signal:

```bash
java -jar target/sky-video-transcoder-0.1.0.jar --health
```

## Boundaries

This is not a cloud transcoding service, media CDN, streaming packager, DRM platform, upload service, job scheduler, GPU transcoder, malware scanner, or verified production deployment. It does not sandbox FFmpeg beyond process/container isolation, inspect media authenticity, manage storage credentials, enforce tenant quotas, generate HLS/DASH manifests, provide distributed queues, or guarantee codec availability outside the documented container.

Production use would require durable job state, isolated untrusted-media execution, object-storage adapters, resource quotas, observability, retries, cancellation, authentication/RBAC, media validation, and deployment verification.

## License

See `LICENSE`.
