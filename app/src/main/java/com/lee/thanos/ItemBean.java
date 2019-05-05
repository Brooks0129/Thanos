package com.lee.thanos;

import java.util.Objects;

public class ItemBean {
    public String title;
    public String description;
    public String buttonContent;
    public int type;

    public ItemBean(String title, String description, String buttonContent, int type) {
        this.title = title;
        this.description = description;
        this.buttonContent = buttonContent;
        this.type = type;
    }

}
