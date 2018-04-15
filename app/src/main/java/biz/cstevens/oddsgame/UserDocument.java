package biz.cstevens.oddsgame;

public class UserDocument {
    public String uID;
    public String name;
    public String email;
    public String[] odds;

    public UserDocument() {}

    public UserDocument(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
