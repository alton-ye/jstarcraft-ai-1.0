package com.jstarcraft.ai.math.structure.matrix;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.nd4j.linalg.factory.Nd4j;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jstarcraft.ai.environment.EnvironmentContext;
import com.jstarcraft.ai.math.structure.MathCalculator;
import com.jstarcraft.ai.math.structure.vector.MathVector;
import com.jstarcraft.ai.math.structure.vector.RandomVector;
import com.jstarcraft.core.utility.RandomUtility;

import it.unimi.dsi.fastutil.ints.Int2FloatAVLTreeMap;

public class RowRandomMatrixTestCase extends MatrixTestCase {

	@Override
	protected RowRandomMatrix getRandomMatrix(int dimension) {
		Table<Integer, Integer, Float> table = HashBasedTable.create();
		for (int rowIndex = 0; rowIndex < dimension; rowIndex++) {
			for (int columnIndex = 0; columnIndex < dimension; columnIndex++) {
				if (RandomUtility.randomBoolean()) {
					table.put(rowIndex, columnIndex, 0F);
				}
			}
		}
		SparseMatrix data = SparseMatrix.valueOf(dimension, dimension, table);
		RandomVector[] vectors = new RandomVector[dimension];
		for (int rowIndex = 0; rowIndex < dimension; rowIndex++) {
			vectors[rowIndex] = new RandomVector(data.getRowVector(rowIndex), new Int2FloatAVLTreeMap());
		}
		RowRandomMatrix matrix = RowRandomMatrix.valueOf(dimension, vectors);
		matrix.iterateElement(MathCalculator.SERIAL, (scalar) -> {
			scalar.setValue(RandomUtility.randomInteger(dimension));
		});
		return matrix;
	}

	@Override
	protected RowRandomMatrix getZeroMatrix(int dimension) {
		Table<Integer, Integer, Float> table = HashBasedTable.create();
		for (int rowIndex = 0; rowIndex < dimension; rowIndex++) {
			for (int columnIndex = 0; columnIndex < dimension; columnIndex++) {
				table.put(rowIndex, columnIndex, 0F);
			}
		}
		SparseMatrix data = SparseMatrix.valueOf(dimension, dimension, table);
		RandomVector[] vectors = new RandomVector[dimension];
		for (int rowIndex = 0; rowIndex < dimension; rowIndex++) {
			vectors[rowIndex] = new RandomVector(data.getRowVector(rowIndex), new Int2FloatAVLTreeMap());
		}
		RowRandomMatrix matrix = RowRandomMatrix.valueOf(dimension, vectors);
		return matrix;
	}

	@Override
	public void testProduct() throws Exception {
		EnvironmentContext context = Nd4j.getAffinityManager().getClass().getSimpleName().equals("CpuAffinityManager") ? EnvironmentContext.CPU : EnvironmentContext.GPU;
		Future<?> task = context.doTask(() -> {
			int dimension = 10;
			MathMatrix leftMatrix = getRandomMatrix(dimension);
			MathMatrix rightMatrix = getRandomMatrix(dimension);
			MathMatrix dataMatrix = getZeroMatrix(dimension);
			MathMatrix labelMatrix = DenseMatrix.valueOf(dimension, dimension);
			MathVector dataVector = dataMatrix.getRowVector(0);
			MathVector labelVector = labelMatrix.getRowVector(0);

			// 相当于transposeProductThis
			labelMatrix.dotProduct(leftMatrix, false, leftMatrix, true, MathCalculator.SERIAL);
			dataMatrix.dotProduct(leftMatrix, false, leftMatrix, true, MathCalculator.SERIAL);
			Assert.assertTrue(equalMatrix(dataMatrix, labelMatrix));
			dataMatrix.dotProduct(leftMatrix, false, leftMatrix, true, MathCalculator.PARALLEL);
			Assert.assertTrue(equalMatrix(dataMatrix, labelMatrix));

			// 相当于transposeProductThat
			labelMatrix.dotProduct(leftMatrix, false, rightMatrix, true, MathCalculator.SERIAL);
			dataMatrix.dotProduct(leftMatrix, false, rightMatrix, true, MathCalculator.SERIAL);
			Assert.assertTrue(equalMatrix(dataMatrix, labelMatrix));
			dataMatrix.dotProduct(leftMatrix, false, rightMatrix, true, MathCalculator.PARALLEL);
			Assert.assertTrue(equalMatrix(dataMatrix, labelMatrix));

			MathVector leftVector = leftMatrix.getRowVector(RandomUtility.randomInteger(dimension));
			MathVector rightVector = rightMatrix.getRowVector(RandomUtility.randomInteger(dimension));
			labelMatrix.dotProduct(leftVector, rightVector, MathCalculator.SERIAL);
			dataMatrix.dotProduct(leftVector, rightVector, MathCalculator.SERIAL);
			Assert.assertTrue(equalMatrix(dataMatrix, labelMatrix));
			dataMatrix.dotProduct(leftVector, rightVector, MathCalculator.PARALLEL);
			Assert.assertTrue(equalMatrix(dataMatrix, labelMatrix));

			labelVector.dotProduct(leftMatrix, false, rightVector, MathCalculator.SERIAL);
			dataVector.dotProduct(leftMatrix, false, rightVector, MathCalculator.SERIAL);
			Assert.assertTrue(equalVector(dataVector, labelVector));
			dataVector.dotProduct(leftMatrix, false, rightVector, MathCalculator.PARALLEL);
			Assert.assertTrue(equalVector(dataVector, labelVector));

			labelVector.dotProduct(leftVector, rightMatrix, true, MathCalculator.SERIAL);
			dataVector.dotProduct(leftVector, rightMatrix, true, MathCalculator.SERIAL);
			Assert.assertTrue(equalVector(dataVector, labelVector));
			dataVector.dotProduct(leftVector, rightMatrix, true, MathCalculator.PARALLEL);
			Assert.assertTrue(equalVector(dataVector, labelVector));

			// 利用转置乘运算的对称性
			dataMatrix = new SymmetryMatrix(dimension);
			labelMatrix.dotProduct(leftMatrix, false, leftMatrix, true, MathCalculator.SERIAL);
			dataMatrix.dotProduct(leftMatrix, false, leftMatrix, true, MathCalculator.SERIAL);
			Assert.assertTrue(equalMatrix(dataMatrix, labelMatrix));
		});
	}

	@Test
	public void testNotify() {
		AtomicInteger oldSize = new AtomicInteger();
		AtomicInteger newSize = new AtomicInteger();
		int dimension = 10;
		RowRandomMatrix matrix = getRandomMatrix(dimension);
		RandomVector vector = matrix.getRowVector(RandomUtility.randomInteger(dimension));
		int size = vector.getElementSize();
		vector.attachMonitor((iterator, oldElementSize, newElementSize, oldKnownSize, newKnownSize, oldUnknownSize, newUnknownSize) -> {
			oldSize.set(oldElementSize);
			newSize.set(newElementSize);
		});

		vector.setValues(Float.NaN);
		Assert.assertEquals(size, oldSize.get());
		Assert.assertEquals(0, newSize.get());

		vector.setValue(RandomUtility.randomInteger(dimension), 0);
		Assert.assertEquals(0, oldSize.get());
		Assert.assertEquals(1, newSize.get());
	}

}
