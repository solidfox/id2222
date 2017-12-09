package se.kth.jabeja.annealing;

public interface AnnealingStrategy {
    float getTemperature();
    boolean accept(int oldBenefit, int newBenefit);
    void anneal();
}
