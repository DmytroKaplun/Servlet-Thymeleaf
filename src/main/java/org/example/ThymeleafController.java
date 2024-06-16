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
import java.util.Arrays;

@WebServlet(value = "/time")
public class ThymeleafController extends HttpServlet {
    private TemplateConfig templateConfig = new TemplateConfig();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        Context context = new Context();
        String timezone = getTimezone(req);
        ZoneId zoneId = ZoneId.of(timezone);

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
            if (req.getCookies() == null) {
                return "UTC";
            }
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("lastTimezone")) {
                    timezone = cookie.getValue();
                }
            }
        }
        return timezone;
    }
}
