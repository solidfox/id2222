package se.kth.jabeja.annealing;

public class NoAnnealing implements AnnealingStrategy {
    @Override
    public float getTemperature() {
        return 0;
    }

    @Override
    public boolean accept(int oldBenefit, int newBenefit) {
        return newBenefit > oldBenefit;
    }

    @Override
    public void anneal() {}
}
