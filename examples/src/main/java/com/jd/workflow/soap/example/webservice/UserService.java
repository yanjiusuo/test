package com.jd.workflow.soap.example.webservice;

import com.jd.workflow.soap.example.webservice.model.User;

import javax.xml.ws.WebEndpoint;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UserService {
    @WebEndpoint
    public Map<String,Object> getUserInfo(String userId,String name);
 
    public void save(User user) ;
   
    public int update(User user);

   
    public String[] testArray(String[] ids, Integer names) ;
   
    public List<User> testArray(String[] ids) ;

   
    public float testFloat(float param) ;
   
    public Date testDate(long timestamp);


}
