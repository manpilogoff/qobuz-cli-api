package com.manpilogoff;

import com.manpilogoff.service.*;
import com.manpilogoff.servlet.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        log.info("Starting server...");
        Server server = new Server(5000);

        ParseService parseService = new ParseService();
        TmuxService tmuxService = new TmuxService();
        AudioService audioService = new AudioService(tmuxService, parseService);

        DownloadServlet downloadServlet = new DownloadServlet(audioService);
        SearchServlet searchServlet = new SearchServlet(audioService);

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.setContextPath("/");

        handler.addServlet(new ServletHolder(searchServlet), "/search");
        handler.addServlet(new ServletHolder(downloadServlet), "/download");

        server.setHandler(handler);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down and cleaning up tmux session...");
           tmuxService.cleanSession();

        }));

        try {
            log.info("  (+)Server started on port:5000");
            server.start();
            server.join();
        } catch(Exception e) {
            log.error("  (-)Failed to start server", e);
        }
    }
}