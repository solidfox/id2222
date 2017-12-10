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
    public boolean accept(double oldBenefit, double newBenefit) {
        if (oldBenefit == newBenefit) { return false; }
        double p = Math.exp((newBenefit-oldBenefit)/temperature);
//        System.out.println(newBenefit + " " + oldBenefit + " " + p);
        return p > Math.random();
    }

    @Override
    public void anneal() {
        temperature = temperature * multiplier;
    }
}
