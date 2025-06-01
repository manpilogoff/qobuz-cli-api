package com.manpilogoff.service;

import com.manpilogoff.dto.TrackData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TmuxService {
    private static final Logger log = LoggerFactory.getLogger(TmuxService.class);

    private static final String PYTHON_VENV = "/opt/venvs/venv_1/bin/activate";
    private static final String TMUX_SESSION = "rip_session";

    public  String executeBash(String bashCommand) {
        try {
            Process process = new ProcessBuilder("/bin/bash", "-c", bashCommand).start();
            return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Ошибка выполнения команды: {}", bashCommand, e);
            return null;
        }
    }

    public  String captureTmuxOutput()  {
        executeBash("tmux send-keys -t " + TMUX_SESSION + " Down ");
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            log.error("Interrupted exception at captureTmuxOutput(): ", e.getCause());
        }
        return executeBash("tmux capture-pane -t " + TMUX_SESSION + " -p");



    }

    public void tmuxSearch(String query, String modifier) {
        startSession();
        executeBash("tmux resize-pane -t " + TMUX_SESSION + ":0.0 -y " + 51);

        if (modifier == null || modifier.isEmpty()) {
            modifier = "track";
        }

        // Посылаем команду внутрь tmux-сессии
        String command = "source " + PYTHON_VENV + " && rip -ndb search qobuz " + modifier + " " + query;
        sendTmuxKey("\"" + command + "\"");
        sendTmuxKey("Enter");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while sleeping after tmuxSearch");
        }
    }

    public TrackData tmuxDownload(String qMod, String id) {
        startSession();
        String command = "source " + PYTHON_VENV + " && rip -v -ndb id qobuz track " + id;
        sendTmuxKey("\"" + command + "\"");
        sendTmuxKey("Enter");
        // После загрузки можно прочитать captureTmuxOutput и распарсить результат
        return null;
    }
    public  void sendTmuxKey(String cmd) {
        executeBash("tmux send-keys -t " + TMUX_SESSION + " " + cmd);
    }

    public  void startSession() {
        if (hasSession()) cleanSession();
        executeBash("tmux new-session -d -s " + TMUX_SESSION + " -x 200 -y 50");
    }

    public  void cleanSession() {
        executeBash("tmux kill-session -t " + TMUX_SESSION);
    }

    public  boolean hasSession() {
        String result = executeBash("tmux has-session -t " + TMUX_SESSION);
        // Если результат пустой и exit code 0 — сессия существует
        return result != null && result.isEmpty();
    }
}
