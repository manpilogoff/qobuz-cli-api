package com.manpilogoff.servlet;

import com.manpilogoff.dto.DownloadResponse;
import com.manpilogoff.dto.SearchResponse;
import com.manpilogoff.dto.TrackData;
import com.manpilogoff.service.AudioService;
import com.manpilogoff.util.JsonUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SearchServlet extends HttpServlet {
    private final AudioService audioService;
    private static final Logger log = LoggerFactory.getLogger(SearchServlet.class);

    @Override
    public void init() {

    }
    public SearchServlet(AudioService audioService) {
        this.audioService = audioService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        try (PrintWriter respWriter = resp.getWriter()){

            String param = req.getParameter("param");
            String decodedParam = URLDecoder.decode(param, StandardCharsets.UTF_8);

            log.info("Search request received for param: {}", decodedParam);

            List<TrackData> result = audioService.search(decodedParam);
            resp.setContentType("application/json; charset=utf-8");
            respWriter.write(JsonUtil.toJson(new SearchResponse("success", param,result,result.size())));
        } catch (Exception e) {
            log.error("Search process failed with exception", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        }
    }
}
