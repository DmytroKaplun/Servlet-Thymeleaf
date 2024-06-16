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
    private TemplateEngine engine;

    @Override
    public void init() {
        engine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    public void process(String templateName, Context context, jakarta.servlet.http.HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        engine.process(templateName, context, response.getWriter());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        System.out.println("Received request at /time");
        Context context = new Context();
        String timezone = getTimezone(req);
        System.out.println("Received timezone: " + timezone);
        ZoneId zoneId = getZoneId(resp, timezone, context);
        if (zoneId == null) return;

        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'XXX");
        String formattedDateTime = zonedDateTime.format(formatter);

        context.setVariable("time", formattedDateTime);
        resp.addCookie(new Cookie("lastTimezone", timezone));
        try {
            process("final-time.html", context, resp);
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    private static String getTimezone(HttpServletRequest req) {
        String timezone = req.getParameter("timezone");
        System.out.println(timezone + "----------------------");
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
                    process("wrong-query", context, resp);
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
