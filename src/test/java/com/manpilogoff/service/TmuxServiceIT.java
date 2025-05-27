package com.manpilogoff.service;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("integration")
public class TmuxServiceIT {

    @BeforeEach

    void setUp() {
        TmuxService.cleanup();
        TmuxService.startSession();
    }

    @AfterEach
    void tearDown() {
        TmuxService.cleanup();
    }

    @Test
    @Order(1)
    void testSessionLifecycle() {
        assertTrue(TmuxService.hasSession(), "Session should be active after start");
        TmuxService.cleanup();
        assertFalse(TmuxService.hasSession(), "Session should be gone after cleanup");
    }

    @Test
    @Order(2)
    void testRunCommandAndOutput() {
        TmuxService.sendCommand("echo HelloFromTmux", true, 500);
        String output = TmuxService.getTmuxOutput();
        assertTrue(output.contains("HelloFromTmux"), "Output should contain sent string");
    }

    @Test
    @Order(3)
    void testParseTracksOnFakeInput() {
        String fakeOutput = """
            [ ] 1. Track One
            [ ] 2. Track Two
            > [ ] 3. Track Three
            """;
        List<String> parsed = TmuxService.parseString(fakeOutput);
        assertEquals(3, parsed.size());
        assertEquals("Track One", parsed.get(0));
        assertEquals("Track Three", parsed.get(2));

    }
}