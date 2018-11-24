package servlet;

import java.io.IOException;

import javax.management.Query;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.filters.AddDefaultCharsetFilter;

import com.alibaba.fastjson.JSON;
import com.yc.dao.ly.BeanUtils;

import Biz.BizException;
import Biz.UserBiz;
import bean.User;

/**
 * Servlet implementation class UserServlet
 */
@WebServlet("/user.s")
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	UserBiz ubiz=new UserBiz();
       	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		String op=request.getParameter("op");
		if ("login".equals(op)) {
			login(request,response);
		}else if ("query".equals(op)) {
			query(request,response);
		}else if("add".equals(op)){
			add(request,response);
		}else if("find".equals(op)){
			find(request,response);
		}else if("save".equals(op)){
			save(request,response);
		}
	}

	
	private void save(HttpServletRequest request, HttpServletResponse response)
			throws ServletException,IOException{
		response.setCharacterEncoding("utf-8");
		User user=BeanUtils.asBean(request, User.class);
		String msg;
		try {
			ubiz.save(user);
			msg="�û���Ϣ����ɹ�";
			//query(request,response);
		} catch (Exception e) {
			e.printStackTrace();	
			msg=e.getMessage();
		}
		response.getWriter().append(msg);
		
	}


	private void find(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		response.setCharacterEncoding("utf-8");
		String id=request.getParameter("id");
		User user=ubiz.findById(id);
		//��user���ظ�ҳ��
		String userString =JSON.toJSONString(user);
		
		response.getWriter().append(userString);
	}


	private void add(HttpServletRequest request, HttpServletResponse response)
			throws  ServletException,IOException{
		
		//����ҳ�洫�صĲ���
		//���������ص�user������
		User user=BeanUtils.asBean(request, User.class);
		//System.out.print(user.getAccount() +"====="+user.getName());
		//����UserBiz.add�������û���ӵ�����
		try {
			String repwd=request.getParameter("repwd");
			ubiz.add(user,repwd);
			//��ʽһ
			//request.getRequestDispatcher("user.s?op=query").forward(request, response);
			//��ʽer
			//query(request, response);
		} catch (BizException e) {
			e.printStackTrace();
			request.setAttribute("msg", e.getMessage());
		}finally {
			query(request,response);
		}
	}
	private void query(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		User user=BeanUtils.asBean(request, User.class);
		request.setAttribute("userList", ubiz.find(user));
		request.getRequestDispatcher("manage-user.jsp").forward(request,response);

	}


	private void login(HttpServletRequest request, HttpServletResponse response)
			throws ServletException,IOException{
		//��������
		String username=request.getParameter("username");
		String userpwd=request.getParameter("userpwd");
			
		User user=null;
		try {
			user = ubiz.login(username, userpwd);
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


	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doGet(request, response);
	}

}
