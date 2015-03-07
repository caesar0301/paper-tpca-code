package cn.edu.sjtu.omnilab.tpca.pattern.clustering;

import cn.edu.sjtu.omnilab.tpca.pattern.core.PatternSet;
import cn.edu.sjtu.omnilab.tpca.pattern.core.PatternSetChange;

public class PatternSetDistance4Change extends DistanceMeasure {
	private double minsim;
	
	public PatternSetDistance4Change(double minsim){
		this.minsim = minsim;
	}

	@Override
	public double measure(Instance x, Instance y) {
		PatternSet px = (PatternSet) x.dataValue();
		PatternSet py = (PatternSet) y.dataValue();
		double changeInfo = PatternSetChange.changeInformationMeasure(px, py, this.minsim);
		return changeInfo;
	}
}
