package com.manpilogoff;

import com.manpilogoff.service.*;
import com.manpilogoff.servlet.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        log.info("Starting server...");
        Server server = new Server(5000);

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.setContextPath("/");

        handler.addServlet(SearchServlet.class, "/search");
        handler.addServlet(DownloadServlet.class, "/download");

        server.setHandler(handler);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down and cleaning up tmux session...");
            TmuxService.cleanup();

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