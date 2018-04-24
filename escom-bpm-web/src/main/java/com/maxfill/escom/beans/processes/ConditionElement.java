package com.maxfill.escom.beans.processes;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.endpoint.EndPoint;

public class ConditionElement extends Element{

    public ConditionElement(Object data, String x, String y) {
        super(data, x, y);
    }

    /**
     * Возвращает выход ДА условия
     * @return
     */
    public EndPoint getYesPoint(){
        for (EndPoint endPoint : getEndPoints()){
            String id = endPoint.getId();
            if (id.equals("yes")){
                return endPoint;
            }
        }
        return null;
    }

    /**
     * Возвращает выход НЕТ условия
     * @return
     */
    public EndPoint getNoPoint(){
        for (EndPoint endPoint : getEndPoints()){
            String id = endPoint.getId();
            if (id.equals("no")){
                return endPoint;
            }
        }
        return null;
    }
}
