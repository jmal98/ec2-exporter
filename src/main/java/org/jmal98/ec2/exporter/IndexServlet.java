package org.jmal98.ec2.exporter;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IndexServlet extends HttpServlet {

	private static final long serialVersionUID = 313995012804498559L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try (
				Writer writer = resp.getWriter();
			){
			
			if (req.getServletPath().equals("/")) {
				writer.write("<html>EC2 Exporter</html>");
				writer.flush();
			} else {
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
		} finally {}
	}

}