package se.citerus.dddsample.interfaces.handling.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import se.citerus.dddsample.application.ApplicationEvents;
import se.citerus.dddsample.domain.model.cargo.TrackingId;
import se.citerus.dddsample.domain.model.handling.HandlingEvent;
import se.citerus.dddsample.domain.model.location.UnLocode;
import se.citerus.dddsample.domain.model.voyage.VoyageNumber;
import se.citerus.dddsample.interfaces.handling.HandlingEventRegistrationAttempt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static se.citerus.dddsample.interfaces.handling.HandlingReportParser.*;

/**
 * Periodically scans a certain directory for files and attempts
 * to parse handling event registrations from the contents.
 * <p/>
 * Files that fail to parse are moved into a separate directory,
 * successful files are deleted.
 */
@Slf4j
@RequiredArgsConstructor
public class UploadDirectoryScanner implements InitializingBean {

    private final Path uploadDirectory;
    private final Path parseFailureDirectory;
    private final ApplicationEvents applicationEvents;

    @Scheduled(fixedDelay = 5,timeUnit = TimeUnit.SECONDS)
    public void scan() {
        File[] files = uploadDirectory.toFile().listFiles();
        if (null == files) {
            return;
        }
        for (File file : files) {
            try {
                parse(file);
                delete(file);
                log.info("Import of {} complete", file.getName());
            } catch (Exception e) {
                log.error("Error parsing uploaded file", e);
                move(file);
            }
        }
    }

    /**
     * Reads an uploaded file into memory and parses it line by line, returning a list of parsed lines.
     * Any unparseable lines will be stored in a new file and saved to the parseFailureDirectory.
     *
     * @param file the file to parse.
     * @throws IOException if reading or writing the file fails.
     */
    private void parse(final File file) throws IOException {
        final List<String> lines = Files.readAllLines(file.toPath());
        final List<String> rejectedLines = new ArrayList<>();
        for (String line : lines) {
            try {
                String[] columns = parseLine(line);
                queueAttempt(columns[0], columns[1], columns[2], columns[3], columns[4]);
            } catch (Exception e) {
                log.error("Rejected line: {}", line, e);
                rejectedLines.add(line);
            }
        }
        if (!rejectedLines.isEmpty()) {
            writeRejectedLinesToFile(toRejectedFilename(file), rejectedLines);
        }
    }

    private String toRejectedFilename(final File file) {
        return file.getName() + ".reject";
    }

    private void writeRejectedLinesToFile(final String filename, final List<String> rejectedLines) throws IOException {
        Files.write(
            parseFailureDirectory.resolve(filename),
            rejectedLines,
            StandardOpenOption.APPEND);
    }

    private String[] parseLine(final String line) {
        final String[] columns = line.split("\\s{2,}");
        if (columns.length == 5) {
            return new String[]{columns[0], columns[1], columns[2], columns[3], columns[4]};
        } else if (columns.length == 4) {
            return new String[]{columns[0], columns[1], "", columns[2], columns[3]};
        } else {
            throw new IllegalArgumentException(String.format("Wrong number of columns on line: %s, must be 4 or 5", line));
        }
    }

    private void queueAttempt(String completionTimeStr, String trackingIdStr, String voyageNumberStr, String unLocodeStr, String eventTypeStr) throws Exception {
        try {
            final Instant date = parseDate(completionTimeStr);
            final TrackingId trackingId = parseTrackingId(trackingIdStr);
            final VoyageNumber voyageNumber = parseVoyageNumber(voyageNumberStr);
            final HandlingEvent.Type eventType = parseEventType(eventTypeStr);
            final UnLocode unLocode = parseUnLocode(unLocodeStr);
            final HandlingEventRegistrationAttempt attempt = new HandlingEventRegistrationAttempt(
                Instant.now(), date, trackingId, voyageNumber, eventType, unLocode);
            applicationEvents.receivedHandlingEventRegistrationAttempt(attempt);
        } catch (IllegalArgumentException e) {
            throw new Exception("Error parsing HandlingReport", e);
        }
    }

    private void delete(final File file) {
        if (!file.delete()) {
            log.error("Could not delete file: {}", file.getName());
        }
    }

    private void move(final File file) {
        final File destination = parseFailureDirectory.resolve(file.getName()).toFile();
        final boolean result = file.renameTo(destination);
        if (!result) {
            log.error("Could not move {} to {}", file.getName(), destination.getAbsolutePath());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (uploadDirectory.equals(parseFailureDirectory)) {
            throw new Exception(String.format("Upload and parse failed directories must not be the same directory: %s", uploadDirectory));
        }
        for (Path dir : Arrays.asList(uploadDirectory, parseFailureDirectory)) {
            if(!Files.exists(dir)){
                Files.createDirectory(dir);
            }
            if (!Files.exists(dir)) {
                throw new IllegalStateException("Failed to create dir: " + dir);
            }
        }
    }
}
