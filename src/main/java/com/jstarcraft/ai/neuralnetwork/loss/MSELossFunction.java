package com.jstarcraft.ai.neuralnetwork.loss;

import com.jstarcraft.ai.math.structure.matrix.MathMatrix;

/**
 * MSE目标函数
 * 
 * <pre></pre>
 * 
 * @author Birdy
 *
 */
public class MSELossFunction extends L2LossFunction {

	@Override
	public float computeScore(MathMatrix tests, MathMatrix trains, MathMatrix masks) {
		float score = super.computeScore(tests, trains, masks);
		return score / trains.getColumnSize();
	}

	@Override
	public void computeGradient(MathMatrix tests, MathMatrix trains, MathMatrix masks, MathMatrix gradients) {
		super.computeGradient(tests, trains, masks, gradients);
		float scale = 1F / trains.getColumnSize();
		gradients.scaleValues(scale);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "MSELossFunction()";
	}

}
