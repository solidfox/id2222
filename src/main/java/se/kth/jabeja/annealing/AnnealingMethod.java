package se.kth.jabeja.annealing;

public enum AnnealingMethod {
    LINEAR("LINEAR"),
    EXPONENTIAL("EXPONENTIAL"),
    LINEARRESTART("LINEARRESTART"),
    NONE("NONE");
    String name;
    AnnealingMethod(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
            return name;
        }
    public AnnealingStrategy getStrategy(float initialTemperature, float annealingSpeed) {
        switch (this) {
            case LINEAR:
                return new LinearAnnealing(initialTemperature, annealingSpeed);
            case EXPONENTIAL:
                return new ExponentialAnnealing(initialTemperature, annealingSpeed);
            case NONE:
                return new NoAnnealing();
            case LINEARRESTART:
                return new RestartingLinearAnnealing(initialTemperature, annealingSpeed);
        }
        throw new IllegalStateException("All cases should be handled before this point.");
    }
}
