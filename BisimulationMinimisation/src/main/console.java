package main;

import java.util.BitSet;
import java.util.List;
import explicit.DTMCSimple;

public class console {

	public static void main(String[] args) {
		
		int numberOfStates = 60000;
		int numberOfLabels = 3;
		System.out.println(numberOfStates + " " + numberOfLabels);
		DTMCSimple<Double> dtmcSimple = RandomModelGenerator.GenerateModel(numberOfStates);
		List<BitSet> propBSs = RandomModelGenerator.Generatelabels(numberOfStates, numberOfLabels);
		long startTime, endTime;
		
		// Measure time for NewAlgorithem.Bisimulation.minimiseDTMC
        startTime = System.currentTimeMillis();
        NewAlgorithem.Bisimulation.minimiseDTMC(dtmcSimple, propBSs);
        endTime = System.currentTimeMillis();
        System.out.println("Time taken for NewAlgorithem: " + (endTime - startTime) + " milliseconds");
		
        
     // Measure time for PrismBisimulation
        startTime = System.currentTimeMillis();
        PrismBisimulation.minimiseDTMC(dtmcSimple, propBSs);
        endTime = System.currentTimeMillis();
        System.out.println("Time taken for PrismBisimulation: " + (endTime - startTime) + " milliseconds");
        
        
//        // Measure time for Buchholz.minimiseDTMC
//        startTime = System.currentTimeMillis();
//        Buchholz.minimiseDTMC(dtmcSimple, propBSs);
//        endTime = System.currentTimeMillis();
//        System.out.println("Time taken for Buchholz: " + (endTime - startTime) + " milliseconds");
//        
        
//     // Measure time for ZeroDerisavi
//        startTime = System.currentTimeMillis();
//        ZeroDerisavi.minimiseDTMC(dtmcSimple, propBSs);
//        endTime = System.currentTimeMillis();
//        System.out.println("Time taken for ZeroDerisavi: " + (endTime - startTime) + " milliseconds");
//        
//        
//     // Measure time for ZeroDerisaviRedBlack
//        startTime = System.currentTimeMillis();
//        ZeroDerisaviRedBlack.minimiseDTMC(dtmcSimple, propBSs);
//        endTime = System.currentTimeMillis();
//        System.out.println("Time taken for ZeroDerisaviRedBlack: " + (endTime - startTime) + " milliseconds");
//        
//     // Measure time for Primitive_Ints
//        startTime = System.currentTimeMillis();
//        Primitive_Ints Ints = new Primitive_Ints();
//      	Ints.minimiseDTMC(dtmcSimple, propBSs);
//        endTime = System.currentTimeMillis();
//        System.out.println("Time taken for Primitive_Ints: " + (endTime - startTime) + " milliseconds");
////        
        
        //System.out.println("minimized from" + 10000 + "to" + );
        
        
        
	}

}
