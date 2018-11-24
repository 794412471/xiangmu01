package Biz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.yc.dao.ly.DBHelper;

import bean.User;

public class UserBiz {
	public User login(String username,String userpwd) throws BizException{
		if(username ==null || username.trim().isEmpty()) {
			throw new BizException("«ÎÃÓ–¥”√ªß√˚");
		}
		if (userpwd == null || userpwd.trim().isEmpty()) {
			throw new BizException("«ÎÃÓ–¥√‹¬Î");
			
		}
		//DBHelper dbHelper=new DBHelper();
		//List<Object> list= new ArrayList<Object>();
		//list.add(username);
		//list.add(userpwd);
		String sql="select * from user where account =? and pwd = ? ";
		//Map<String, String> user=dbHelper.findMap(sql, list);
		return DBHelper.unique(sql, User.class,username,userpwd);
	}

	public List<User> findfAll() {
		return DBHelper.select("select * from user", User.class);
	}

	public void add(User user,String repwd) throws BizException{
		String sql="insert into user(name,account,tel,pwd) values(?,?,?,?)";
		DBHelper.insert(sql, user.getName(),user.getAccount(),user.getTel(),
				user.getPwd());
		
	}

	public Object find(User user) {
		String sql="select * from user where 1=1 ";
		ArrayList< Object> params =new ArrayList<Object>();
		
		if (user.getAccount() !=null && user.getAccount().trim().isEmpty() == false) {
			sql+=" and account like ? ";
			params.add("%"+user.getAccount()+"%");
		}
		if (user.getName() != null && user.getName().trim().isEmpty() == false) {
			sql +=" and name like ? ";
			params.add("%"+user.getName()+"%");
		}
		if (user.getTel() != null && user.getTel().trim().isEmpty() == false) {
			sql +=" and tel like ? ";
			params.add("%"+user.getTel()+"%");
		}
		
		return DBHelper.select(sql, params);
	}

	public User findById(String id) {
		return DBHelper.unique("select * from user where id = ? ", User.class,id);
	}

	public void save(User user) throws BizException {
		if (user.getAccount() == null || user.getAccount().trim().isEmpty()) {
			throw new BizException("«ÎÃÓ–¥”√ªß√˚");
		}
		if (user.getName() == null || user.getName().trim().isEmpty()) {
			throw new BizException("«ÎÃÓ–¥–’√˚");
		}
		
		DBHelper.update("update user set name = ? ,account = ? ,tel = ? where id = ? ", user.getName(),
				user.getAccount(),user.getTel(),user.getId());
	}
}
