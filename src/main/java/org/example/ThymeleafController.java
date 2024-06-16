package org.example;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet(value = "/time")
public class ThymeleafController extends HttpServlet {
    private TemplateConfig templateConfig = new TemplateConfig();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        Context context = new Context();
        String timezone = getTimezone(req);
        ZoneId zoneId = getZoneId(resp, timezone, context);
        if (zoneId == null) return;

        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'XXX");
        String formattedDateTime = zonedDateTime.format(formatter);

        context.setVariable("time", formattedDateTime);
        resp.addCookie(new Cookie("lastTimezone", timezone));
        try {
            templateConfig.process("final-time.html", context, resp);
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    private static String getTimezone(HttpServletRequest req) {
        String timezone = req.getParameter("timezone");
        if (req.getParameter("timezone") == null) {
            Cookie[] cookies = req.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("lastTimezone")) {
                    timezone = cookie.getValue();
                }
            }
        }
        return timezone;
    }

    private ZoneId getZoneId(HttpServletResponse resp, String timezone, Context context) {
        ZoneId zoneId;
        if (timezone != null && !timezone.trim().isEmpty()) {
            timezone = timezone.replace(" ", "+");
            try {
                zoneId = ZoneId.of(timezone);
            } catch (DateTimeException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("text/html; charset=utf-8");
                context.setVariable("mistake", timezone);
                try {
                    templateConfig.process("wrong-query", context, resp);
                } catch (IOException ex) {
                    e.getStackTrace();
                }
                return null;
            }
        } else {
            zoneId = ZoneId.of("UTC");
        }
        return zoneId;
    }
}
