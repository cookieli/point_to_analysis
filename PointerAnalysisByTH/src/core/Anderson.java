package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import soot.Local;
import soot.jimple.InstanceFieldRef;

class AssignConstraint {
	Local from, to;
	AssignConstraint(Local from, Local to) {
		this.from = from;
		this.to = to;
	}
}

class NewConstraint {
	Local to;
	int allocId;
	NewConstraint(int allocId, Local to) {
		this.allocId = allocId;
		this.to = to;
	}
}
/*class FieldAssignConstraint {
	InstanceFieldRef from;
	Local to;
	FieldAssignConstraint(InstanceFieldRef from, Local to){
		this.from = from;
		this.to = to;
	}
}*/

public class Anderson {
	private List<AssignConstraint> assignConstraintList = new ArrayList<AssignConstraint>();
	private List<NewConstraint> newConstraintList = new ArrayList<NewConstraint>();
	//private List<FieldAssignConstraint> FieldAssignConstraintList = new ArrayList<>();
	public List<Local> assignParameters = new ArrayList<>();
	Map<Local, Local> assignLocalToReal = new HashMap<>();
	Map<Local, Local> tempToLocal = new HashMap<>();
	//some data structure about assign method
    //now we need data structure to init method

	Map<Local, TreeSet<Integer>> pts = new HashMap<Local, TreeSet<Integer>>();
	void addAssignConstraint(Local from, Local to) {
		assignConstraintList.add(new AssignConstraint(from, to));
	}
	//void addFieldAssignConstraint(InstanceFieldRef from, Local to) {FieldAssignConstraintList.add(new FieldAssignConstraint(from, to));}
	void addNewConstraint(int alloc, Local to){
		newConstraintList.add(new NewConstraint(alloc, to));
	}
	void mapParametersWithReal(Local local, int parameter_id){
		assignLocalToReal.put(local, assignParameters.get(parameter_id));
	}
	void setAssignParameters(Local parameter){
		this.assignParameters.add(parameter);
	}



	void run() {
		for (NewConstraint nc : newConstraintList) {
			if (!pts.containsKey(nc.to)) {
				pts.put(nc.to, new TreeSet<Integer>());
			}
			pts.get(nc.to).add(nc.allocId);
		}
		for (boolean flag = true; flag; ) {
			flag = false;
			for (AssignConstraint ac : assignConstraintList) {
				if (!pts.containsKey(ac.from)) {
					continue;
				}	
				if (!pts.containsKey(ac.to)) {
					pts.put(ac.to, new TreeSet<Integer>());
				}
				if (pts.get(ac.to).addAll(pts.get(ac.from))) {
					flag = true;
				}
			}
		}
		/*for (boolean flag = true; flag; ){

		}*/
	}
	TreeSet<Integer> getPointsToSet(Local local) {
		return pts.get(local);
	}
	
}
