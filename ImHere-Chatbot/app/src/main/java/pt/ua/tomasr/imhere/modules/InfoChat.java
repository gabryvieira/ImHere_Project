package pt.ua.tomasr.imhere.modules;

/**
 * Created by reytm on 02/11/2016.
 */

public class InfoChat {
    private Integer id;
    private String name;
    private String description;
    private String time;
    private String event;


    public InfoChat(Integer id, String name, String description, String time, String event) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.time = time;
        this.event = event;
    }

    public Integer getID() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getTime() {
        return time;
    }
    public String getEvent() { return event; }

    public void setId(Integer id){
        this.id=id;
    }
    public void setName(String name){
        this.name=name;
    }
    public void setDescription(String description){
        this.description=description;
    }
    public void setTime(String time){ this.time=time;}
    public void setEvent(String event){
        this.event=event;
    }


}
