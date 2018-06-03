package group26.textify.Utils;


public class Language {
    private String name;
    private String id;
    public static final Language[] languageArray = {
            new Language("---- Don't translate ----", null),
            new Language("Deutsch", "de"),
            new Language("English", "en"),
            new Language("Español", "es"),
            new Language("Gaeilge", "ga"),
            new Language("Français", "fr"),
            new Language("Nederlands", "nl"),
            new Language("Português", "pt")
    };
    public Language(String name, String id) {
        this.name = name;
        this.id = id;
    }
    public String toString() {
        return name;
    }
    public String getID() {
        return id;
    }

}