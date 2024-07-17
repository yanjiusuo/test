package com.jd.workflow.console.service.doc.app.controller;


import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.service.doc.app.dto.impl.Dog;
import com.jd.workflow.console.service.doc.app.dto.AllTypeEntity;
import com.jd.workflow.console.service.doc.app.dto.Animal;
import com.jd.workflow.console.service.doc.app.dto.PageQuery;
import com.jd.workflow.console.service.doc.app.dto.Person;
import io.swagger.annotations.*;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


@RestController
@Api("测试控制器")
public class TestController {
    /**
     * @CookieValue Springfox不支持 https://github.com/springfox/springfox/issues/2798
     * @param person
     * @param body
     * @param pageNo
     * @param pageSize
     * @param id1
     * @param id2
     * @param token1
     * @param token2
     * @param cookie1
     * @param cookie2
     * @return
     */
    @RequestMapping("/person/{id1}/{id2}")
    @ApiOperation("获取全部数据")
    public String allData(@CookieValue("cookie1") String cookie1,
                          @CookieValue("cookie2") String cookie2, Person person, @RequestBody Person body, @RequestBody Person body1,
                          @ApiParam("分页页码")
                          String pageNo, String pageSize,
                          @PathVariable(value="id1") String id1,
                          @PathVariable(value="id2") String id2,
                          @RequestHeader("token1") String token1,
                          @RequestHeader("token2") String token2

                          ) {
        // 返回请求结果
        return "123";
    }
    @RequestMapping(value = "/sss/**")
    public void execute(
            @RequestHeader Map<String, Object> header,
            @RequestParam Map<String, Object> params,

            HttpServletRequest request,
            HttpServletResponse response) throws IOException {


    }
    @RequestMapping(value = "formData",method = RequestMethod.POST,consumes={"application/x-www-form-urlencoded"})
    public String formData(
        Person person,String key
    ) {
        // 返回请求结果
        return "123";
    }
    @RequestMapping(value = "allOf",method = RequestMethod.POST)

    public Dog allOf(
            @RequestBody Animal animals
            ) {
        // 返回请求结果
        return null;
    }
    @RequestMapping(value = "implicit",method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户ID", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "roleId", value = "角色ID", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "person", value = "角色ID", required = true, dataTypeClass = Person.class, paramType = "body"),
    })
    public Person implicit(

    ) {
        // 返回请求结果
        return null;
    }
    @RequestMapping(value = "generic",method = RequestMethod.POST)

    public CommonResult<Person> generic(

    ) {
        // 返回请求结果
        return null;
    }
    @RequestMapping(value = "query",method = {RequestMethod.POST,RequestMethod.GET})
    public String query(
            Person person,String key
    ) {
        // 返回请求结果
        return "123";
    }

    /**
     * 针对query对象里嵌套query参数的场景
     * @param query
     * @return
     */
    @RequestMapping(value = "complexQuery",method = {RequestMethod.POST,RequestMethod.GET})
    public String complexQuery(
            PageQuery query
    ) {
        // 返回请求结果
        return "123";
    }
    @RequestMapping(value = "mapQuery",method = {RequestMethod.POST,RequestMethod.GET})
    public String complexQuery(
            Map<String,Object> query, ModelMap modelMap
            ) {
        // 返回请求结果
        return "123";
    }


    @RequestMapping(value = "samePath",method = RequestMethod.GET)
    public String samePathGet(
            Person person
    ) {
        // 返回请求结果
        return "123";
    }
    @RequestMapping(value = "samePath",method = RequestMethod.POST)
    public String samePathPost(
           String sid
    ) {
        // 返回请求结果
        return "123";
    }
    @RequestMapping("/allType")
    public AllTypeEntity allType(@RequestBody AllTypeEntity allTypeEntity) {
        // 返回请求结果
        return null;
    }



}
