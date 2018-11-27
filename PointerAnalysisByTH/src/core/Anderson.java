package core;

import java.util.*;

import soot.Local;
import soot.SootField;
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
class ClassFields {
	Local name;
	List<SootField> fields;
	List<HashSet<Local>> fieldsPointTo;
	public ClassFields() {}
	public ClassFields(Local name){
		this.name = name;
		this.fields = new ArrayList<>();
		this.fieldsPointTo = new ArrayList<>();
	}
	public boolean contains(SootField field){
		return fields.contains(field);
	}
	void addFieldPointTo(SootField field, Local pointTo){
		if(! fields.contains(field)){
			fields.add(field);
			HashSet<Local> temp = new HashSet<>();
			temp.add(pointTo);
			fieldsPointTo.add(temp);
		} else {
			int idx = fields.indexOf(field);
			fieldsPointTo.get(idx).add(pointTo);
		}
	}
	HashSet<Local> getPointTo(SootField field){
		int idx = fields.indexOf(field);
		return fieldsPointTo.get(idx);
	}
}

class AllClassesWithFields {
	Map<Local, ClassFields> allClasses;
	AllClassesWithFields(){
		allClasses = new HashMap<>();
	}
	public void setClasseAndField(Local c, SootField field, Local pointed){
		if(allClasses.containsKey(c)){
			allClasses.get(c).addFieldPointTo(field, pointed);
		} else {
			ClassFields s = new ClassFields(c);
			s.addFieldPointTo(field, pointed);
			allClasses.put(c, s);
		}
	}
	public HashSet<Local> getFieldPointedTo(Local c, SootField f){
		if(allClasses.containsKey(c)){
			return allClasses.get(c).getPointTo(f);
		} else {
			throw new NullPointerException("can't find this key" + c.toString());
		}
	}
	public void assign(Local a, Local b){
		ClassFields temp = allClasses.get(a);
		allClasses.put(b, temp);
	}
	public boolean contains(Local l, SootField f){
		if(allClasses.containsKey(l)){
			ClassFields temp = allClasses.get(l);
			return temp.contains(f);
		}
		return false;
	}
	public boolean containsKey(Local l){
		return allClasses.containsKey(l);
	}
}
//class ParameterMapLocal{
//	List<Local> paras;
//	Map<Local, Local> LocalPara;
//	ParameterMapLocal(){
//		paras = new ArrayList<>();
//		LocalPara = new HashMap<>();
//	}
//	void addFunctionParas(Local para){
//		paras.add(para);
//	}
//	//void map
//}

public class Anderson {
	private List<AssignConstraint> assignConstraintList = new ArrayList<AssignConstraint>();
	private List<NewConstraint> newConstraintList = new ArrayList<NewConstraint>();
	//private List<FieldAssignConstraint> FieldAssignConstraintList = new ArrayList<>();
	public List<Local> assignParameters = new ArrayList<>();
	Map<Local, Local> assignLocalToReal = new HashMap<>();
	//Map<Local, Local> tempToLocal = new HashMap<>();
    Map<Local, HashSet<Local>> tempToLocal = new HashMap<>();
	//some data structure about assign method
    //now we need data structure to init method
	//Map<Local, ClassFields> class_and_fields = new HashMap<>();
    Map<Local, HashSet<Local>> classWithField = new HashMap<>();
    HashSet<Local> fieldPointTo = new HashSet<>();
	Map<Local, TreeSet<Integer>> pts = new HashMap<Local, TreeSet<Integer>>();
	AllClassesWithFields classes = new AllClassesWithFields();
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
