package no.nav.statusplattform.api.v3.controllers;

import java.time.LocalDateTime;

public record TimeLineEntryFake(
        TimeLineAnnotation annotation,
        LocalDateTime startTime,
        LocalDateTime endTime,
        TimeLineEntryMetadata metadata
) {
}
