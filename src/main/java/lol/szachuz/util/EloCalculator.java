package lol.szachuz.util;

public class EloCalculator {

    // Współczynnik K - określa jak dynamicznie zmienia się ranking.
    // 32 to standard dla amatorskich szachów i gier online.
    // Możesz go zmniejszyć do 10-20 dla graczy z bardzo wysokim poziomem.
    private static final int K_FACTOR = 32;

    public static final double WIN = 1.0;
    public static final double DRAW = 0.5;
    public static final double LOSS = 0.0;

    /**
     * Oblicza zmianę rankingu (delta) dla gracza.
     *
     * @param playerRating   Aktualne MMR gracza
     * @param opponentRating Aktualne MMR przeciwnika
     * @param result         Wynik meczu (1.0 = wygrana, 0.5 = remis, 0.0 = przegrana)
     * @return Ilość punktów do dodania (lub odjęcia, jeśli ujemne)
     */
    public static int calculateMmrChange(int playerRating, int opponentRating, double result) {
        double exponent = (double)(opponentRating - playerRating) / 400.0;
        double expectedScore = 1.0 / (1.0 + Math.pow(10.0, exponent));
        double change = K_FACTOR * (result - expectedScore);
        return (int) Math.round(change);
    }
}