package net.kazhik.gambarumeterlib.entity;

/**
 * Created by kazhik on 15/01/03.
 */
public class Preference {
    private String key;
    private String name;
    private Object value;

    public Preference(String key, String name, String value) {
        this.key = key;
        this.name = name;
        this.value = value;
    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setIntValue(int value) {
        this.value = value;
        
    }
    public int getIntValue() {
        return (Integer)this.value;
        
    }
    public void setBooleanValue(boolean value) {
        this.value = value;
        
    }
    public boolean getBooleanValue() {
        return (Boolean)this.value;
        
    }
    public void setStringValue(String value) {
        this.value = value;
        
    }
    public String getStringValue() {
        return (String)this.value;
        
    }
}
