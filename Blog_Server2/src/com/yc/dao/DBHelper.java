package com.yc.dao;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;


public class DBHelper {
	/* ��ʼ��context���� */
	static Context ctx = null;
	
	//��̬�죬������������
	static{
		try {
			Class.forName(Env.getInstance().getProperty("driverClassName"));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		try {
			ctx = new InitialContext();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ȡ����
	 * @throws Exception 
	 */
	public Connection getConnection(){
		Connection con=null;
		try {
			DataSource ds = BasicDataSourceFactory.createDataSource(Env.getInstance());
			con = ds.getConnection();
			
			//ͨ���������ӳ�ȥȡ
//			DataSource dataSource = (DataSource)ctx.lookup("java:comp/env/jdbc/cludtags");
//			//out.print(hello);
//			//ȡ��һ������
//		   con = dataSource.getConnection();
		   
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return con;
	}


	/**
	 * �رյķ���
	 */
	public void closeAll(Connection con,PreparedStatement pstmt,ResultSet rs,CallableStatement cs){
		if(rs!=null){
			try {
				rs.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

		if(cs!=null){
			try {
				cs.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

		if(pstmt!=null){
			try {
				pstmt.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

		if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}


	private void setValues(PreparedStatement st,List<Object> objs) {
		// �ж��Ƿ��в���
		if (objs == null || objs.size() == 0) {
			return;
		}
		try {
			for (int i = 0,len=objs.size(); i < len; i++) {
				if(objs.get(i)!=null){
					String paramType = objs.get(i).getClass().getName(); // ��ò���������
					if (Integer.class.getName().equals(paramType)) { // �ж��Ƿ���int����
						st.setInt(i + 1, (Integer) objs.get(i));
					} else if (Double.class.getName().equals(paramType)) { // �ж��Ƿ���dobule����
						st.setDouble(i + 1, (Double) objs.get(i));
					} else if (String.class.getName().equals(paramType)) { // �ж��Ƿ���string����
						st.setString(i + 1, (String) objs.get(i));
					} else {
						st.setObject(i + 1, objs.get(i));
					}
				}else{
					st.setObject(i + 1,objs.get(i));
				}

			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * ��ȡ�������ÿ���е�����
	 * @param rs�������
	 * @return
	 */
	private String[] getColumnNames(ResultSet rs){
		String[] colNames=null;
		try {
			ResultSetMetaData md=rs.getMetaData(); //��ȡ�������Ԫ���ݣ�����ӳ�˽��������Ϣ
			colNames=new String[md.getColumnCount()];//����һ������colnames����������е�����
			for(int i=0;i<colNames.length;i++){  //���������浽colname������
				colNames[i]=md.getColumnName(i+1).toLowerCase();
			}
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return colNames;
	}


	/**
	 * ��ɾ��
	 * @param sql��sql��伯�ϣ�������Լӣ�
	 * @param params����ʾ?��Ӧ�Ĳ���ֵ�ļ���
	 * @return int:���ص�ֵ���ɹ�>0��ʧ��<=0
	 */
	public int update(List<String> sql,List<List<Object>> params){
		int result=0;
		Connection con=getConnection();
		PreparedStatement pstmt=null;
		try {
			con.setAutoCommit(false);  //������
			for(int i=0;i<sql.size();i++){
				List<Object> param=params.get(i);
				pstmt=con.prepareStatement(sql.get(i));  //Ԥ�������
				setValues(pstmt,param);    //���ò���
				result=pstmt.executeUpdate();
			}
			con.commit(); //û�д�ִ��
		} catch (SQLException e) {
			try {
				con.rollback();  //����ع�
			} catch (SQLException e1) {
				throw new RuntimeException(e);
			}
			throw new RuntimeException(e);
		}finally{
			try {
				con.setAutoCommit(true);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			closeAll(con,pstmt,null,null);
		}
		return result;
	}

	/**
	 * ��ɾ��������
	 * @param sql��sql��伯�ϣ�������Լӣ�
	 * @param params����ʾ?��Ӧ�Ĳ���ֵ�ļ���
	 * @return int:���ص�ֵ���ɹ�>0��ʧ��<=0
	 */
	public boolean updates(List<String> sqls,List<List<Object>> params){
		Connection con=getConnection();
		PreparedStatement pstmt=null;
		//java�����Զ��ύ�����������Ǳ����ȹر��Զ��ύ
		try {
			con.setAutoCommit(false);
			//ѭ��ִ��sql���
			for(int i=0;i<sqls.size();i++){
				pstmt=con.prepareStatement(sqls.get(i)); //ȡ����i��sql���
				setValues(pstmt, params.get(i)); //ȡ����i��sql����Ӧ�Ĳ����б�
				pstmt.addBatch();
			}
			//����������ִ�к�û�г������ύ
			pstmt.executeBatch();
			con.commit();
		} catch (SQLException e) {
			//���ִ�й����г����ˣ���ع�
			try {
				con.rollback();
			} catch (SQLException e1) {
				throw new RuntimeException(e);
			}
			throw new RuntimeException(e);
		} finally{
			try {
				con.setAutoCommit(true);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			this.closeAll(con, pstmt,null,null);
		}

		return true;
	}

	/**
	 * ������ɾ��
	 * @param sql��sql��伯�ϣ�������Լӣ�
	 * @param params����ʾ?��Ӧ�Ĳ���ֵ�ļ���
	 * @return int:���ص�ֵ���ɹ�>0��ʧ��<=0
	 */
	public int update(String sql,List<Object> params){
		int result=0;
		Connection con=getConnection();
		PreparedStatement pstmt=null;	
		try {
			pstmt=con.prepareStatement(sql);  //Ԥ�������
			setValues(pstmt,params);    //���ò���
			result=pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}finally{
			closeAll(con,pstmt,null,null);
		}
		return result;
	}


	/**
	 * �ۺϲ�ѯ
	 * @param sql���ۺϲ�ѯ���
	 * @param params�������б������滻sql�е�?��ռλ����
	 * @return list:�����
	 */

	public List<String> uniqueResult(String sql,List<Object> params){
		List<String> list=new ArrayList<String>();
		Connection con=getConnection();
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try {
			pstmt=con.prepareStatement(sql);  //Ԥ�������
			setValues(pstmt,params);   //���ò���
			rs=pstmt.executeQuery();  //ִ�в�ѯ

			ResultSetMetaData md=rs.getMetaData();  //�������Ԫ���ݣ�����ӳ�˽��������Ϣ
			int count=md.getColumnCount();    //ȡ����������е�����

			if(rs.next()){
				for(int i=1;i<=count;i++){
					list.add(rs.getString(i));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}finally{
			closeAll(con,pstmt,rs,null);
		}
		return list;
	}


	/**
	 * ��ѯ
	 * @param <T> ���ͣ�����Ҫ�õ��ļ����д�Ķ��������
	 * @param sql: ��ѯ��䣬���Ժ���?
	 * @param params: ?����Ӧ�Ĳ���ֵ�ļ���
	 * @param c�� ������������Ӧ�ķ������
	 * @return ���洢�˶���ļ���
	 * @throws SQLException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public <T> List<T> find(String sql,Class<T> c,List<Object> params) {
		List<T> list=new ArrayList<T>(); //Ҫ���صĽ���ļ���
		Connection con=getConnection();
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try {
			pstmt=con.prepareStatement(sql); //Ԥ�������
			setValues(pstmt, params); //����ռλ��
			rs=pstmt.executeQuery();  //ִ�в�ѯ��䣬�õ������

			Method[] ms=c.getMethods(); //ȡ���������ʵ�������з���
			
			String[] colnames=getColumnNames(rs); //��ȡ������������е�����
			T t;
			String mname=null;  //������
			String cname=null;  //����
			String ctypename=null; //������

			while(rs.next()){
				t=(T)c.newInstance(); //�����������ʵ��������    Product t=(Product)c.newInstance();
				for(int i=0;i<colnames.length;i++){//ѭ�������� ,��ʽΪsetXXXX��getXXX
					cname=colnames[i]; //ȡ����������ǰ�����set  setXXX
					cname="set"+cname.substring(0,1).toUpperCase()+cname.substring(1).toLowerCase();
					if(ms!=null&&ms.length>0){
						for(Method m:ms){//ѭ������
							mname=m.getName(); //ȡ��������

							if(cname.equals(mname)&&rs.getObject(colnames[i])!=null){//�жϷ������������Ƿ�һ������ͬ�򼤻����ע������      //ֻҪ"set"+��������.equalsIgnoreCase�������������򼤻��������
								//setXXX(String str); setXXX(int num); �����Ӧ�ķ���������֪��������������
								ctypename=rs.getObject(colnames[i]).getClass().getName();//��ȡ��ǰ�е�������

								if("java.lang.Integer".equals(ctypename)){
									m.invoke(t,rs.getInt(colnames[i])); //obj.setXX(xx);
								}else if("java.lang.String".equals(ctypename)){
									m.invoke(t, rs.getString(colnames[i]));
								}else if("java.math.BigInteger".equals(ctypename)){
									m.invoke(t, rs.getDouble(colnames[i]));
								}else if("java.math.BigDecimal".equals(ctypename)){
									try{
										m.invoke(t, rs.getInt(colnames[i]));
									}catch(Exception e1){
										m.invoke(t, rs.getDouble(colnames[i]));
									}
								}else if("java.sql.Timestamp".equals(ctypename)){
									m.invoke(t, rs.getString(colnames[i]));
								}else if("java.sql.Date".equals(ctypename)){
									m.invoke(t, rs.getString(colnames[i]));
								}else if("java.sql.Time".equals(ctypename)){
									m.invoke(t, rs.getString(colnames[i]));
								}else if("image".equals(ctypename)) {
									m.invoke(t,rs.getBlob(colnames[i]));
								}else{
									m.invoke(t, rs.getString(colnames[i]));
								}
								break;
							}
						}
					}
				}
				list.add(t);
			}
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}finally{
			closeAll(con, pstmt, rs,null);
		}
		return list;
	}

	/**
	 * ��ѯ
	 * @param <T> ���ͣ�����Ҫ�õ��ļ����д�Ķ��������
	 * @param sql: ��ѯ��䣬���Ժ���?
	 * @param params: ?����Ӧ�Ĳ���ֵ�ļ���
	 * @param c�� ������������Ӧ�ķ������
	 * @return ���洢�˶���ļ���
	 * @throws SQLException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public <T> T findByOne(String sql,Class<T> c,List<Object> params) {
		Connection con=getConnection();
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		T t = null;
		try {
			pstmt=con.prepareStatement(sql); //Ԥ�������
			setValues(pstmt, params); //����ռλ��
			rs=pstmt.executeQuery();  //ִ�в�ѯ��䣬�õ������

			Method[] ms=c.getMethods(); //ȡ���������ʵ�������з���
			
			String[] colnames=getColumnNames(rs); //��ȡ������������е�����
			String mname=null;  //������
			String cname=null;  //����
			String ctypename=null; //������

			while(rs.next()){
				t=(T)c.newInstance(); //�����������ʵ��������    Product t=(Product)c.newInstance();
				for(int i=0;i<colnames.length;i++){//ѭ�������� ,��ʽΪsetXXXX��getXXX
					cname=colnames[i]; //ȡ����������ǰ�����set  setXXX
					cname="set"+cname.substring(0,1).toUpperCase()+cname.substring(1).toLowerCase();
					if(ms!=null&&ms.length>0){
						for(Method m:ms){//ѭ������
							mname=m.getName(); //ȡ��������

							if(cname.equals(mname)&&rs.getObject(colnames[i])!=null){//�жϷ������������Ƿ�һ������ͬ�򼤻����ע������      //ֻҪ"set"+��������.equalsIgnoreCase�������������򼤻��������
								//setXXX(String str); setXXX(int num); �����Ӧ�ķ���������֪��������������
								ctypename=rs.getObject(colnames[i]).getClass().getName();//��ȡ��ǰ�е�������

								if("java.lang.Integer".equals(ctypename)){
									m.invoke(t,rs.getInt(colnames[i])); //obj.setXX(xx);
								}else if("java.lang.String".equals(ctypename)){
									m.invoke(t, rs.getString(colnames[i]));
								}else if("java.math.BigInteger".equals(ctypename)){
									m.invoke(t, rs.getDouble(colnames[i]));
								}else if("java.math.BigDecimal".equals(ctypename)){
									try{
										m.invoke(t, rs.getInt(colnames[i]));
									}catch(Exception e1){
										m.invoke(t, rs.getDouble(colnames[i]));
									}
								}else if("java.sql.Timestamp".equals(ctypename)){
									m.invoke(t, rs.getString(colnames[i]));
								}else if("java.sql.Date".equals(ctypename)){
									m.invoke(t, rs.getString(colnames[i]));
								}else if("java.sql.Time".equals(ctypename)){
									m.invoke(t, rs.getString(colnames[i]));
								}else if("image".equals(ctypename)) {
									m.invoke(t,rs.getBlob(colnames[i]));
								}else{
									m.invoke(t, rs.getString(colnames[i]));
								}
								break;
							}
						}
					}
				}
			}
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}finally{
			closeAll(con, pstmt, rs,null);
		}
		return t;
	}
	/**
	 * ��ѯ���ݵķ���
	 * @param sql��Ҫִ�еĲ�ѯ���
	 * @param params����Ӧ��sql����е��ʺŵ�ֵ
	 * @return�������������������ݵļ��� Map<String,String> keyΪ����
	 */
	public List<Map<String,String>> find(String sql,List<Object> params){
		List<Map<String,String>> result=new ArrayList<Map<String,String>>();
		Connection con=getConnection();//��ȡ����
		PreparedStatement pstmt=null;
		ResultSet rs=null;  
		try {
			//��ȡ����
			con=this.getConnection();
			//Ԥ����sql���
			pstmt=con.prepareStatement(sql);

			//��ռλ����ֵ
			setValues(pstmt, params);

			//ִ����䲢��ȡ���صĽ����
			rs=pstmt.executeQuery();

			//��ȡ���صĽ�������е���Ϣ
			String[] cols=getColumnNames(rs); //��ȡ������������е�����

			Map<String,String> map; //�������һ����¼��������Ϊkey,��Ӧ�е�ֵΪvalue

			//��Ϊ��װ��Map�У���map����ѡ������Ϊkey,��Ӧ�е�ֵΪvalue,��������Ҫ��ȡ�����е�����
			while(rs.next()){
				map=new HashMap<String,String>();
				for(String col:cols){
					map.put(col,rs.getString(col));
				}
				result.add(map);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally{
			this.closeAll(con, pstmt, rs,null);
		}
		return result;
	}


	/**
	 * ��ѯ���ݵķ���
	 * @param sql��Ҫִ�еĲ�ѯ���
	 * @param params����Ӧ��sql����е��ʺŵ�ֵ
	 * @return�������������������ݵļ��� Map<String,String> keyΪ����
	 */
	public List<List<String>> finds(String sql,List<Object> params){
		List<List<String>> results=new ArrayList<List<String>>(); //������еļ�¼
		List<String> result; //���һ����¼
		Connection con=getConnection();//��ȡ����
		PreparedStatement pstmt=null;
		ResultSet rs=null;  
		try {
			//��ȡ����
			con=this.getConnection();
			//Ԥ����sql���
			pstmt=con.prepareStatement(sql);

			//��ռλ����ֵ
			setValues(pstmt, params);

			//ִ����䲢��ȡ���صĽ����
			rs=pstmt.executeQuery();

			//��ȡ���صĽ�������е���Ϣ
			ResultSetMetaData rsmd=rs.getMetaData();

			//��Ϊ��װ��Map�У���map����ѡ������Ϊkey,��Ӧ�е�ֵΪvalue,��������Ҫ��ȡ�����е�����
			while(rs.next()){
				result=new ArrayList<String>();
				for(int i=0,len=rsmd.getColumnCount();i<len;i++){
					result.add(rs.getString(i+1));
				}
				results.add(result);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally{
			this.closeAll(con, pstmt, rs, null);
		}
		return results;
	}


	/**
	 * ����ѯ
	 * @param sql����ѯ���
	 * @param params�� ��ѯ�����?����Ӧ��ֵ
	 * @return�������������һ��List���У���Mapһ��һ�Ĵ��
	 * @throws SQLException
	 */
	public List<String> findList(String sql,List<Object> params){
		List<String> result=new ArrayList<String>(); //�����һ�δ���list�з���
		Connection con=getConnection();//��ȡ����
		PreparedStatement pstmt=null;
		ResultSet rs=null;  
		try {
			pstmt=con.prepareStatement(sql);
			setValues(pstmt, params);
			rs=pstmt.executeQuery();
			String[] colnames=getColumnNames(rs); //��ȡ������������е�����
			
			while(rs.next()){
				for(int i=0,len=colnames.length;i<len;i++){
					result.add(rs.getString(i+1));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}finally{
			this.closeAll(con, pstmt, rs ,null);
		}
		return result;
	}



	/**
	 * 
	 * @param sql  Ҫִ�е�sql���
	 * @param objs ִ��sql�����Ҫ�Ĳ���
	 * @return  ȡ�����ݿ������, key���ֶ������ֶα���(Сд��ĸ), valueӦ���ֶε�ֵ
	 */
	public Map<String,String> findMap(String sql,List<Object> objs){
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map<String,String> results = null;
		try {
			con = getConnection();
			pstmt = con.prepareStatement(sql); // 3.sqlִ�й���
			setValues(pstmt, objs);
			rs = pstmt.executeQuery(); // 4.ִ��sqlȡ���������ݰ׽����
			ResultSetMetaData rsmd = rs.getMetaData(); // Ԫ����; ����ȡȡ���Ľ�������ݵ�����
			int cloumCount = rsmd.getColumnCount();
			if (rs.next()) { // �жϽ�����Ƿ������� (������һ����¼�ķ�ʽȡ��)
				results = new HashMap<String,String>();
				for (int i = 1; i <= cloumCount; i++) {
					results.put(rsmd.getColumnName(i).toLowerCase(), rs.getString(i));
				}
			}
		}  catch (SQLException e) {
			throw new RuntimeException(e);
		}finally{
			this.closeAll(con, pstmt, rs ,null);
		}
		return results;
	}


	/**
	 * ������̲�������
	 * @param cst
	 * @param params
	 */
	@SuppressWarnings("unchecked")
	public void setParams(CallableStatement cs,Map<Integer,Object> paramsIn,Map<Integer,String> paramsOut){
		int key=0; //��Ӧ���ʺŵ����
		Object value=null;
		String typename=null;

		String attrType;
		Set keys;  //���еļ�
		if(paramsIn!=null&&paramsIn.size()>0){
			keys=paramsIn.keySet();  //ȡ��������εļ�������ζ�Ӧ���ʺŵ����
			if(keys!=null){
				Iterator iterator=keys.iterator();
				while(iterator.hasNext()){
					key=(Integer) iterator.next();
					value=paramsIn.get(key);      //1,88
					attrType=value.getClass().getName();

					//�ж�ֵ����������
					try {
						if("java.lang.Integer".equals(attrType)){
							cs.setInt(key,(Integer)value);
						}else if("java.lang.String".equals(attrType)){
							cs.setString(key,(String)value);
						}

					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		int typeId=0;

		if(paramsOut!=null&&paramsOut.size()>0){
			keys=paramsOut.keySet();  //ȡ��������εļ�������ζ�Ӧ���ʺŵ����
			if(keys!=null){
				Iterator iterator=keys.iterator();
				while(iterator.hasNext()){
					key=(Integer) iterator.next();
					typename=(String) paramsOut.get(key);      //3,varchar  4, cursor

					//�ж�ֵ����������
					try {
						 if("int".equals(typename)){
							typeId=Types.INTEGER;
						}else if("double".equals(typename)){
							typeId=Types.NUMERIC;
						}else{
							typeId=Types.VARCHAR;
						}
						cs.registerOutParameter(key,typeId);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

}
