package tv.ismar.library.util;

import com.fasterxml.jackson.core.type.TypeReference;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LongHai on 17-4-13.
 */
public class JacksonUtilsTest {

    @Test
    public void testJson() throws Exception {
        DeptBean.UserBean userBean1 = new DeptBean.UserBean(1, "liubei", "123", "liubei@163.com");
        DeptBean.UserBean userBean2 = new DeptBean.UserBean(2, "guanyu", "123", "guanyu@163.com");
        DeptBean.UserBean userBean3 = new DeptBean.UserBean(3, "zhangfei", "123", "zhangfei@163.com");

        List<DeptBean.UserBean> userBeans = new ArrayList<>();
        userBeans.add(userBean1);
        userBeans.add(userBean2);
        userBeans.add(userBean3);

        DeptBean deptBean = new DeptBean(1, "sanguo", userBeans);
        //对象转json
        String userBeanToJson = JacksonUtils.toJson(userBean1);
        String deptBeanToJson = JacksonUtils.toJson(deptBean);

        System.out.println("deptBean to json:" + deptBeanToJson);
        System.out.println("userBean to json:" + userBeanToJson);

        // json转字符串
        DeptBean.UserBean jsonToUserBean = JacksonUtils.fromJson(userBeanToJson, DeptBean.UserBean.class);
        DeptBean jsonToDeptBean = JacksonUtils.fromJson(deptBeanToJson, DeptBean.class);

        System.out.println("json to DeptBean" + jsonToDeptBean.toString());
        System.out.println("json to UserBean" + jsonToUserBean.toString());

        //List 转json字符串
        String listToJson = JacksonUtils.toJson(userBeans);
        System.out.println("list to json:" + listToJson);

        //数组json转 List
        List<DeptBean.UserBean> jsonToUserBeans = JacksonUtils.fromJson(listToJson, new TypeReference<List<DeptBean.UserBean>>() {
        });
        String userBeanString = "";
        for (DeptBean.UserBean userBean : jsonToUserBeans) {
            userBeanString += userBean.toString() + "\n";
        }
        System.out.println("json to userBeans:" + userBeanString);
    }

}