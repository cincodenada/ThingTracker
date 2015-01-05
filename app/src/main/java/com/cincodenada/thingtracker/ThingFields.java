package com.cincodenada.thingtracker;

import java.util.LinkedHashMap;

public class ThingFields {
    public static final LinkedHashMap<String, String> fieldTypes = new LinkedHashMap<String, String>();
    static {
        fieldTypes.put("text","Text");
        fieldTypes.put("yesno","Checkbox");
        fieldTypes.put("list","List");
    }
}
