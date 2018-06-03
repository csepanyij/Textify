package group26.textify.Utils;

public class DatabaseEntry {

    private String fileName;
    private String recognisedText;
    private String time;

    public DatabaseEntry(String fileName, String recognisedText, String time) {
        this.fileName = fileName;
        this.recognisedText = recognisedText;
        this.time = time;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getRecognisedText() {
        return this.recognisedText;
    }

    public String getTime() {
        return time;
    }

    public DatabaseEntry(){}

}
