package servlet;

import java.io.IOException; 
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Biz.BizException;
import Biz.UserBiz;
import bean.User;

/**
 * Servlet implementation class Lpginservlet
 */
@WebServlet("/login.s")
public class Lpginservlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
   

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//���ղ���
		String username =request.getParameter("username");
		String userpwd=request.getParameter("userpwd");
		
		UserBiz uBiz=new UserBiz();
		User user=null;
		try {
			user = uBiz.login(username, userpwd);
		} catch (BizException e) {
			
			e.printStackTrace();
			request.setAttribute("msg", e.getMessage());
			request.getRequestDispatcher("login.jsp").forward(request, response);
			return;
		}
		
		if (user == null ) {
			request.setAttribute("msg", "�û����������");
			//ʧ��
			request.getRequestDispatcher("login.jsp").forward(request, response);
		}else {
			//���û���Ϣ���浽�ػ���
			request.getSession().setAttribute("loginedUser", user);
			//�ɹ�
			response.sendRedirect("index.jsp");
		}
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
