package main;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import explicit.DTMCSimple;
import explicit.StateModelChecker;
import prism.PrismException;

public class RandomModelGenerator {

	public static final int MAXnumberOfStates = (int) 1000;
	public static final int MAXnumberOfLabels = 5;
	public static void main(String[] args) {
			
		Random random = new Random();
		int numberOfStates = random.nextInt(MAXnumberOfStates) + 1;
		int numberOfLabels = random.nextInt(MAXnumberOfLabels) + 1;
		//System.out.println(numberOfStates + " " + numberOfLabels);
		

		DTMCSimple<Double> dtmcSimple = new DTMCSimple<Double>(numberOfStates);

		//generate transitions
		//System.out.println("Teansitions");
		double threshold = 2 * Math.log(numberOfStates) / numberOfStates;
		for (int source = 0; source < numberOfStates; source++) {
			int outgoing = 0; // number of outgoing transitions of source
			
			
			double[] probability = new double[numberOfStates];
			
			for (int target = 0; target < numberOfStates; target++) {
				if (random.nextDouble() < threshold) {
					probability[target] = 1;
					outgoing++;
				}
			}
			if (outgoing > 0) {
				for (int target = 0; target < numberOfStates; target++) {
					probability[target] /= outgoing;
					if(probability[target]/outgoing > 0) {
						//System.out.println(source + " " + target + " " + probability[target]/outgoing);
						dtmcSimple.setProbability(source, target, probability[target]/outgoing);						
					}
					
				}
			} else {
				dtmcSimple.setProbability(source, source, 1.0);
				//System.out.println(source + " " + source + " " + 1);
			}
		}
		
		// generate labels
		//System.out.println("Labels");
		List<BitSet> propBSs = new ArrayList<>(numberOfStates);
		for(int s = 0; s < numberOfStates; s++) {
			
			BitSet bitSet = new BitSet(numberOfLabels);
			propBSs.add(bitSet);
			//System.out.print(s + ": ");
			int mask = random.nextInt((1<<numberOfLabels));
			
			for(int i = 0; i < numberOfLabels; i++) {
				if(((mask >> i)&1) == 1) {
					propBSs.get(s).set(i);
				//	System.out.print(i + " ");
				}
			}
			
			//System.out.print('\n');
		}
		
		
		boolean[] res = Buchholz.decide(dtmcSimple, propBSs);	
		
		/*
		System.out.println("Buchholz:");
		for(int i = 0; i < numberOfStates; i++) {
			for(int j = 0; j < numberOfStates; j++) {
				if(res[i*numberOfStates + j]) {
					System.out.print(1 + " ");						
				}else {
					System.out.print(0 + " ");
				}
				
			}
			System.out.println('\n');
		}//*/
		
		
		boolean[] ZeroDerisaviRes = ZeroDerisavi.decide(dtmcSimple, propBSs);	
		/*
		System.out.println("ZeroDerisavi:");
		for(int i = 0; i < numberOfStates; i++) {
			for(int j = 0; j < numberOfStates; j++) {
				if(ZeroDerisaviRes[i*numberOfStates + j]) {
					System.out.print(1 + " ");						
				}else {
					System.out.print(0 + " ");
				}
				
			}
			System.out.println('\n');
		}
		//*/
		
		
		///// compare the result
		for(int i = 0; i < numberOfStates; i++) {
			for(int j = 0; j < numberOfStates; j++) {
				if(ZeroDerisaviRes[i*numberOfStates + j] != res[i*numberOfStates + j]) {
					System.out.print("Erorr !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					System.exit(0);
				}
				
			}
		}
		
		System.out.print("okay");
	}

}
