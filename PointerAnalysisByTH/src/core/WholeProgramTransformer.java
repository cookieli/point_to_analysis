package core;

import java.util.HashSet;
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
				Local new_Local = null;
				if (sm.hasActiveBody()) {
					for (Unit u : sm.getActiveBody().getUnits()) {
						if(sm.toString().contains("FieldSensitivity")){
						//	System.out.println("S: "+u.toString());
						}
						if(sm.toString().contains("<benchmark.objects.A: void <init>(benchmark.objects.B)")){
						    System.out.println("---------"+ "init method---" + u.toString());
                        }
						/*if(sm.toString().contains("FieldSensitivity") && sm.toString().contains("assign")){
						    System.out.println("-------then into assign method body---"+ sm.toString()+"------");
							//System.out.println("-----unit----"+u.toString()+"--------");
							if (u instanceof DefinitionStmt){
                                //System.out.println("------"+u.toString()+"--------");
                                //System.out.println("========left opr====="+((DefinitionStmt) u).getLeftOp().toString()+"=====");
                                *//*if(((DefinitionStmt) u).getRightOp().toString().contains("@parameter")){
                                    System.out.println("right opr"+ ((DefinitionStmt) u).getRightOp().toString()+ "=====");
                                }*//*
                                for(int i = 0; i < anderson.assignParameters.size(); i++){
                                    if(((DefinitionStmt) u).getRightOp().toString().contains("@parameter" + i)){
                                        anderson.mapParametersWithReal((Local) ((DefinitionStmt) u).getLeftOp(), i);
                                    }
                                }
                                if(((DefinitionStmt) u).getLeftOp().toString().contains("$") && ((DefinitionStmt) u).getRightOp() instanceof InstanceFieldRef){
                                    //anderson.tempToLocal.put((Local) ((DefinitionStmt) u).getLeftOp(), (Local) ((InstanceFieldRef) ((DefinitionStmt) u).getRightOp()).getBase());
                                    for(Local e: anderson.fieldPointTo){
                                        anderson.tempToLocal.put((Local) ((DefinitionStmt) u).getLeftOp(), e);
                                    }
                                }
                                if(((DefinitionStmt) u).getRightOp().toString().contains("$") && ((DefinitionStmt) u).getLeftOp() instanceof InstanceFieldRef){
                                    Local temp = anderson.tempToLocal.get((Local) ((DefinitionStmt) u).getRightOp());
                                    Local rightTempOfReal = anderson.assignLocalToReal.get(temp);
                                    //Local leftTempOfReal = anderson.assignLocalToReal.get(((InstanceFieldRef)((DefinitionStmt) u).getLeftOp()).getBase());
                                    //anderson.addAssignConstraint(rightTempOfReal, leftTempOfReal);
                                    anderson.fieldPointTo.add(rightTempOfReal);
                                }
                            }
						}*/
						if (u instanceof InvokeStmt) {
							if(sm.toString().contains("FieldSensitivity")){
								//System.out.println("--invoke ---"+"S: "+u.toString()+"--------------");
								//System.out.println("invoke method-------"+((InvokeStmt) u).getInvokeExpr().getMethod().toString());
								//InvokeExpr ie = ((InvokeStmt) u).getInvokeExpr();
								//System.out.println("-----invoke method----"+ ie.getMethod().toString());
							}
							InvokeExpr ie = ((InvokeStmt) u).getInvokeExpr();
                            //System.out.println("-----invoke method----"+ ie.getMethod().toString());
							if(ie.getMethod().toString().equals("<test.FieldSensitivity: void assign(benchmark.objects.A,benchmark.objects.A)>")) {
                                //System.out.println(ie.getArgs().size()+" first into assign ");
                                System.out.println("how assign method execute-----" + ie.getMethod().hasActiveBody());
                                for (int i = 0; i < ie.getArgs().size(); i++) {
                                    anderson.setAssignParameters((Local) ie.getArgs().get(i));
                                }
                                if (ie.getMethod().hasActiveBody()) {
                                    for (Unit sub : ie.getMethod().getActiveBody().getUnits()) {
                                        System.out.println("----the assign body unit---" + sub.toString());
                                        if (sub instanceof DefinitionStmt) {
                                            System.out.println("-----definition-----" + sub.toString());

                                            for (int i = 0; i < anderson.assignParameters.size(); i++) {// this loop means we can map parameters to real values
                                                if (((DefinitionStmt) sub).getRightOp().toString().contains("@parameter" + i)) {
                                                    anderson.mapParametersWithReal((Local) ((DefinitionStmt) sub).getLeftOp(), i);
                                                }
                                            }
                                            if (((DefinitionStmt) sub).getLeftOp().toString().contains("$") && ((DefinitionStmt) sub).getRightOp() instanceof InstanceFieldRef) {
                                                //anderson.tempToLocal.put((Local) ((DefinitionStmt) u).getLeftOp(), (Local) ((InstanceFieldRef) ((DefinitionStmt) u).getRightOp()).getBase());
                                                
                                                for (Local e : anderson.fieldPointTo) {
                                                    anderson.tempToLocal.put((Local) ((DefinitionStmt) sub).getLeftOp(), e);
                                                }
                                            }
                                            if (((DefinitionStmt) sub).getRightOp().toString().contains("$") && ((DefinitionStmt) sub).getLeftOp() instanceof InstanceFieldRef) {
                                                Local temp = anderson.tempToLocal.get((Local) ((DefinitionStmt) sub).getRightOp());
                                                Local rightTempOfReal = anderson.assignLocalToReal.get(temp);
                                                //Local leftTempOfReal = anderson.assignLocalToReal.get(((InstanceFieldRef)((DefinitionStmt) u).getLeftOp()).getBase());
                                                //anderson.addAssignConstraint(rightTempOfReal, leftTempOfReal);
                                                anderson.fieldPointTo.add(rightTempOfReal);
                                            }

                                        }
                                    }
                                }
                            }
                            if(ie.getMethod().toString().equals("<benchmark.objects.A: void <init>(benchmark.objects.B)>")){// it means it's a new method
                                //System.out.println("it's in new method");
                                if(ie.getArgs().size() > 0){
                                    HashSet<Local> fieldTo = new HashSet<>();
                                    anderson.classWithField.put(new_Local, fieldTo);
                                    for(int i = 0; i < ie.getArgs().size(); i++){
                                        //System.out.println("allocId: " + allocId + ie.getArgs().get(i).toString());
                                        //anderson.addAssignConstraint((Local) ie.getArgs().get(i), new_Local);
                                        anderson.fieldPointTo.add((Local) ie.getArgs().get(i));
                                        anderson.classWithField.get(new_Local).add((Local) ie.getArgs().get(i));
                                    }
                                }
                            }
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
								new_Local = (Local) ((DefinitionStmt) u).getLeftOp();

							}
							if (((DefinitionStmt)u).getLeftOp() instanceof Local && ((DefinitionStmt)u).getRightOp() instanceof Local) {
								if(sm.toString().contains("FieldSensitivity")){
									//System.out.println("==local assign========"+"S: "+((DefinitionStmt) u).toString() + "==================");
									//System.out.println("right opr " + ((InstanceFieldRef)(((DefinitionStmt) u).getRightOp())).getField().getName());
								}
								anderson.addAssignConstraint((Local)((DefinitionStmt) u).getRightOp(), (Local)((DefinitionStmt) u).getLeftOp());
							}

							if ( ((DefinitionStmt) u).getLeftOp() instanceof Local && ((DefinitionStmt) u).getRightOp() instanceof InstanceFieldRef) {
								if(sm.toString().contains("FieldSensitivity")){
									/*System.out.println("method name:"+sm.toString());
									System.out.println("=======InstanceFieldRef======"+u.toString()+"=========");
									System.out.println("=====instanceField===="+((Local)((InstanceFieldRef)((DefinitionStmt) u).getRightOp()).getBase()).toString() +"======");
									System.out.println("right opr " + ((InstanceFieldRef)(((DefinitionStmt) u).getRightOp())).getField().getName());*/
									//anderson.addAssignConstraint(((Local)((InstanceFieldRef)((DefinitionStmt) u).getRightOp()).getBase()), (Local)((DefinitionStmt) u).getLeftOp());
                                    for(Local e: anderson.fieldPointTo){
                                        anderson.addAssignConstraint(e, (Local)((DefinitionStmt) u).getLeftOp());
                                    }
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
