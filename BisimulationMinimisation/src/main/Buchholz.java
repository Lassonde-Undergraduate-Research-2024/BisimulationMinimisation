package main;

import static main.Constants.ACCURACY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


import explicit.CTMCModelChecker;
import explicit.CTMCSimple;
import explicit.DTMCSimple;
import explicit.ModelCheckerResult;
import explicit.ModelSimple;
import explicit.StateModelChecker;
import prism.PrismComponent;
import prism.PrismException;


/**
 * Decides which states of a labelled Markov chain are probabilistic bisimilar.  The implementation
 * is based on the bisimilarity algorithm from the paper "Efficient computation of
 * equivalent and reduced representations for stochastic automata" by Peter Buchholz.
 *
 * 
 */
public class Buchholz extends PrismComponent{

	/**
	 * A class to represent the equivalence classes during the split method.
	 */


	public Buchholz() {
		// TODO Auto-generated constructor stub
	}

	protected static int[] partition;
	
	
	private static class EquivalenceClass {
		private boolean initialized;
		private double value;
		private int next;

		/**
		 * Initializes this equivalence class as uninitialized (no state belonging to this equivalence
		 * class has been found yet).
		 */
		public EquivalenceClass() {
			this.initialized = false;
			this.value = 0;
			this.next = 0;
		}
	}
	

	/**
	 * Decides probabilistic bisimilarity for the given labelled Markov chain.
	 *
	 * @param chain a labelled Markov chain
	 * @return a boolean array that captures for each state pair whether
	 * the states are probabilistic bisimilar:
	 * bisimilar[s * chain.getNumberOfStates() + t] == states s and t are probabilistic bisimilar
	 */
	public static boolean[] decide(DTMCSimple<Double> dtmc, List<BitSet> propBSs) {
	
		initialisePartitionInfo(dtmc, propBSs); //-> this will give partition[]
		
		
		int NumberOfStates = dtmc.getNumStates();
		List<Integer> indices = new ArrayList<Integer>();
		for (int state = 0; state < NumberOfStates; state++) {	
			int label = partition[state];
			if (!indices.contains(label)) {
				indices.add(label);
			}
		}

		int numberOfEquivalenceClasses = indices.size(); // number of equivalence classes
		List<Set<Integer>> classes = new ArrayList<Set<Integer>>(); // equivalence classes
		TreeSet<Integer> splitters = new TreeSet<Integer>(); // potential splitters
		int[] clazzOf = new int[NumberOfStates]; // for each state ID, the index of its equivalence class
		for (int clazz = 0; clazz < numberOfEquivalenceClasses; clazz++) {
			classes.add(new HashSet<Integer>());
			splitters.add(clazz);
		}
		
		for (int state = 0; state < NumberOfStates; state++) {
			int label = partition[state];
			int index = indices.indexOf(label);
			clazzOf[state] = index;
			classes.get(index).add(state);
		}

		double[] values = new double[NumberOfStates];
		while (!splitters.isEmpty()) {
			List<EquivalenceClass> split = new ArrayList<EquivalenceClass>();
			for (int clazz = 0; clazz < numberOfEquivalenceClasses; clazz++) {
				split.add(new EquivalenceClass());
			}
			int splitter = splitters.first();
			splitters.remove(splitter);
			
			// computing values
			Arrays.fill(values, 0);
			for (int target : classes.get(splitter)) {
				for (int source = 0; source < NumberOfStates; source++) { //source -> target 
					values[source] += dtmc.getProbability(source, target);
				}
			}
			

			for (int state = 0; state < NumberOfStates; state++) {
				int clazz = clazzOf[state];
				if (!split.get(clazz).initialized) {
					classes.set(clazz, new HashSet<Integer>());
					classes.get(clazz).add(state);
					split.get(clazz).initialized = true;
					split.get(clazz).value = values[state];
				} else {
					if (Math.abs(split.get(clazz).value - values[state]) >= ACCURACY && split.get(clazz).next == 0) {
						splitters.add(clazz);
					}
					while (Math.abs(split.get(clazz).value - values[state]) >= ACCURACY && split.get(clazz).next != 0) {
						clazz = split.get(clazz).next;
					}
					if (Math.abs(split.get(clazz).value - values[state]) < ACCURACY) {
						clazzOf[state] = clazz;
						classes.get(clazz).add(state);
					} else {
						splitters.add(numberOfEquivalenceClasses);
						clazzOf[state] = numberOfEquivalenceClasses;
						split.get(clazz).next = numberOfEquivalenceClasses;
						split.add(new EquivalenceClass());
						split.get(numberOfEquivalenceClasses).initialized = true;
						split.get(numberOfEquivalenceClasses).value = values[state];
						classes.add(new HashSet<Integer>());
						classes.get(numberOfEquivalenceClasses).add(state);
						numberOfEquivalenceClasses++;
					}
				}
			}
		}
		
		/*
		for (Set<Integer> clazz : classes) {
			System.out.println("Class : ");
			for (Integer t : clazz) {
				
				System.out.print(t + " ");
			}
			System.out.println('\n');
		}
		*/
		boolean[] bisimilar = new boolean[NumberOfStates * NumberOfStates];
		for (Set<Integer> clazz : classes) {
			for (Integer s : clazz) {
				for (Integer t : clazz) {
					bisimilar[s * NumberOfStates + t] = true;
				}
			}
		}
		return bisimilar;
	}
	
	
	
	private static void initialisePartitionInfo(ModelSimple<Double> model, List<BitSet> propBSs)
	{
		BitSet bs1, bs0;
		int numStates = model.getNumStates();

		// Compute all non-empty combinations of propositions
		List<BitSet> all = new ArrayList<BitSet>();
		bs1 = (BitSet) propBSs.get(0).clone();
		bs0 = (BitSet) bs1.clone();
		bs0.flip(0, numStates);
		all.add(bs1);
		all.add(bs0);
		int n = propBSs.size();
		for (int i = 1; i < n; i++) {
			BitSet bs = propBSs.get(i);
			int m = all.size();
			for (int j = 0; j < m; j++) {
				bs1 = all.get(j);
				bs0 = (BitSet) bs1.clone();
 				bs0.andNot(bs);
				bs1.and(bs);
				if (bs1.isEmpty()) {
					all.set(j, bs0);
				} else {
					if (!bs0.isEmpty())
						all.add(bs0);
				}
			}
		}

		partition = new int[numStates+4];
		// Construct initial partition
		int numBlocks = all.size();
		for (int j = 0; j < numBlocks; j++) {
			BitSet bs = all.get(j);
			for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
				//System.out.println(i);
				partition[i] = j;
			}
		}
	}
	
	

	
}