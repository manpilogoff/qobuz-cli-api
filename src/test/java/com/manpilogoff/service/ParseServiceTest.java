package com.manpilogoff.service;

import com.manpilogoff.dto.TrackData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ParseServiceTest {

    private final ParseService parser = new ParseService();

    @ParameterizedTest(name = "[{index}] input: {0}")
    @MethodSource("validCases")
    void parseValidInput_ReturnsExpectedTrack(String input, TrackData expected) {
        assertEquals(expected, parser.parseActiveTrackAndId(input));
    }

    private static Stream<Arguments> validCases() {
        return Stream.of(
                Arguments.of("> [ ] 1. Some Track by SomeArtist\nID: 42", new TrackData("Some Track", "SomeArtist", "42")),
                Arguments.of("> [ ] 9. Название трека by Артист\nID: 100", new TrackData("Название трека", "Артист", "100")),
                Arguments.of("> [ ] 5. Hello-World! by Art.ist-123\nID: 999", new TrackData("Hello-World!", "Art.ist-123", "999"))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Random gibberish",
            "> [ ] 3. Just a title no ID",
            "ID: only\nwithout title"
    })
    void parseInvalidInput_ReturnsNullFields(String input) {
        TrackData result = parser.parseActiveTrackAndId(input);
        assertNull(result.trackTitle());
        assertNull(result.artistName());
        assertNull(result.id());
    }

    @Test
    void parseWithoutPreviewBlock_returnsNullId() {
        // Arrange
        String output = "SPACE - select, ENTER - download, ESC - exit\n" +
                "> [ ] 1. Active Track by Artist\n" +
                "  [ ] 2. Other Track by Other Artist";

        // Act
        TrackData result = parser.parseActiveTrackAndId(output);

        // Assert
        assertAll(
                () -> assertEquals("Active Track", result.trackTitle()),
                () -> assertEquals("Artist", result.artistName()),
                () -> assertNull(result.id())
        );
    }
}
