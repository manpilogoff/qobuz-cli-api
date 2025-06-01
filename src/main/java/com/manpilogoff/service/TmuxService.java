package com.manpilogoff.service;

import com.manpilogoff.dto.TrackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TmuxService {
    private static final Logger logger = LoggerFactory.getLogger(TmuxService.class);
    private static final String TMUX_SESSION = "rip_session";
    private static final String VENV_PYTHON = "/opt/venvs/venv_1/bin/activate";

    /**
     * Проверяет, активна ли tmux-сессия.
     */
    public static boolean hasSession() {
        try {
            Process p = new ProcessBuilder("tmux", "has-session", "-t", TMUX_SESSION).start();
            int exitCode = p.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Отправляет команду в tmux с возможностью нажать Enter и задержкой.
     */
    public static void runTmuxCommand(String cmd, boolean enter, int delayMs) {
        run("tmux send-keys -t " + TMUX_SESSION + " \"" + cmd + "\"");
        if (enter) run("tmux send-keys -t " + TMUX_SESSION + " Enter");
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ignored) {}
    }

    /**
     * Псевдоним для runTmuxCommand. Используется для простых tmux-команд.
     */
    public static void sendCommand(String cmd, boolean enter, int delayMs) {
        runTmuxCommand(cmd, enter, delayMs);
    }

    /**
     * Выполняет bash-команду и возвращает stdout как строку.
     */
    public static String run(String cmd) {
        try {
            Process p = new ProcessBuilder("/bin/bash", "-c", cmd).start();
            return new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Ошибка выполнения команды: {}", cmd, e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Удаляет текущую tmux-сессию.
     */
    public static void cleanup() {
        run("tmux kill-session -t " + TMUX_SESSION);
        logger.info("tmux session killed");
    }

    /**
     * Запускает новую tmux-сессию в фоне.
     */
    public static void startSession() {
        cleanup();
        run("tmux new-session -d -s " + TMUX_SESSION);
        logger.info("Tmux session started");
    }

    /**
     * Выполняет команду поиска треков по запросу.
     */
    public static void sendSearchCommand(String query) {
        startSession();
        String cmd = "source " + VENV_PYTHON + " && rip -ndb search qobuz track " + query;
        runTmuxCommand(cmd, true, 3000);
    }

    /**
     * Возвращает текущий текст из tmux-панели.
     */
    public static String getTmuxOutput() {
        return run("tmux capture-pane -t " + TMUX_SESSION + " -p");
    }

    /**
     * Парсит только активный трек и ID из вывода tmux.
     */
    public static TrackInfo parseActiveTrackAndId(String output) {
        Pattern trackPattern = Pattern.compile("^>\\s*\\[\\s*]\\s*\\d+\\.\\s*(.*?) by (.+)$", Pattern.MULTILINE);
        Pattern idPattern = Pattern.compile("ID:\\s*(\\d+)");
        String title = null, artist = null, id = null;

        Matcher m = trackPattern.matcher(output);
        if (m.find()) {
            title = m.group(1).trim();
            artist = m.group(2).trim();
        }
        Matcher idMatcher = idPattern.matcher(output);
        if (idMatcher.find()) {
            id = idMatcher.group(1);
        }
        return new TrackInfo(title, artist, id);
    }

    /**
     * Прокручивает вниз N раз и собирает треки на каждом шаге.
     */
    public static List<TrackInfo> scrollAndCapture(int count)  {
        List<TrackInfo> tracks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            TrackInfo track = parseActiveTrackAndId(getTmuxOutput());
            tracks.add(track);
            runTmuxCommand("Down", false, 150);
        }
        return tracks;
    }

    /**
     * Высокоуровневая операция поиска (start -> search -> scroll).
     */
    public static List<TrackInfo> search(String param) {
        sendSearchCommand(param);
        return scrollAndCapture(20);
    }

    /**
     * Высокоуровневая операция скачивания трека по номеру.
     */
    public static TrackInfo download(int trackNumber) {
        List<TrackInfo> currentTracks = scrollAndCapture(20);
        currentTracks = currentTracks.subList(0, Math.min(20, currentTracks.size()));
        if (trackNumber < 1 || trackNumber > currentTracks.size()) {
            logger.error("Invalid track number: {}", trackNumber);
        }
        TrackInfo selectedTrack = currentTracks.get(trackNumber - 1);
        for (int i = 0; i < trackNumber - 1; i++) {
            runTmuxCommand("Down", false, 50);
        }
        runTmuxCommand("", true, 10);
        return selectedTrack;
    }
}
