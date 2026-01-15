package lol.szachuz.api.dto;

public class MmrUpdateRequest {
    private int userId;
    private int mmrChange;
    private boolean isWin;

    // Gettery i Settery
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getMmrChange() { return mmrChange; }
    public void setMmrChange(int mmrChange) { this.mmrChange = mmrChange; }

    public boolean isWin() { return isWin; }
    public void setWin(boolean win) { isWin = win; }
}