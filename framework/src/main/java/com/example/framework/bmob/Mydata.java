package com.example.framework.bmob;

import cn.bmob.v3.BmobObject;

/**
 * test bmob
 */
public class Mydata extends BmobObject {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    private int sex;

}
