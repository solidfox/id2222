package se.kth.jabeja.annealing;

public class ExponentialAnnealing implements AnnealingStrategy {
    float temperature;
    float multiplier;

    public ExponentialAnnealing(float initialTemperature, float multiplier) {
        this.temperature = initialTemperature;
        this.multiplier = multiplier;
    }

    @Override
    public float getTemperature() {
        return temperature;
    }

    @Override
    public boolean accept(int oldBenefit, int newBenefit) {
        if (oldBenefit == newBenefit) { return false; }
        double p = Math.exp((newBenefit-oldBenefit)/temperature);
        return p > Math.random();
    }

    @Override
    public void anneal() {
        temperature = temperature * multiplier;
    }
}
