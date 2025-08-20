package br.com.apisentinel.client.governance;

public class AssociationRequest {
    private String target;
    private String type;

    public AssociationRequest() {
    }

    public AssociationRequest(String target, String type) {
        this.target = target;
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String t) {
        this.target = t;
    }

    public String getType() {
        return type;
    }

    public void setType(String t) {
        this.type = t;
    }
}
