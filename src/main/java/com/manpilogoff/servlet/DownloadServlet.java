package com.manpilogoff.servlet;

import com.manpilogoff.dto.DownloadResponse;
import com.manpilogoff.dto.TrackData;
import com.manpilogoff.service.AudioService;
import com.manpilogoff.service.TmuxService;
import com.manpilogoff.util.JsonUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.PrintWriter;

import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;

public class DownloadServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(DownloadServlet.class);
    private final AudioService audioService;

    public DownloadServlet(AudioService audioService) {
        this.audioService = audioService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String numParam = req.getParameter("number");

        try {
            log.info("Raw download number received: {}", numParam );

            TrackData obtainedTrack = audioService.download(numParam);
            writeResponse(resp, SC_OK, obtainedTrack);
        } catch (Exception e) {
            log.error("Download processing failed:  {}", e.getMessage());
            writeResponse(resp, SC_INTERNAL_SERVER_ERROR, null);
        }
    }

    private void writeResponse(HttpServletResponse resp, int status, TrackData track)   {
        String jsonStatus;

        if(status == 200) jsonStatus = "success";
        else jsonStatus = "error";

        try (PrintWriter writer = resp.getWriter()){
            resp.setContentType("application/json; charset=utf-8");
            resp.setStatus(status);
            writer.write(JsonUtil.toJson(new DownloadResponse(jsonStatus,track)));
        } catch (IOException e) {
            log.error("Exception during response writing: {}", e.getMessage());
        }
    }
}
