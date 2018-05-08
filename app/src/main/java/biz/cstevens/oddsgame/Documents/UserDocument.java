package biz.cstevens.oddsgame.Documents;

public class UserDocument {
    public String name;
    public String imgUri;
    public String fcmToken;

    public UserDocument() {}

    public UserDocument(String name, String imgUri, String fcmToken) {
        this.name = name;
        this.imgUri = imgUri;
        this.fcmToken = fcmToken;
    }
}
