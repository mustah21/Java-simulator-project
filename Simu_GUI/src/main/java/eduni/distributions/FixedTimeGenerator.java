package eduni.distributions;

public class FixedTimeGenerator extends Generator implements ContinuousGenerator {
    private final double fixedTime;

    public FixedTimeGenerator(double fixedTime) {
        super();
        if (fixedTime < 0) {
            throw new ParameterException("FixedTimeGenerator: Time must be non-negative.");
        }
        this.fixedTime = fixedTime;
    }

    public FixedTimeGenerator(double fixedTime, long seed) {
        super(seed);
        if (fixedTime < 0) {
            throw new ParameterException("FixedTimeGenerator: Time must be non-negative.");
        }
        this.fixedTime = fixedTime;
    }

    @Override
    public double sample() {
        return fixedTime;
    }
}

