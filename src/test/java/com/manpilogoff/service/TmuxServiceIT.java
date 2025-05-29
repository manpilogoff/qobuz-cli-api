package com.manpilogoff.service;

import com.manpilogoff.dto.TrackInfo;
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
            [ ] 1. Lose Yourself by Eminem
            [ ] 2. Without Me by Eminem
            > [ ] 3. Houdini by Eminem
            [ ] 4. The Real Slim Shady by Eminem
            [ ] 5. Stan by Eminem
            [ ] 6. Mockingbird by Eminem
            ┌── preview ──────────────────────────┐
            │ Released on:                        │
            │ 2005-12-06                          │
            │                                     │
            │ ID: 3972271                         │
            └─────────────────────────────────────┘
            """;

        List<TrackInfo> parsed = TmuxService.parseString(fakeOutput);
        assertEquals(6, parsed.size());

        // Проверяем отдельные поля
        assertEquals(new TrackInfo("Lose Yourself", "Eminem", "3972271"), parsed.get(0));
        assertEquals(new TrackInfo("Houdini", "Eminem", "3972271"), parsed.get(2));
        assertEquals("Mockingbird", parsed.get(5).trackTitle());
        assertEquals("Eminem", parsed.get(3).artistName());
        assertEquals("3972271", parsed.get(4).qobuzId());
    }
}
