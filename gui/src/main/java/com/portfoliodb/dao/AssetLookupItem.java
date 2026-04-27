package com.portfoliodb.dao;

public class AssetLookupItem extends LookupItem {
    private final String type;

    public AssetLookupItem(int id, String name, String type) {
        super(id, name);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
