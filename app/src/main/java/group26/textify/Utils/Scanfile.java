package group26.textify.Utils;

import java.io.Serializable;
import org.joda.time.LocalDateTime;

import java.io.File;

@SuppressWarnings("serial")
public class Scanfile implements Serializable{

    final int SUMMARY_SIZE = 130;

    private String name;
    private String text;
    private String summary;
    private File image;
    private LocalDateTime dateTaken;

    private boolean selected;

    public Scanfile(String name, String text, LocalDateTime dateTaken,File image){
        this.name = name;
        this.text = text;
        this.dateTaken = dateTaken;
        if(text.length()>SUMMARY_SIZE){
            this.summary = text.substring(0,SUMMARY_SIZE)+"...";
        }else{
            this.summary = text;
        }
        this.image=image;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public String getSummary() {
        return summary;
    }

    public File getImage() { return image; }

    public LocalDateTime getDateTaken() { return dateTaken; }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}