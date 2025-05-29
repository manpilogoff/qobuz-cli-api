package com.manpilogoff.service;

import com.manpilogoff.dto.TrackInfo; // Добавить импорт
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
     * Псевдоним для runTmuxCommand.
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
            return "";
        }
    }

    /**
     * Удаляет текущую tmux-сессию.
     */
    public static void cleanup() {
        logger.info("Очистка tmux-сессии");
        run("tmux kill-session -t " + TMUX_SESSION);
    }

    /**
     * Запускает новую tmux-сессию в фоне.
     */
    public static void startSession() {
        logger.info("Запуск новой tmux-сессии");
        run("tmux new-session -d -s " + TMUX_SESSION);
    }

    /**
     * Выполняет команду поиска треков по запросу.
     */
    public static void sendSearchCommand(String query) {
        logger.info("== Search Request: '{}' ==", query);

        try {
            cleanup();
        } catch (Exception e) {
            logger.error("Ошибка при очистке tmux-сессии", e);
            return;
        }

        try {
            startSession();
        } catch (Exception e) {
            logger.error("Ошибка запуска новой tmux-сессии", e);
            return;
        }

        try {
            String cmd = "source " + VENV_PYTHON + " && rip -ndb search qobuz track " + query;
            logger.info("Выполнение команды поиска");
            runTmuxCommand(cmd, true, 3000);
        } catch (Exception e) {
            logger.error("Ошибка при выполнении команды поиска", e);
        }
    }

    /**
     * Получает список треков из текущего вывода tmux.
     */
    public static List<TrackInfo> parseTracks() { // CHANGED
        return parseString(getTmuxOutput());
    }

    /**
     * Парсит вывод tmux, вытаскивая название трека, исполнителя и ID.
     * Изменено: теперь возвращает List<TrackInfo>, а не List<String>
     */
    public static List<TrackInfo> parseString(String output) { // CHANGED
        List<TrackInfo> tracks = new ArrayList<>();

        // Паттерн для строки вида: [ ] 3. INFINITY VOLUME TWO by lxst cxntury
        Pattern trackPattern = Pattern.compile("^(?:>\\s*)?\\[\\s*]\\s*\\d+\\.\\s*(.*?) by (.+)$");

        // Паттерн для ID: ID: 123456789
        Pattern idPattern = Pattern.compile("ID:\\s*(\\d+)");

        // Собираем все ID (если их несколько, присваиваем по индексу)
        List<String> ids = new ArrayList<>();

        Matcher idMatcher = idPattern.matcher(output);
        while (idMatcher.find()) {
            ids.add(idMatcher.group(1));
        }

        int trackIndex = 0;
        for (String line : output.split("\\n")) {
            Matcher m = trackPattern.matcher(line.trim());
            if (m.find()) {
                String title = m.group(1).trim();
                String artist = m.group(2).trim();
                String id = ids.size() == 1 ? ids.get(0) : (ids.size() > trackIndex ? ids.get(trackIndex) : null);
                tracks.add(new TrackInfo(title, artist, id));
                trackIndex++;
            }
        }
        return tracks;
    }

    /**
     * Прокручивает вниз N раз и собирает треки на каждом шаге.
     */
    public static List<TrackInfo> scrollAndCapture(int count) throws InterruptedException { // CHANGED
        logger.info("Прокрутка интерфейса на {} шагов", count);
        List<TrackInfo> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            runTmuxCommand("Down", false, 10);
            result.addAll(parseString(getTmuxOutput()));
        }
        return result;
    }

    /**
     * Возвращает текущий текст из tmux-панели.
     */
    public static String getTmuxOutput() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {}
        return run("tmux capture-pane -t " + TMUX_SESSION + " -p");
    }

    /**
     * Наводит курсор (">") на трек по номеру.
     */
    public static void moveCursorToTrack(int trackNumber) {
        logger.info("Навигация к треку №{}", trackNumber);
        for (int i = 0; i < trackNumber - 1; i++) {
            runTmuxCommand("Down", false, 50);
        }
    }

    /**
     * Выполняет выбор текущего трека (эмуляция Enter).
     */
    public static void selectTrack() {
        logger.info("Выбор текущего трека");
        runTmuxCommand("", true, 10);
    }

    /**
     * Высокоуровневая операция поиска (cleanup -> start -> search -> scroll -> up)
     * */
    public static List<TrackInfo> search(String param) { // CHANGED
        logger.info("Performing full search for param: {}", param);
        cleanup();
        startSession();
        sendSearchCommand(param);

        List<TrackInfo> allTracks = new ArrayList<>(parseTracks());
        try {
            allTracks.addAll(scrollAndCapture(29));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < 29; i++) {
            sendCommand("Up", false, 10);
        }

        // Ограничиваем до 27 (как и было)
        return allTracks.subList(0, Math.min(27, allTracks.size()));
    }

    /**
     * Высокоуровневая операция скачивания трека по номеру
     * */
    public static TrackInfo download(int trackNumber) { // CHANGED
        logger.info("Performing download for track #{}", trackNumber);
        List<TrackInfo> currentTracks;

        try {
            currentTracks = scrollAndCapture(29);
            for (int i = 0; i < 29; i++) {
                sendCommand("Up", false, 20);
            }
            currentTracks = currentTracks.subList(0, Math.min(27, currentTracks.size()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (trackNumber < 1 || trackNumber > currentTracks.size()) {
            throw new IllegalArgumentException("invalid track number");
        }

        TrackInfo selectedTrack = currentTracks.get(trackNumber - 1);
        moveCursorToTrack(trackNumber);
        selectTrack();
        return selectedTrack;
    }
}
