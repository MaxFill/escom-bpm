package com.maxfill.model.process.schemes.elements;

public class AnchorElem extends WorkflowElement{
    private static final long serialVersionUID = 8128988436740920824L;
    public static final String STYLE_YES = "{fillStyle:'#099b05'}";
    public static final String STYLE_NO = "{fillStyle:'#C33730'}";
    public static final String STYLE_MAIN = "{fillStyle:'#98AFC7'}";

    private String position;
    private Boolean type;
    private WorkflowConnectedElement owner;
    private String style;

    public AnchorElem(String caption, String position, Boolean type, WorkflowConnectedElement owner) {
        super(caption);
        this.position = position;
        this.type = type;
        this.owner = owner;
    }

    public WorkflowConnectedElement getOwner() {
        return owner;
    }
    public void setOwner(WorkflowConnectedElement owner) {
        this.owner = owner;
    }

    public String getPosition() {
        return position;
    }
    public void setPosition(String position) {
        this.position = position;
    }

    public Boolean getType() {
        return type;
    }
    public void setType(Boolean type) {
        this.type = type;
    }

    public Boolean isSource(){
        return type;
    }

    public Boolean isTarget(){
        return !type;
    }

    @Override
    public String getStyle() {
        return style;
    }
    public void setStyle(String style) {
        this.style = style;
    }

    /* *** *** */

    @Override
    public String toString() {
        return "AnchorElem{" +
                "position='" + position + '\'' +
                ", caption='" + caption + '\'' +
                '}';
    }
}
