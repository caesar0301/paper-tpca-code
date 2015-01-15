package omnilab.bd.pattern.clustering;


public abstract class DistanceMeasure {
	
	public abstract double measure(Instance x, Instance y);
	
	public boolean compare(double x, double y) {
		return x < y;
	}

	public double getMinValue() {
		return 0;
	}

	public double getMaxValue() {
		return Double.POSITIVE_INFINITY;
	}
}
