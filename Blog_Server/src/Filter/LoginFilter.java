package Filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(urlPatterns= {"*.jsp","*.s"})
public class LoginFilter implements Filter {

  
	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest servletRequest=(HttpServletRequest)request;
		/*
		 * ʵ���ų�Ȩ�޷��ʿ��Ƶ���Դ
		 */
		//��ȡ��ǰ���ʿ�����
		String path=servletRequest.getServletPath();
		if (path.endsWith("user.s") ||path.endsWith("login.jsp")) {
			
			chain.doFilter(request, response);
			return;

		}
		if (servletRequest.getSession().getAttribute("loginedUser" )!= null) {
			chain.doFilter(request, response);
		}else {
			request.setAttribute("msg", "���ȵ�¼ϵͳ");
			request.getRequestDispatcher("login.jsp").forward(request, response);

		}
		
	}

	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
