package com.sense.dbproxy.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sense.dbproxy.po.User;



@Component 
public class UserDao extends BaseDao{

	public void save(com.sense.dbproxy.api.DbProxy.User user) {
		// TODO Auto-generated method stub
		getJdbcTemplate().update("insert into t_user (name,password) values('"+user.getName()+"',"+user.getPassword()+")");
//		throw new RuntimeException("@S#D$FRGTYHUJIK");
		
	}
	
	@Transactional(readOnly=true)
	public User getUser(int id){
		User user =   getJdbcTemplate().queryForObject("select * from t_user where id="+id, rowMapper);
		return user;
		
	}
//    @Autowired
//    @Qualifier("readWriteDataSource")
//    public void setDS(DataSource ds) {
//        setDataSource(ds);
//    }
//    
//    
    private RowMapper<User> rowMapper = new RowMapper<User> () {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            return user;
        }
    };
//    
//   
//    
//    
//    public void save(final User user) {
//        final String sql = "insert into user(name) values(?)";
//        KeyHolder generatedKeyHolder = new GeneratedKeyHolder(); 
//        getJdbcTemplate().update(new PreparedStatementCreator() {
//            
//            @Override
//            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
//                PreparedStatement psst = con.prepareStatement(sql, new String[]{"id"});
//                psst.setString(user.getId(), user.getName());
//                return psst;
//            }
//        }, generatedKeyHolder);
//        
//        user.setId(generatedKeyHolder.getKey().intValue());
//    }
//    
//    public void update(User user) {
//        String sql = "update user set name=? where id=?";
//        getJdbcTemplate().update(sql, user.getName(), user.getId());
//    }
//    
//    public void delete(int id) {
//        String sql = "delete from user where id=?";
//        getJdbcTemplate().update(sql, id);
//    }
//    
//    public User findById(int id) {
//        String sql = "select id, name from user where id=?";
//        List<User> userList = getJdbcTemplate().query(sql, rowMapper, id);
//        
//        if(userList.size() == 0) {
//            return null;
//        }
//        return userList.get(0);
//    }
    
}
