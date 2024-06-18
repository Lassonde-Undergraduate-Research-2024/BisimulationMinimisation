package main;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import explicit.DTMCSimple;
import explicit.StateModelChecker;
import prism.PrismException;

public class console {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(args[0]);
		System.out.println(args[1]);
		DTMCSimple<Double> dtmcSimple;
		Map<String, BitSet> labels;
		
		//*
		try {
			dtmcSimple = new DTMCSimple<Double>();
			dtmcSimple.buildFromPrismExplicit(args[0]);
			dtmcSimple.addInitialState(0);
			labels = StateModelChecker.loadLabelsFile(args[1]);
			//List<String> propNames = new ArrayList<>(labels.keySet());
			List<BitSet> propBSs = new ArrayList<>(labels.values());
			boolean[] res = Buchholz.decide(dtmcSimple, propBSs);
			
			for(int i = 0; i < dtmcSimple.getNumStates(); i++) {
				for(int j = 0; j < dtmcSimple.getNumStates(); j++) {
					if(res[i*dtmcSimple.getNumStates() + j]) {
						System.out.print(1 + " ");						
					}else {
						System.out.print(0 + " ");
					}
					
				}
				System.out.println('\n');
			}
			
			
			//boolean[] res = bz.decide(dtmcSimple, propNames, propBSs);
			
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//*/
		
	}

}