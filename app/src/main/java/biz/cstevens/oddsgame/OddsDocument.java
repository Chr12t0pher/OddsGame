package biz.cstevens.oddsgame;

public class OddsDocument {
    public String a_id;
    public String a_name;
    public int a_odds;
    public String b_id;
    public String b_name;
    public int b_odds;
    public String message;
    public int odds;
    public boolean reversed;

    public OddsDocument() {}

    public OddsDocument(String a_id, String a_name, int a_odds, String b_id, String b_name, int b_odds, String message, int odds, boolean reversed) {
        this.a_id = a_id;
        this.a_name = a_name;
        this.a_odds = a_odds;
        this.b_id = b_id;
        this.b_name = b_name;
        this.b_odds = b_odds;
        this.message = message;
        this.odds = odds;
        this.reversed = reversed;
    }

    public OddsDocument(String a_id, String a_name, String b_id, String b_name, String message, int odds) {
        this.a_id = a_id;
        this.a_name = a_name;
        this.a_odds = -1;
        this.b_id = b_id;
        this.b_name = b_name;
        this.b_odds = -1;
        this.message = message;
        this.odds = odds;
        this.reversed = false;
    }
}
