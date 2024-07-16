package main;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import explicit.Bisimulation;
import explicit.DTMCSimple;
import explicit.StateModelChecker;
import prism.PrismException;

public class RandomModelGenerator {

	public static final int MAXnumberOfStates = (int) 1000;
	public static final int MAXnumberOfLabels = 2;

	public static DTMCSimple<Double> GenerateModel(int numberOfStates){

		Random random = new Random();
		DTMCSimple<Double> dtmcSimple = new DTMCSimple<Double>(numberOfStates);

		double threshold = 2 * Math.log(numberOfStates) / numberOfStates;
		for (int source = 0; source < numberOfStates; source++) {
			double outgoing = 0; // number of outgoing transitions of source


			double[] probability = new double[numberOfStates];

			for (int target = 0; target < numberOfStates; target++) {
				if (random.nextDouble() < threshold) {
					probability[target] = 1;
					outgoing++;
				}
			}
			if (outgoing > 0) {
				for (int target = 0; target < numberOfStates; target++) {

					if(probability[target]/outgoing > 0.0) {
						//System.out.println(source + " " + target + " " + probability[target]/outgoing);
						dtmcSimple.setProbability(source, target, probability[target]/outgoing);						
					}

				}
			} else {
				dtmcSimple.setProbability(source, source, 1.0);
				//	System.out.println(source + " " + source + " " + 1);
			}
		}
		return dtmcSimple;
	}

	public static List<BitSet> Generatelabels(int numberOfStates, int numberOfLabels){
		Random random = new Random();
		List<BitSet> propBSs = new ArrayList<>();
		for(int s = 0; s < numberOfLabels; s++) {
			BitSet bitSet = new BitSet(numberOfStates);
			propBSs.add(bitSet);
		}

		for(int s = 0; s < numberOfStates; s++) {
			int mask = random.nextInt((1<<numberOfLabels));
			for(int i = 0; i < numberOfLabels; i++) {
				if(((mask >> i)&1) == 1) {
					propBSs.get(i).set(s, true);
				}else {
					propBSs.get(i).set(s, false);	
				}
			}

			//propBSs.add(bitSet);
		}

		System.out.println("Labels");
		for(int i = 0; i < numberOfLabels; i++) {
			System.out.print(i + ": ");
			for(int j = 0; j < numberOfStates; j++) {
				//System.out.print(i+ " "+ j +" ::");
				if(propBSs.get(i).get(j))
					System.out.print(1);
				else
					System.out.print(0);
			}
			System.out.println(" ");
		}

		return propBSs;
	}




	private static void RandomModel() {
		System.out.println("--------------------------------------------");

		Random random = new Random();
		int numberOfStates = random.nextInt(MAXnumberOfStates) + 1;
		int numberOfLabels = random.nextInt(MAXnumberOfLabels) + 1;
		System.out.println(numberOfStates + " " + numberOfLabels);
		DTMCSimple<Double> dtmcSimple = GenerateModel(numberOfStates);
		List<BitSet> propBSs = Generatelabels(numberOfStates, numberOfLabels);


		Primitive_Ints Ints = new Primitive_Ints();
		boolean[] Primitive = Ints.bisimilar(dtmcSimple, propBSs);
		boolean[] BuchholzRes = Buchholz.bisimilar(dtmcSimple, propBSs);	
		boolean[] ZeroDerisaviRes = ZeroDerisavi.bisimilar(dtmcSimple, propBSs);	
		boolean[] ZeroDerisavisRedBlackRes = ZeroDerisaviRedBlack.bisimilar(dtmcSimple, propBSs);
		boolean[] PrismBisim = PrismBisimulation.getResult(dtmcSimple, propBSs);

		/*
		System.out.println("Primitive");
		for(int i = 0; i < numberOfStates; i++) {
			for(int j = 0; j < numberOfStates; j++) {
				if(Primitive[i*numberOfStates + j]) {
					System.out.print(1 + " ");						
				}else {
					System.out.print(0 + " ");
				}

			}
			System.out.println('\n');
		}
		//*/
	

		/*
		System.out.println("PrismBisim");
		for(int i = 0; i < numberOfStates; i++) {
			for(int j = 0; j < numberOfStates; j++) {
				if(PrismBisim[i*numberOfStates + j]) {
					System.out.print(1 + " ");						
				}else {
					System.out.print(0 + " ");
				}

			}
			System.out.println('\n');
		}
		//*/

		/*
		System.out.println("Buchholz:");
		for(int i = 0; i < numberOfStates; i++) {
			for(int j = 0; j < numberOfStates; j++) {
				if(BuchholzRes[i*numberOfStates + j]) {
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
				int x = i*numberOfStates+j;
				if(BuchholzRes[x] != BuchholzRes[x] || 
					BuchholzRes[x] != ZeroDerisavisRedBlackRes[x]||
					BuchholzRes[x] != ZeroDerisaviRes[x] ||
					BuchholzRes[x] != Primitive[x]) {


					System.out.println("Erorr!! " + i + " " + j + " " + PrismBisim[i*numberOfStates + j] + " " + BuchholzRes[i*numberOfStates + j]);
					//System.out.println(" " + PrismBisimulation[i*numberOfStates + j] + ' ' + ZeroDerisaviRes[i*numberOfStates + j]);
					System.out.println(dtmcSimple.toString());

					System.exit(0);
				}

			}
		}

		System.out.println("okay");



		//DTMCSimple<Double> newDtmcSimple = Buchholz.minimiseDTMC(dtmcSimple, propBSs);
		//System.out.println(newDtmcSimple.toString());

		//DTMCSimple<Double> newDtmcSimple2 = ZeroDerisavi.minimiseDTMC(dtmcSimple, propBSs);
		//System.out.println(newDtmcSimple2.toString());
	}


	public static void main(String[] args) {

		for(int i = 0; i < 10000; i++)
		{
			RandomModel();
			//System.out.println(i);
		}
	}

}