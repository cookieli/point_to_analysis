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
        //System.out.println("internalTransform-------" + arg1.toString() + "======");
        TreeMap<Integer, Local> queries = new TreeMap<Integer, Local>();
        Anderson anderson = new Anderson();

        ReachableMethods reachableMethods = Scene.v().getReachableMethods();
        QueueReader<MethodOrMethodContext> qr = reachableMethods.listener();
        while (qr.hasNext()) {
            SootMethod sm = qr.next().method();
            //System.out.println("0000000000+ sm method" + sm.toString()+ "-------");
            /*if (sm.toString().contains("FieldSensitivity"))
                System.out.println(sm);*/

            int allocId = 0;
            Local new_Local = null;
            //System.out.println("========sm method =====" + sm.toString()+ "====method");
            if (sm.hasActiveBody()) {
                if (sm.toString().contains("test.")) {
                    System.out.println("sm method=====" + sm.toString() + " =====");
                    // System.out.println("==============S==========: "+u.toString());
                }
                for (Unit u : sm.getActiveBody().getUnits()) {
                    if (sm.toString().contains("test.")) {
                        System.out.println("======method unit=======" + u.toString() + " ====method");
                    }
                    if (sm.toString().contains("<benchmark.objects.A: void <init>(benchmark.objects.B)")) {
                        // System.out.println("---------"+ "init method---" + u.toString());
                    }
                    if (sm.toString().contains("FieldSensitivity") && sm.toString().contains("assign")) {
                        continue;
                        //System.out.println("-----then into assing method body------" + sm.toString()+ "---------");
                        //System.out.println("---------assign init ----" + u.toString());
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
                        /*if( u instanceof  SpecialInvokeExpr){
                            if(sm.toString().contains("FieldSensitivity")){
                                System.out.println("special invoke -----" + u.toString());
                            }
                        }*/
                    if (u instanceof InvokeStmt) {
                        if (sm.toString().contains("FieldSensitivity")) {
                            //System.out.println("--invoke ---"+"S: "+u.toString()+"--------------");
                            //System.out.println("invoke method-------"+((InvokeStmt) u).getInvokeExpr().getMethod().toString());
                            //InvokeExpr ie = ((InvokeStmt) u).getInvokeExpr();
                            //System.out.println("-----invoke method----"+ ie.getMethod().toString());
                        }
                        InvokeExpr ie = ((InvokeStmt) u).getInvokeExpr();
                        //System.out.println("-----invoke method----"+ ie.getMethod().toString());
                        if (ie instanceof SpecialInvokeExpr && ie.getMethod().toString().contains("init") && sm.toString().contains("test.")) {
                            System.out.println("------special invoke-------" + ie.toString());
                            SpecialInvokeExpr sie = (SpecialInvokeExpr) ie;
                            //System.out.println("=======sie string===="+ sie.toString()+ " ====it's args' length" + sie.getBase().toString()+ "; "+ sie.getArgs().size());
                            if (sie.getArgCount() > 0) {
                                if (anderson.classWithField.containsKey((Local) sie.getBase())) {
                                    System.out.println("----base--" + sie.getBase().toString() + "=====");
                                    for (int i = 0; i < sie.getArgCount(); i++) {
                                        anderson.classWithField.get((Local) sie.getBase()).add((Local) sie.getArgs().get(i));
                                    }
                                } else {
                                    //System.out.println("----base--"+ sie.getBase().toString() + "=====");
                                    HashSet<Local> ss = new HashSet<>();
                                    for (int i = 0; i < sie.getArgCount(); i++) {
                                        ss.add((Local) sie.getArg(i));
                                        //  System.out.println("special invoke arg========="+ sie.getArg(i).toString());
                                    }
                                    anderson.classWithField.put((Local) sie.getBase(), ss);

                                }
                            }
                        }
                        if (ie.getMethod().toString().contains("void assign")) {
                            //System.out.println(ie.getArgs().size()+" first into assign ");
                            //System.out.println("how assign method execute-----" + ie.getMethod().hasActiveBody());
                            for (int i = 0; i < ie.getArgs().size(); i++) {
                                anderson.setAssignParameters((Local) ie.getArgs().get(i));
                            }
                            if (ie.getMethod().hasActiveBody()) {
                                for (Unit sub : ie.getMethod().getActiveBody().getUnits()) {
                                    // System.out.println("----the assign body unit---" + sub.toString());
                                    if (sub instanceof DefinitionStmt) {
                                        //System.out.println("-----definition-----" + sub.toString());

                                        for (int i = 0; i < anderson.assignParameters.size(); i++) {// this loop means we can map parameters to real values
                                            if (((DefinitionStmt) sub).getRightOp().toString().contains("@parameter" + i)) {
                                                anderson.mapParametersWithReal((Local) ((DefinitionStmt) sub).getLeftOp(), i);
                                            }
                                        }
                                        if (((DefinitionStmt) sub).getLeftOp().toString().contains("$") && ((DefinitionStmt) sub).getRightOp() instanceof InstanceFieldRef) {
                                            //anderson.tempToLocal.put((Local) ((DefinitionStmt) u).getLeftOp(), (Local) ((InstanceFieldRef) ((DefinitionStmt) u).getRightOp()).getBase());
                                                
                                                /*for (Local e : anderson.fieldPointTo) {
                                                    anderson.tempToLocal.put((Local) ((DefinitionStmt) sub).getLeftOp(), e);
                                                }*/
                                            HashSet<Local> temp = new HashSet<>();
                                            Local rightOfLocal = (Local) ((InstanceFieldRef) ((DefinitionStmt) sub).getRightOp()).getBase();
                                            Local rightOfReal = anderson.assignLocalToReal.get(rightOfLocal);
                                            if (!anderson.classWithField.containsKey(rightOfReal)) {
                                                //System.err.println("can't find "+ rightOfReal.toString()+ " field");
                                                throw new NullPointerException("can't find " + rightOfReal.toString() + " field");
                                            } else {
                                                for (Local e : anderson.classWithField.get(rightOfReal)) {
                                                    temp.add(e);
                                                }
                                                anderson.tempToLocal.put((Local) ((DefinitionStmt) sub).getLeftOp(), temp);
                                            }
                                        }
                                        if (((DefinitionStmt) sub).getRightOp().toString().contains("$") && ((DefinitionStmt) sub).getLeftOp() instanceof InstanceFieldRef) {
                                            HashSet<Local> temp = anderson.tempToLocal.get((Local) ((DefinitionStmt) sub).getRightOp());
                                            //Local rightTempOfReal = anderson.assignLocalToReal.get(temp);
                                            Local leftOfReal = anderson.assignLocalToReal.get(((InstanceFieldRef) ((DefinitionStmt) sub).getLeftOp()).getBase());
                                            //anderson.addAssignConstraint(rightTempOfReal, leftTempOfReal);
                                            //anderson.fieldPointTo.add(rightTempOfReal);
                                            if (!anderson.classWithField.containsKey(leftOfReal)) {
                                                anderson.classWithField.put(leftOfReal, temp);
                                            } else {
                                                anderson.classWithField.get(leftOfReal).addAll(temp);
                                            }
                                        }

                                    }
                                }
                            }
                        }
                        if (ie.getMethod().toString().equals("<benchmark.objects.A: void <init>(benchmark.objects.B)>")) {// it means it's a new method
                            // System.out.println("it's in new method a = new(b)");
                            if (ie.getArgs().size() > 0) {
                                //HashSet<Local> fieldTo = new HashSet<>();
                                //anderson.classWithField.put(new_Local, fieldTo);
                                for (int i = 0; i < ie.getArgs().size(); i++) {
                                    //System.out.println("allocId: " + allocId + ie.getArgs().get(i).toString());
                                    //anderson.addAssignConstraint((Local) ie.getArgs().get(i), new_Local);
                                    anderson.fieldPointTo.add((Local) ie.getArgs().get(i));
                                    //anderson.classWithField.get(new_Local).add((Local) ie.getArgs().get(i));
                                }
                            }
                        }
                        if (ie.getMethod().toString().equals("<benchmark.internal.Benchmark: void alloc(int)>")) {
                            allocId = ((IntConstant) ie.getArgs().get(0)).value;
                        }
                        if (ie.getMethod().toString().equals("<benchmark.internal.Benchmark: void test(int,java.lang.Object)>")) {
                            Value v = ie.getArgs().get(1);
                            int id = ((IntConstant) ie.getArgs().get(0)).value;
                            queries.put(id, (Local) v);
                        }
                    }
                    if (u instanceof DefinitionStmt) {

                        if (((DefinitionStmt) u).getRightOp() instanceof NewExpr) {
                            //System.out.println("Alloc " + allocId);
                            if (sm.toString().contains("FieldSensitivity")) {
                                //System.out.println("====new assign====="+"S: "+((DefinitionStmt) u).toString() + "==================");
                                //System.out.println("right opr " + ((InstanceFieldRef)(((DefinitionStmt) u).getRightOp())).getField().getName());
                            }
                            anderson.addNewConstraint(allocId, (Local) ((DefinitionStmt) u).getLeftOp());
                            new_Local = (Local) ((DefinitionStmt) u).getLeftOp();

                        }
                        if (((DefinitionStmt) u).getLeftOp() instanceof Local && ((DefinitionStmt) u).getRightOp() instanceof Local) {
                            if (sm.toString().contains("FieldSensitivity")) {
                                //System.out.println("==local assign========"+"S: "+((DefinitionStmt) u).toString() + "==================");
                                //System.out.println("right opr " + ((InstanceFieldRef)(((DefinitionStmt) u).getRightOp())).getField().getName());
                            }
                            if (anderson.classWithField.containsKey(((DefinitionStmt) u).getRightOp())) {
                                //System.out.println("------local assign right opr"+((DefinitionStmt)u).getRightOp().toString());
                                HashSet<Local> temp = anderson.classWithField.get(((DefinitionStmt) u).getRightOp());
                                if (anderson.classWithField.containsKey(((DefinitionStmt) u).getLeftOp())) {
                                    anderson.classWithField.get(((DefinitionStmt) u).getLeftOp()).addAll(temp);
                                } else {
                                    anderson.classWithField.put((Local) (((DefinitionStmt) u).getLeftOp()), temp);
                                    System.out.println("--------local assign left opr ----" + ((DefinitionStmt) u).getLeftOp().toString());
                                }
                            }
                            anderson.addAssignConstraint((Local) ((DefinitionStmt) u).getRightOp(), (Local) ((DefinitionStmt) u).getLeftOp());
                        }
                        if (((DefinitionStmt) u).getLeftOp() instanceof InstanceFieldRef && ((DefinitionStmt) u).getRightOp() instanceof Local) {
                            Local Base = (Local) ((InstanceFieldRef) ((DefinitionStmt) u).getLeftOp()).getBase();
                            if (!anderson.classWithField.containsKey(Base)) {
                                HashSet<Local> temp = new HashSet<>();
                                temp.add((Local) ((DefinitionStmt) u).getRightOp());
                                anderson.classWithField.put(Base, temp);
                            } else {
                                anderson.classWithField.get(Base).add((Local) ((DefinitionStmt) u).getRightOp());
                            }
                        }
                        if (((DefinitionStmt) u).getLeftOp() instanceof InstanceFieldRef && ((DefinitionStmt) u).getRightOp() instanceof InstanceFieldRef) {
                            Local leftBase = (Local) ((InstanceFieldRef) ((DefinitionStmt) u).getLeftOp()).getBase();
                            Local rightBase = (Local) ((InstanceFieldRef) ((DefinitionStmt) u).getRightOp()).getBase();
                            HashSet<Local> temp;
                            try {
                                temp = anderson.classWithField.get(rightBase);
                            } catch (NullPointerException e) {
                                System.out.println("your class don't have" + rightBase.toString() + " in class with field");
                                return;
                            }
                            if (anderson.classWithField.containsKey(leftBase)) {
                                for (Local elem : temp) {
                                    anderson.classWithField.get(elem).add(elem);
                                }
                            } else {
                                HashSet left_temp = new HashSet();
                                for (Local elem : temp) {
                                    left_temp.add(elem);
                                }
                                anderson.classWithField.put(leftBase, left_temp);
                            }
                        }

                        if (((DefinitionStmt) u).getLeftOp() instanceof Local && ((DefinitionStmt) u).getRightOp() instanceof InstanceFieldRef) {
                            if (sm.toString().contains("FieldSensitivity")) {
                                //System.out.println("=======InstanceFieldRef======"+u.toString()+"=========");
									/*System.out.println("method name:"+sm.toString());
									System.out.println("=======InstanceFieldRef======"+u.toString()+"=========");
									System.out.println("=====instanceField===="+((Local)((InstanceFieldRef)((DefinitionStmt) u).getRightOp()).getBase()).toString() +"======");
									System.out.println("right opr " + ((InstanceFieldRef)(((DefinitionStmt) u).getRightOp())).getField().getName());*/
                                //anderson.addAssignConstraint(((Local)((InstanceFieldRef)((DefinitionStmt) u).getRightOp()).getBase()), (Local)((DefinitionStmt) u).getLeftOp());
                                    /*for(Local e: anderson.fieldPointTo){
                                        anderson.addAssignConstraint(e, (Local)((DefinitionStmt) u).getLeftOp());
                                    }*/
                                Local Base = (Local) ((InstanceFieldRef) ((DefinitionStmt) u).getRightOp()).getBase();
                                if (!anderson.classWithField.containsKey(Base)) {
                                    System.out.println("something wrong");
                                    System.out.println("the wrong class is " + Base.toString());
                                } else {
                                    System.out.println("the right class is " + Base.toString());
                                    for (Local e : anderson.classWithField.get(Base)) {
                                        anderson.addAssignConstraint(e, (Local) ((DefinitionStmt) u).getLeftOp());
                                    }
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
