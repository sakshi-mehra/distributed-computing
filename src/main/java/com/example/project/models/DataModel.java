package com.example.project.models;

    public class DataModel {

    private Integer id;
    private String firstName;
    private String lastName;
//    private String userName;
//    private String password;
//    private String mobile;
//    private String email;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    @Override
    public boolean equals(Object obj){
        if (this == obj){
            return true;
        }
        DataModel person = (DataModel) obj;
        if (firstName != null ?
                !firstName.equals(person.firstName)
                :person.firstName != null){
            return false;
        }
        else {
            return true;
        }
    }
    @Override
    public String toString() {
        return "UserLoginModel [id=" + id + ", firstName=" + firstName
                + ", lastName=" + lastName + "]";
    }
//                +
//                ", userName=" + userName
//                + ", password=" + password + ", mobile=" + mobile
//                + ", e public String getUserName() {\n" +
//                "//        return userName;\n" +
//                "//    }\n" +
//                "//    public void setUserName(String userName) {\n" +
//                "//        this.userName = userName;\n" +
//                "//    }\n" +
//                "//    public String getPassword() {\n" +
//                "//        return password;\n" +
//                "//    }\n" +
//                "//    public void setPassword(String password) {\n" +
//                "//        this.password = password;\n" +
//                "//    }mail=" + email + "]";
//    }
//
//    public String getMobile() {
//        return mobile;
//    }
//    public void setMobile(String mobile) {
//        this.mobile = mobile;
//    }
//    public String getEmail() {
//        return email;
//    }
//    public void setEmail(String email) {
//        this.email = email;
//    }
}
