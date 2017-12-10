package se.kth.jabeja.annealing;

public class RestartingLinearAnnealing implements AnnealingStrategy {
    private float initialTemperature;
    private float delta;
    private int steps = 0;
    private int restartedSteps;
    private int restartFrom = 400;

    public RestartingLinearAnnealing(float initialTemperature, float delta) {
        this.initialTemperature = initialTemperature;
        this.delta = delta;
    }

    @Override
    public float getTemperature() {
        return Math.max(1, initialTemperature - delta * restartedSteps);
    }

    @Override
    public boolean accept(double oldBenefit, double newBenefit) {
        return newBenefit * getTemperature() > oldBenefit;
    }

    @Override
    public void anneal() {
        steps++;
        restartedSteps = steps >= restartFrom ? steps - restartFrom : steps;
    }
}
