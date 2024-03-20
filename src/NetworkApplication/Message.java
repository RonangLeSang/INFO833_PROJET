package NetworkApplication;

public class Message {

    private int scrIndex;
    private int srcID;
    private int reqIndex;
    private int reqID;
    private int type;
    private int newConnectionId;
    private int newConnectionIndex;
    Message(int scrIndex, int srcID, int reqIndex, int reqID, int type) {
	this.scrIndex = scrIndex;
    this.srcID = srcID;
    this.reqIndex = reqIndex;
    this.reqID = reqID;
    this.type = type;
    }

    public void setSrcID(int srcID) {this.srcID = srcID;}
    public void setReqIndex(int reqIndex) {this.reqIndex = reqIndex;}
    public void setReqID(int reqID) {this.reqID = reqID;}
    public void setType(int type) {this.type = type;}
    public void setNewConnectionId(int newConnectionId) {this.newConnectionId = newConnectionId;}
    public void setNewConnectionIndex(int newConnectionIndex) {this.newConnectionIndex = newConnectionIndex;}

    public int getSrcID() {return srcID;}
    public int getReqIndex() {return reqIndex;}
    public int getReqID() {return reqID;}
    public int getType() {return type;}
    public int getNewConnectionId() {return newConnectionId;}
    public int getNewConnectionIndex() {return newConnectionIndex;}
}