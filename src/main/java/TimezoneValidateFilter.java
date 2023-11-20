import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;

@WebFilter(value = "/time")
public class TimezoneValidateFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String timezone = req.getParameter("timezone");

        if (timezone == null || isValidTimezone(timezone)) {
            chain.doFilter(req, res);
        } else {
            res.setStatus(400);
            res.setContentType("text/html; charset=utf-8");

            try (PrintWriter writer = res.getWriter()) {
                writer.write("Invalid timezone");
            }
        }
    }

    private boolean isValidTimezone(String timeZone) {
        try {
            ZoneId.of(timeZone.replace(" ", "+"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }