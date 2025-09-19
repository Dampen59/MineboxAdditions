package io.dampen59.mineboxadditions.state;

public class OfferState {
    private boolean mouseAlertSent = false;
    private boolean bakeryAlertSent = false;
    private boolean buckstarAlertSent = false;
    private boolean cocktailAlertSent = false;

    private String mouseOffer = null;
    private String bakeryOffer = null;
    private String buckstarOffer = null;
    private String cocktailOffer = null;

    public boolean isMouseAlertSent() { return mouseAlertSent; }
    public void setMouseAlertSent(boolean value) { this.mouseAlertSent = value; }

    public boolean isBakeryAlertSent() { return bakeryAlertSent; }
    public void setBakeryAlertSent(boolean value) { this.bakeryAlertSent = value; }

    public boolean isBuckstarAlertSent() { return buckstarAlertSent; }
    public void setBuckstarAlertSent(boolean value) { this.buckstarAlertSent = value; }

    public boolean isCocktailAlertSent() { return cocktailAlertSent; }
    public void setCocktailAlertSent(boolean value) { this.cocktailAlertSent = value; }

    public String getMouseOffer() { return mouseOffer; }
    public void setMouseOffer(String offer) { this.mouseOffer = offer; }

    public String getBakeryOffer() { return bakeryOffer; }
    public void setBakeryOffer(String offer) { this.bakeryOffer = offer; }

    public String getBuckstarOffer() { return buckstarOffer; }
    public void setBuckstarOffer(String offer) { this.buckstarOffer = offer; }

    public String getCocktailOffer() { return cocktailOffer; }
    public void setCocktailOffer(String offer) { this.cocktailOffer = offer; }

    public void reset() {
        mouseAlertSent = false;
        bakeryAlertSent = false;
        buckstarAlertSent = false;
        cocktailAlertSent = false;

        mouseOffer = null;
        bakeryOffer = null;
        buckstarOffer = null;
        cocktailOffer = null;
    }

    public static class MermaidItemOffer {
        public int quantity = 0;
        public String itemTranslationKey = null;
        public String itemTranslationKeyArgs = null;

        public void set(int qty, String key, String args) {
            this.quantity = qty;
            this.itemTranslationKey = key;
            this.itemTranslationKeyArgs = args;
        }
    }
}