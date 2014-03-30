package dp.slideviewer.servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class View extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getSession(true);
        String fileName = request.getRequestURI().substring(request.getContextPath().length());
        request.setAttribute("ndpiFileName", request.getServletContext().getInitParameter("slideDir") + fileName);
        if(fileName.endsWith(".ndpi")) {
            request.setAttribute("maxZoom", new Integer(9));
        }
        else {
            request.setAttribute("maxZoom", new Integer(8));
        }
        if (request.getParameter("lat") != null && request.getParameter("lng") != null && request.getParameter("zoom") != null) {
            request.setAttribute("lat", new String(request.getParameter("lat")));
            request.setAttribute("lng", new String(request.getParameter("lng")));
            request.setAttribute("zoom", new String(request.getParameter("zoom")));
        }
        request.getRequestDispatcher("/view.jsp").forward(request, response);
        return;
    }
    
}
