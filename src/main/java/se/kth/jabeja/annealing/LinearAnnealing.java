package se.kth.jabeja.annealing;

public class LinearAnnealing implements AnnealingStrategy {
    private float initialTemperature;
    private float delta;
    private int steps = 0;

    public LinearAnnealing(float initialTemperature, float delta) {
        this.initialTemperature = initialTemperature;
        this.delta = delta;
    }

    @Override
    public float getTemperature() {
        return Math.max(1, initialTemperature - delta * steps);
    }

    @Override
    public boolean accept(int oldBenefit, int newBenefit) {
        return newBenefit * getTemperature() > oldBenefit;
    }

    @Override
    public void anneal() {
        steps++;
    }
}
