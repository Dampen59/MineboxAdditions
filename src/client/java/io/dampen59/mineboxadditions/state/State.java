package io.dampen59.mineboxadditions.state;

import io.socket.client.Socket;

public class State {
    private boolean isConnectedToMinebox = false;
    private boolean loginCommandSent = false;
    private boolean mouseAlertSent = false;
    private boolean bakeryAlertSent = false;
    private boolean buckstarAlertSent = false;
    private boolean cocktailAlertSent = false;

    private String mouseCurrentItemOffer = null;
    private String bakeryCurrentItemOffer = null;
    private String buckstarCurrentItemOffer = null;
    private String cocktailCurrentItemOffer = null;

    private int currentMoonPhase = -1;

    private Socket objSocket = null;

    public void setConnectedToMinebox(boolean prmValue) {
        this.isConnectedToMinebox = prmValue;
    }
    public boolean getConnectedToMinebox() {
        return this.isConnectedToMinebox;
    }

    public void setLoginCommandSent(boolean prmValue) {
        this.loginCommandSent = prmValue;
    }

    public boolean getLoginCommandSent() {
        return this.loginCommandSent;
    }

    public void setMouseAlertSent(boolean prmValue) {
        this.mouseAlertSent = prmValue;
    }
    public boolean getMouseAlertSent() {
        return this.mouseAlertSent;
    }

    public void setBakeryAlertSent(boolean prmValue) {
        this.bakeryAlertSent = prmValue;
    }
    public boolean getBakeryAlertSent() { return this.bakeryAlertSent; }

    public void setBuckstarAlertSent(boolean prmValue) {
        this.buckstarAlertSent = prmValue;
    }
    public boolean getBuckstarAlertSent() { return this.buckstarAlertSent; }

    public void setCocktailAlertSent(boolean prmValue) {
        this.cocktailAlertSent = prmValue;
    }
    public boolean getCocktailAlertSent() { return this.cocktailAlertSent; }

    public void setMouseCurrentItemOffer(String prmValue) { this.mouseCurrentItemOffer = prmValue; }
    public String getMouseCurrentItemOffer() { return this.mouseCurrentItemOffer; }

    public void setBakeryCurrentItemOffer(String prmValue) { this.bakeryCurrentItemOffer = prmValue; }
    public String getBakeryCurrentItemOffer() { return this.bakeryCurrentItemOffer; }

    public void setBuckstarCurrentItemOffer(String prmValue) { this.buckstarCurrentItemOffer = prmValue; }
    public String getBuckstarCurrentItemOffer() { return this.buckstarCurrentItemOffer; }

    public void setCocktailCurrentItemOffer(String prmValue) { this.cocktailCurrentItemOffer = prmValue; }
    public String getCocktailCurrentItemOffer() { return this.cocktailCurrentItemOffer; }

    public void setCurrentMoonPhase(int prmValue) { this.currentMoonPhase = prmValue; }
    public int getCurrentMoonPhase() { return this.currentMoonPhase; }

    public void setSocket(Socket prmValue) { this.objSocket = prmValue; }
    public Socket getSocket() { return this.objSocket; }
}
