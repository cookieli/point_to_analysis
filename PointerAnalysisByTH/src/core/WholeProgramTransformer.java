package core;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import soot.Local;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;

public class WholeProgramTransformer extends SceneTransformer {
	
	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) {
		
		TreeMap<Integer, Local> queries = new TreeMap<Integer, Local>();
		Anderson anderson = new Anderson(); 
		
		ReachableMethods reachableMethods = Scene.v().getReachableMethods();
		QueueReader<MethodOrMethodContext> qr = reachableMethods.listener();		
		while (qr.hasNext()) {
			SootMethod sm = qr.next().method();
			/*if (sm.toString().contains("FieldSensitivity"))
				System.out.println(sm);*/

				int allocId = 0;
				if (sm.hasActiveBody()) {
					for (Unit u : sm.getActiveBody().getUnits()) {
						/*if(sm.toString().contains("FieldSensitivity")){
							System.out.println("S: "+u.toString());
						}*/
						if (u instanceof InvokeStmt) {
							if(sm.toString().contains("FieldSensitivity")){
								System.out.println("--invoke ---"+"S: "+u.toString()+"--------------");
							}
							InvokeExpr ie = ((InvokeStmt) u).getInvokeExpr();
							if (ie.getMethod().toString().equals("<benchmark.internal.Benchmark: void alloc(int)>")) {
								allocId = ((IntConstant)ie.getArgs().get(0)).value;
							}
							if (ie.getMethod().toString().equals("<benchmark.internal.Benchmark: void test(int,java.lang.Object)>")) {
								Value v = ie.getArgs().get(1);
								int id = ((IntConstant)ie.getArgs().get(0)).value;
								queries.put(id, (Local)v);
							}
						}
						if (u instanceof DefinitionStmt) {

							if (((DefinitionStmt)u).getRightOp() instanceof NewExpr) {
								//System.out.println("Alloc " + allocId);
								if(sm.toString().contains("FieldSensitivity")){
									System.out.println("====new assign====="+"S: "+((DefinitionStmt) u).toString() + "==================");
									//System.out.println("right opr " + ((InstanceFieldRef)(((DefinitionStmt) u).getRightOp())).getField().getName());
								}
								anderson.addNewConstraint(allocId, (Local)((DefinitionStmt) u).getLeftOp());
							}
							if (((DefinitionStmt)u).getLeftOp() instanceof Local && ((DefinitionStmt)u).getRightOp() instanceof Local) {
								if(sm.toString().contains("FieldSensitivity")){
									System.out.println("==local assign========"+"S: "+((DefinitionStmt) u).toString() + "==================");
									//System.out.println("right opr " + ((InstanceFieldRef)(((DefinitionStmt) u).getRightOp())).getField().getName());
								}
								anderson.addAssignConstraint((Local)((DefinitionStmt) u).getRightOp(), (Local)((DefinitionStmt) u).getLeftOp());
							}

							if (((DefinitionStmt) u).getRightOp() instanceof InstanceFieldRef) {
								if(sm.toString().contains("FieldSensitivity")){
									System.out.println("method name:"+sm.toString());
									System.out.println("=======InstanceFieldRef======"+u.toString()+"=========");
									System.out.println("=====instanceField===="+((Local)((InstanceFieldRef)((DefinitionStmt) u).getRightOp()).getBase()).toString() +"======");
									System.out.println("right opr " + ((InstanceFieldRef)(((DefinitionStmt) u).getRightOp())).getField().getName());
								}
							}
						}
					}
				}
			//}
		}
		
		anderson.run();
		String answer = "";
		for (Entry<Integer, Local> q : queries.entrySet()) {
			TreeSet<Integer> result = anderson.getPointsToSet(q.getValue());
			answer += q.getKey().toString() + ":";
			for (Integer i : result) {
				answer += " " + i;
			}
			answer += "\n";
		}
		AnswerPrinter.printAnswer(answer);
		
	}

}
