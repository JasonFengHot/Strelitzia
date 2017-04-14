package tv.ismar.library.util;

import java.util.List;

/**
 * Created by beaver on 17-4-13.
 */

public class DeptBean {
    private int deptId;
    private String deptName;
    private List<UserBean> userBeanList;


    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public List<UserBean> getUserBeanList() {
        return userBeanList;
    }

    public void setUserBeanList(List<UserBean> userBeanList) {
        this.userBeanList = userBeanList;
    }


    @Override
    public String toString() {
        String userBeanListString = "";
        for (UserBean userBean : userBeanList) {
            userBeanListString += userBean.toString() + "\n";
        }

        return "DeptBean [deptId=" + deptId + ", deptName=" + deptName
                + ", \nuserBeanListString=" + userBeanListString + "]";
    }

    public DeptBean(int deptId, String deptName, List<UserBean> userBeanList) {
        super();
        this.deptId = deptId;
        this.deptName = deptName;
        this.userBeanList = userBeanList;
    }

    public DeptBean() {
        super();
    }

    public static class UserBean {
        private int userId;
        private String userName;
        private String password;
        private String email;

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return "UserBean [userId=" + userId + ", userName=" + userName
                    + ", password=" + password + ", email=" + email + "]";
        }

        public UserBean() {
        }

        public UserBean(int userId, String userName, String password, String email) {
            this.userId = userId;
            this.userName = userName;
            this.password = password;
            this.email = email;
        }

    }
}
