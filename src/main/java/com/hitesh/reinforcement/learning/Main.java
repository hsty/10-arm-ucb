package com.hitesh.reinforcement.learning;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import org.apache.commons.math3.analysis.function.Sqrt;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends ApplicationFrame {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public Main(String title) {
		super(title);
		// Charting stuff, not an expert, most of this was picked directly from
		// stackoverflow
		// See:
		// 1.https://stackoverflow.com/questions/36754524/how-to-plot-a-line-graph-in-java-using-dataset-from-a-text-file
		// 2.https://stackoverflow.com/questions/2780493/how-to-wrap-category-labels-in-jfreechart

		final XYSeries dataset2 = createDataset(0.1); // Epsilon = 0.1, u = 0
		final XYSeries dataset3 = createUCBDataset(2); // Epsilon = 0.0, u = 2

		XYSeriesCollection collection = new XYSeriesCollection();

		collection.addSeries(dataset2);
		collection.addSeries(dataset3);

		final JFreeChart chart = createChart(collection);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(560, 370));
		chartPanel.setMouseZoomable(true, false);
		setContentPane(chartPanel);
	}

	private XYSeries createUCBDataset(int u) {
		NormalDistribution normal = new NormalDistribution();
		Random rand = new Random();

		Sqrt sqrt = new Sqrt();

		double[] Q_a = new double[10];
		int[] N_a = new int[10];
		double[] q_a = new double[10];
		double[][] rewardsMulRuns = new double[2000][1000]; // We save the actual values in case required later
		double[] avgRewards = new double[1000]; // This stores the avg values across 2000 runs

		// 2000 Runs
		for (int m = 0; m < 2000; m++) {

			logger.info("Initializing the arrays");
			for (int i = 0; i < 10; i++) {
				Q_a[i] = 0.0;
				N_a[i] = 0;
				q_a[i] = normal.sample();
			}

			for (int i = 0; i < 10; i++) {
				logger.info("Assigned q_a[{}]={}", i, q_a[i]);
			}
			
			//Initializing all values, this just makes all subsequent calculations easier
			
			for(int k = 0; k < 10; k++) {
				N_a[k] = N_a[k] +1;
				double reward = bandit(k, q_a);
				double newVal = Q_a[k] + (reward - Q_a[k]) / N_a[k];
				Q_a[k] = newVal;
			}
			

			int action;

			logger.info("Initialization complete");

			// 1000 Iterations
			for (int j = 1; j < 1000; j++) {

				// For UCB lets calculate the values using the Upper Confidence method
				double[] ucb = new double[10];

				for (int r = 0; r < 10; r++) {
					ucb[r] = Q_a[r] + u * sqrt.value(Math.log(j) / N_a[r]);
				}

				action = argmax(ucb, rand);

				// Commented the log below as it was polluting the log stream

//				logger.info(
//						"Calling bandit to get the reward, using action: {}, and normal distribution using q_a[{}]={} "
//								+ "as mean and variance as 1",
//						action, action, q_a[action]);

				// Get the reward for this particular action
				double reward = bandit(action, q_a);

				// Store in an array to use later
				rewardsMulRuns[m][j] = reward;

				// Update Q_a with the latest value
				logger.info("Reward:{}", reward);
				logger.info("Before update, Bandit: {}, {}, {}", action, N_a[action], Q_a[action]);
				N_a[action] = N_a[action] + 1;
				double newVal = Q_a[action] + (reward - Q_a[action]) / N_a[action];
				Q_a[action] = newVal;
				logger.info("After update, Bandit:{}, {}, {}", action, N_a[action], Q_a[action]);
			}

			for (int k = 0; k < 10; k++) {
				logger.info("End of a Run, Assigned values: N_a[{}]={}, Q_a[{}]={} ", k, N_a[k], k, Q_a[k]);
			}
		}

		XYSeries dataset = new XYSeries("u: " + u);

		// Iterate over all the 1000 runs
		for (int i = 0; i < 1000; i++) {
			double sum = 0.0;

			// Sum the value of rewards for this particular instance for each of the runs
			for (int j = 0; j < 2000; j++) {
				sum = sum + rewardsMulRuns[j][i];
			}
			avgRewards[i] = sum / 2000;

			dataset.add(i, avgRewards[i]);

		}
		return dataset;

	}

	private JFreeChart createChart(XYSeriesCollection collection) {
		return ChartFactory.createXYLineChart("Rewards", "Runs", "Rewards", collection);
	}

	private XYSeries createDataset(double e) {

		NormalDistribution normal = new NormalDistribution();
		Random rand = new Random();

		double[] Q_a = new double[10];
		int[] N_a = new int[10];
		double[] q_a = new double[10];
		double[][] rewardsMulRuns = new double[2000][1000]; // We save the actual values in case required later
		double[] avgRewards = new double[1000]; // This stores the avg values across 2000 runs

		// 2000 Runs
		for (int m = 0; m < 2000; m++) {

			logger.info("Initializing the arrays");
			for (int i = 0; i < 10; i++) {
				Q_a[i] = 0.0;
				N_a[i] = 0;
				q_a[i] = normal.sample();
			}

			for (int i = 0; i < 10; i++) {
				logger.info("Assigned q_a[{}]={}", i, q_a[i]);
			}

			int action;

			logger.info("Initialization complete");

			// 1000 Iterations
			for (int j = 0; j < 1000; j++) {

				// If this is Epsilon-Greedy lets take actions as usual
				if (rand.nextFloat() <= e) {
					logger.info("Taking action randomly");
					action = rand.nextInt(0, 10);
					logger.info("Epsilon action: {}", action);
				} else {
					logger.info("Selecting next action based on the most value, ergo \"greedy\"");
					action = argmax(Q_a, rand);
					logger.info("Greedy Action: {}", action);
				}

				// Commented the log below as it was polluting the log stream

//				logger.info(
//						"Calling bandit to get the reward, using action: {}, and normal distribution using q_a[{}]={} "
//								+ "as mean and variance as 1",
//						action, action, q_a[action]);

				// Get the reward for this particular action
				double reward = bandit(action, q_a);

				// Store in an array to use later
				rewardsMulRuns[m][j] = reward;

				// Update Q_a with the latest value
				logger.info("Reward:{}", reward);
				logger.info("Before update, Bandit: {}, {}, {}", action, N_a[action], Q_a[action]);
				N_a[action] = N_a[action] + 1;
				double newVal = Q_a[action] + (reward - Q_a[action]) / N_a[action];
				Q_a[action] = newVal;
				logger.info("After update, Bandit:{}, {}, {}", action, N_a[action], Q_a[action]);
			}

			for (int k = 0; k < 10; k++) {
				logger.info("End of a Run, Assigned values: N_a[{}]={}, Q_a[{}]={} ", k, N_a[k], k, Q_a[k]);
			}
		}

		XYSeries dataset = new XYSeries("ε: " + e);

		// Iterate over all the 1000 runs
		for (int i = 0; i < 1000; i++) {
			double sum = 0.0;

			// Sum the value of rewards for this particular instance for each of the runs
			for (int j = 0; j < 2000; j++) {
				sum = sum + rewardsMulRuns[j][i];
			}
			avgRewards[i] = sum / 2000;

			dataset.add(i, avgRewards[i]);

		}
		return dataset;
	}

	public static void main(String[] args) {

		final String title = "Rewards Avg Runs";
		final Main demo = new Main(title);
		demo.pack();
		demo.setSize(800, 600);

		demo.setLocationRelativeTo(null);
		demo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		demo.setVisible(true);
	}

	private static double bandit(int action, double[] q_a) {
		// Pick a reward using q_a of action as mean and 1 as variance
		// This will tend to conglomorate around q_a with the largest reward
		NormalDistribution qNormal = new NormalDistribution(q_a[action], 1);
		return qNormal.sample();
	}

	private static int argmax(double[] Q, Random rand) {

		// What we are trying to achieve is that we find the Max Value
		// Iterate through all indices and check which ones have this said Max value
		// Then pick a random action from those

		double maxQ = Double.NEGATIVE_INFINITY;

		List<Integer> maxIndices = new ArrayList<>();

		for (int j = 0; j < 10; j++) {
			if (Q[j] > maxQ) {
				maxQ = Q[j];
				maxIndices.clear();
				maxIndices.add(j);
			} else if (Q[j] == maxQ) {
				maxIndices.add(j);
			}
		}

		int idx = maxIndices.get(rand.nextInt(maxIndices.size()));

		return idx;

	}

}
