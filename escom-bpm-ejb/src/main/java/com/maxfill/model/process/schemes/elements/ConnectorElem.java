package com.maxfill.model.process.schemes.elements;

/**
 * Сущность "Элемент схемы процесса "Коннектор"
 */
public class ConnectorElem extends WorkflowElement{
    private static final long serialVersionUID = -2062835627519052276L;

    private AnchorElem from;
    private AnchorElem to;

    public ConnectorElem(String caption, AnchorElem from, AnchorElem to) {
        super(caption);
        this.from = from;
        this.to = to;
    }

    public AnchorElem getFrom() {
        return from;
    }
    public void setFrom(AnchorElem from) {
        this.from = from;
    }

    public AnchorElem getTo() {
        return to;
    }
    public void setTo(AnchorElem to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "ConnectorElem{" +
                "caption='" + caption + '\'' +
                '}';
    }

    @Override
    public String getStyle() {
        return "";
    }
}
