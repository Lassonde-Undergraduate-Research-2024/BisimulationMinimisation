package NewAlgorithem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import explicit.DTMCSimple;
import explicit.Distribution;
import explicit.ModelSimple;



/**
 * Decides probabilistic bisimilarity for labelled Markov chains.
 * 
 * @author Eric Ruppert
 * @author Franck van Breugel
 * @author Hiva Karami
 */
public class Bisimulation {

	
	protected static Partition partition;
	protected static int numberOfStates;
	protected static int numberOfLabels;
	/**
	 * Decides probabilistic bisimilarity for the given labelled Markov chain.
	 * 
	 * @param chain a labelled Markov chain
	 * @return a two dimensional boolean array that captures for each state pair whether
	 * the states are probabilistic bisimilar
	 */
	public static void decide(DTMCSimple<Double> dtmc, List<BitSet> propBSs) {
		
		numberOfStates = dtmc.getNumStates(); 
		numberOfLabels = propBSs.size();

		/*
		 * initial partition: states are in the same block iff they have the same labelling
		 */
		final List<BitSet> initial = new ArrayList<BitSet>();
		
		final BitSet one = (BitSet) propBSs.get(0).clone();
		final BitSet notOne = (BitSet) one.clone();
		notOne.flip(0, numberOfStates);
		if (!one.isEmpty()) { // ADDED
			initial.add(one);
		}
		if (!notOne.isEmpty()) { // ADDED
			initial.add(notOne);
		}
		for (int l = 1; l < numberOfLabels; l++) {
			BitSet set = propBSs.get(l);
			int size = initial.size();
			for (int i = 0; i < size; i++) {
				BitSet intersection = (BitSet) initial.get(i).clone();
				BitSet difference = (BitSet) intersection.clone();
				difference.andNot(set);
				intersection.and(set);
				if (!intersection.isEmpty()) {
					initial.set(i, intersection);
					if (!difference.isEmpty()) {
						initial.add(difference);
					}
				}
			}
		}

		int numberOfBlocks = initial.size();

		// partition
		partition = new Partition(numberOfStates, initial);
		
		// first potential splitter
		int first = 0; 

		// last potential splitter
		int last = numberOfBlocks - 1;

		/*
		 * States that transition to potential splitters and their signatures
		 */
		final SignatureList toCheck = new SignatureList();

		/*
		 * for all 0 <= s < numberOfStates : hasBeenChecked[s] = state s has been checked in the current round 
		 */
		final boolean[] hasBeenChecked = new boolean[numberOfStates];

		/*
		 * for all 0 <= s < numberOfStates : index[state] = index of state in toCheck
		 */
		final int[] index = new int[numberOfStates];

		/*
		 * for all 0 <= target < numberOfStates : predecessors[target] contains (source, probability)
		 * if source transitions to target with probability. 
		 */
		final List<Edge>[] predecessors = new List[numberOfStates]; // use LinkedHashMap?
		for (int target = 0; target < numberOfStates; target++) {
			predecessors[target] = new ArrayList<Edge>();
		}		
		for (int source = 0; source < numberOfStates; source++) {
			
			Iterator<Map.Entry<Integer, Double>> iter = dtmc.getTransitionsIterator(source);
			while (iter.hasNext()) {
				Map.Entry<Integer, Double> e = iter.next();
				predecessors[e.getKey()].add(new Edge(source, e.getValue()));
			}
		}

		while (first <= last) {
			
			Arrays.fill(hasBeenChecked, false);
			toCheck.clearisFirst();
			toCheck.clear();
			for (int block = first; block <= last; block++) { // loop through new blocks created in previous round
				for (int target : partition.getStates(block)) { // loop through states of block permutation[block]
					for (Edge edge : predecessors[target]) { // loop through incoming transitions of state
						int source = edge.getSource();
						double weight = edge.getWeight();
						if (!hasBeenChecked[source]) { // first time we are visiting source state in this round; create a new signature for it
							hasBeenChecked[source] = true;
							index[source] = toCheck.size();
							toCheck.add(new Signature(source, partition.getBlock(source), block, weight), false);
						} else { // already visited source in this round; so simply add edge.weight to weight of block b in e.source's signature
							toCheck.get(index[source]).add(block, weight);
						}
					}
				}
			}
			//System.out.println("The signitureList:");
			//System.out.println(toCheck.toString());
			toCheck.quicksort(0, toCheck.size()-1, 0);
			//System.out.println("The signitureList atfer threeWayQuickSort:");
			//System.out.println(toCheck.toString());
			//System.out.println("//////////////////////////////");
			// split the blocks
			first = last + 1;

			int numberToCheck = toCheck.size();
			int numberChecked = 0;
			while (numberChecked < numberToCheck) {
				int maxSize = 0;
				int maxBlock = 0;
				
				int oldBlock = toCheck.get(numberChecked).getOldBlock();
				while (numberChecked < numberToCheck && toCheck.get(numberChecked).getOldBlock() == oldBlock) { // out of bounds
					ArrayList<Integer> newBlock = new ArrayList<Integer>();
					do { // loop through nodes with same signature, adding them to new block
						int state = toCheck.get(numberChecked).getState(); 
						newBlock.add(state);
						numberChecked++;
					} while (numberChecked < numberToCheck && !toCheck.isFirst(numberChecked));

					if (newBlock.size() != partition.getStates(oldBlock).size()) { // FvB: nothing needs to be done when newBlock is the same as permutation[oldBlock]
						// create new block
						partition.refine(oldBlock, newBlock);
						last++;
						int size = newBlock.size();
						if (size > maxSize) { // keep track of largest sub-block of split block
							maxBlock = last;
							maxSize = size;
						}
					}
				}

				if (maxSize > partition.getStates(oldBlock).size()) {
					partition.swap(maxBlock, oldBlock);
				}
			}
		}

		return;
	}
	
	
	
	public static boolean[][] bisimilar(DTMCSimple<Double> dtmc, List<BitSet> propBSs){
		
		//initialisePartitionInfo(dtmc, propBSs); 
		decide(dtmc, propBSs);
		final boolean[][] bisimilar = new boolean[numberOfStates][numberOfStates];
		for (List<Integer> block : partition) {
			for (Integer s : block) {
				for (Integer t : block) {
					bisimilar[s][t] = true;
				}
			}
		}
		
		return bisimilar;
	}
	
	public static DTMCSimple<Double> minimiseDTMC(DTMCSimple<Double> dtmc, List<BitSet> propBSs){
		
		decide(dtmc, propBSs);
		
		
		int numBlocks = partition.size();
		DTMCSimple<Double> dtmcNew = new DTMCSimple<Double>(numBlocks);

		
		int[] blockOf = new int[numberOfStates];	
		int cnt = 0;
		for (List<Integer> block : partition) {
			for (Integer s : block) {
				blockOf[s] = cnt;
			}
			cnt++;
		}
		
		for (int source = 0; source < numberOfStates; source++) {
			Iterator<Map.Entry<Integer, Double>> iter = dtmc.getTransitionsIterator(source);
			while (iter.hasNext()) {
				Map.Entry<Integer, Double> e = iter.next();
				dtmcNew.addToProbability(blockOf[source], blockOf[e.getKey()], e.getValue());
				
			}
		}
		
		return dtmcNew;
	}
	
	
}
