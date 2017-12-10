package se.kth.jabeja.annealing;

public interface AnnealingStrategy {
    float getTemperature();
    boolean accept(double oldBenefit, double newBenefit);
    void anneal();
}
