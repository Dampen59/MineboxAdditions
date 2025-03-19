package io.dampen59.mineboxadditions.minebox;

public class MineboxChatFlag {
    private String lang;
    private String flag;

    public MineboxChatFlag(String lang, String flag) {
        this.lang = lang;
        this.flag = flag;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

}