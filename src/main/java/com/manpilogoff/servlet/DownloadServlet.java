package com.manpilogoff.servlet;

import com.manpilogoff.dto.DownloadResponse;
import com.manpilogoff.service.TmuxService;
import com.manpilogoff.util.JsonUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class DownloadServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(DownloadServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        try {
            String numStr = req.getParameter("number");

            log.info("Raw download number received: {}", numStr );

            int trackNumber = Integer.parseInt(numStr);
            String selectedTrack = TmuxService.download(trackNumber);
            writeResponse(resp, 200, JsonUtil.toJson(new DownloadResponse("success", selectedTrack)));
        } catch (Exception e) {
            log.error("Download processing failed", e);
        }
    }

    private void writeResponse(HttpServletResponse resp, int status, String json) throws IOException {
        resp.setContentType("application/json; charset=utf-8");
        resp.setStatus(status);
        resp.getWriter().write(json);
    }
}
