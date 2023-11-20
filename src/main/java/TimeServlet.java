import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    private TemplateEngine engine;

    @Override
    public void init() {
        engine = new TemplateEngine();

        JakartaServletWebApplication jakartaServletWebApplication =
                JakartaServletWebApplication.buildApplication(this.getServletContext());

        try (WebApplicationTemplateResolver resolver = new WebApplicationTemplateResolver(jakartaServletWebApplication)) {
            resolver.setPrefix("/WEB-INF/template/");
            resolver.setSuffix(".html");
            resolver.setTemplateMode("HTML5");
            resolver.setOrder(engine.getTemplateResolvers().size());
            resolver.setCacheable(false);
            engine.addTemplateResolver(resolver);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html");

        String timezone = getTimezoneFromRequestOrCookie(request, response);
        String validTimezone = parserTimezone(timezone);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("time", validTimezone);

        Context context = new Context(request.getLocale(), Map.of("time", params));

        try (PrintWriter writer = response.getWriter()) {
            engine.process("timePage", context, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getTimezoneFromRequestOrCookie(HttpServletRequest request, HttpServletResponse response) {
        String timezoneParam = request.getParameter("timezone");

        if (timezoneParam != null) {
            timezoneParam = timezoneParam.replace("UTC+", "Etc/GMT-").replace("UTC-", "Etc/GMT+");
            saveTimezoneToCookie(response, timezoneParam);
            return timezoneParam;
        }

        return getTimezoneFromCookie(request);
    }

    private void saveTimezoneToCookie(HttpServletResponse response, String timezoneParam) {
        response.addCookie(new Cookie("lastTimezone", timezoneParam));
    }

    private String getTimezoneFromCookie(HttpServletRequest request) {
        String timezoneParam = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("lastTimezone".equals(cookie.getName())) {
                    timezoneParam = cookie.getValue();
                    break;
                }
            }
        }

        return (timezoneParam != null) ? timezoneParam : "Etc/GMT";
    }

    private String parserTimezone(String timezone) {
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime currentTime = ZonedDateTime.now(zoneId);
        return currentTime.format(FORMATTER).replace("GMT", "UTC");
    }
}