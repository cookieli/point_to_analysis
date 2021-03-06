package core;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import soot.*;
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

                    if (u instanceof InvokeStmt) {
                        if (sm.toString().contains("FieldSensitivity")) {
                            System.out.println("--invoke ---"+"S: "+u.toString()+"--------------");
                            System.out.println("invoke method-------"+((InvokeStmt) u).getInvokeExpr().getMethod().toString());
                            //InvokeExpr ie = ((InvokeStmt) u).getInvokeExpr();
                            //System.out.println("-----invoke method----"+ ie.getMethod().toString());
                        }
                        InvokeExpr ie = ((InvokeStmt) u).getInvokeExpr();
                        //System.out.println("-----invoke method----"+ ie.getMethod().toString());
                        if (ie instanceof SpecialInvokeExpr && ie.getMethod().toString().contains("init") && sm.toString().contains("test.")) {
                            //System.out.println("------special invoke-------" + ie.toString());
                            SpecialInvokeExpr sie = (SpecialInvokeExpr) ie;
                            //System.out.println("=======sie string===="+ sie.toString()+ " ====it's args' length" + sie.getBase().toString()+ "; "+ sie.getArgs().size());
                            if (sie.getArgCount() > 0) {
                                if(ie.getMethod().hasActiveBody()){
                                    Local Base = (Local) sie.getBase();

                                    for(Unit sub: ie.getMethod().getActiveBody().getUnits()){
                                       // System.out.println("init method unit-----" + sub.toString());
                                        if(sub instanceof DefinitionStmt){
                                            if(((DefinitionStmt)sub).getLeftOp() instanceof InstanceFieldRef && ((DefinitionStmt)sub).getRightOp() instanceof Local){
                                               // System.out.println("left" + ((DefinitionStmt)sub).getLeftOp().toString());
                                                //System.out.println("right" + ((DefinitionStmt)sub).getRightOp().toString());
                                                if(!((DefinitionStmt)sub).getRightOp().toString().contains("$")){
                                                    anderson.classes.setClasseAndField(Base, ((InstanceFieldRef)(((DefinitionStmt)sub).getLeftOp())).getField(),(Local) sie.getArg(0));
                                                }
                                            }
                                        }
                                    }
                                }
                                //Local Base = (Local) sie.getBase();

                            }
                        }
                        else if ((ie instanceof  SpecialInvokeExpr) && sm.toString().contains("test.") && ie.getArgs().size() > 0) {
                            //System.out.println(ie.getArgs().size()+" first into assign ");
                            //System.out.println("how assign method execute-----" + ie.getMethod().hasActiveBody());
                            anderson.clearAssignParas();
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
                                            anderson.addTempPointTo(sub);
                                        }
                                        if (((DefinitionStmt) sub).getRightOp().toString().contains("$") && ((DefinitionStmt) sub).getLeftOp() instanceof InstanceFieldRef) {
                                            Local temp = (Local) ((DefinitionStmt) sub).getRightOp();
                                            //Local rightTempOfReal = anderson.assignLocalToReal.get(temp);
                                            Local leftOfReal = anderson.assignLocalToReal.get(((InstanceFieldRef) ((DefinitionStmt) sub).getLeftOp()).getBase());
                                            SootField leftField = ((InstanceFieldRef) ((DefinitionStmt) sub).getLeftOp()).getField();
                                            anderson.getPointedFromTemp(leftOfReal, leftField, temp);
                                           // anderson.getPointedFromTemp(((InstanceFieldRef) ((DefinitionStmt) sub).getLeftOp()),(Local) ((DefinitionStmt) sub).getRightOp());
                                        }

                                    }
                                }
                            }
                        } else if(ie instanceof VirtualInvokeExpr && sm.toString().contains("test.")){
                            if(ie.getMethod().hasActiveBody()){
                                VirtualInvokeExpr vie = (VirtualInvokeExpr) ie;
                                Local Base = (Local) vie.getBase();
                                anderson.clearAssignParas();
                                if(vie.getArgs().size() > 0){
                                    for(int i = 0; i < vie.getArgs().size(); i++){
                                        anderson.setAssignParameters((Local) vie.getArg(i));
                                    }
                                }
                                for(Unit v: ie.getMethod().getActiveBody().getUnits()){
                                    System.out.println("=======vie======"+v.toString());
                                    if (v instanceof DefinitionStmt){
                                        for (int i = 0; i < anderson.assignParameters.size(); i++) {// this loop means we can map parameters to real values
                                            if(((DefinitionStmt) v).getRightOp().toString().contains("@this")){
                                                anderson.mapParametersWithReal((Local)((DefinitionStmt) v).getLeftOp(), Base);
                                            }
                                            if (((DefinitionStmt) v).getRightOp().toString().contains("@parameter" + i)) {
                                                anderson.mapParametersWithReal((Local) ((DefinitionStmt) v).getLeftOp(), i);
                                            }
                                            else if (((DefinitionStmt) v).getLeftOp().toString().contains("$") && ((DefinitionStmt) v).getRightOp() instanceof InstanceFieldRef) {
                                                anderson.addTempPointTo(v);
                                            }
                                            else if (((DefinitionStmt) v).getRightOp().toString().contains("$") && ((DefinitionStmt) v).getLeftOp() instanceof InstanceFieldRef) {
                                                Local temp = (Local) ((DefinitionStmt) v).getRightOp();
                                                //Local rightTempOfReal = anderson.assignLocalToReal.get(temp);
                                                Local leftOfReal = anderson.assignLocalToReal.get(((InstanceFieldRef) ((DefinitionStmt) v).getLeftOp()).getBase());
                                                SootField leftField = ((InstanceFieldRef) ((DefinitionStmt) v).getLeftOp()).getField();
                                                anderson.getPointedFromTemp(leftOfReal, leftField, temp);
                                                // anderson.getPointedFromTemp(((InstanceFieldRef) ((DefinitionStmt) sub).getLeftOp()),(Local) ((DefinitionStmt) sub).getRightOp());
                                            }
                                            else if(((DefinitionStmt) v).getRightOp() instanceof Local && ((DefinitionStmt) v).getLeftOp() instanceof InstanceFieldRef){
                                                System.out.println("it should be here");
                                                Local leftOfReal = anderson.assignLocalToReal.get(((InstanceFieldRef) ((DefinitionStmt) v).getLeftOp()).getBase());
                                                SootField leftField = ((InstanceFieldRef) ((DefinitionStmt) v).getLeftOp()).getField();
                                                Local rightOfReal = anderson.assignLocalToReal.get(((DefinitionStmt) v).getRightOp());
                                                anderson.classes.setClasseAndField(leftOfReal, leftField, rightOfReal);
                                            }
                                            else if(((DefinitionStmt) v).getLeftOp() instanceof Local && ((DefinitionStmt) v).getRightOp() instanceof InstanceFieldRef){
                                                System.out.println("it should be here");
                                                Local rightOfReal = anderson.assignLocalToReal.get(((InstanceFieldRef) ((DefinitionStmt) v).getRightOp()).getBase());
                                                SootField rightField = ((InstanceFieldRef) ((DefinitionStmt) v).getRightOp()).getField();
                                                Local leftOfReal = anderson.assignLocalToReal.get(((DefinitionStmt) v).getLeftOp());
                                                //anderson.classes.setClasseAndField(leftOfReal, leftField, rightOfReal);
                                                HashSet<Local> temp = anderson.classes.getFieldPointedTo(rightOfReal, rightField);
                                                for(Local e: temp){
                                                    anderson.addAssignConstraint(e, leftOfReal);
                                                }
                                            }
                                        }

                                    }
                                }
                            }
                        }

                        if (ie.getMethod().toString().equals("<benchmark.internal.Benchmark: void alloc(int)>")) {
                            if(ie.getArgs().size() > 0)
                                allocId = ((IntConstant) ie.getArgs().get(0)).value;
                            else
                                allocId = 0;
                        }
                        if (ie.getMethod().toString().equals("<benchmark.internal.Benchmark: void test(int,java.lang.Object)>")) {
                            Value v = ie.getArgs().get(1);
                            int id = ((IntConstant) ie.getArgs().get(0)).value;
                            queries.put(id, (Local) v);
                        }
                    }
                    if (u instanceof DefinitionStmt) {

                        if(((DefinitionStmt) u).getRightOp() instanceof NewArrayExpr){
                            anderson.addNewConstraint(allocId, (Local) ((DefinitionStmt) u).getLeftOp());
                        }

                        if(((DefinitionStmt) u).getRightOp() instanceof VirtualInvokeExpr){
                            if (sm.toString().contains("test.")){
                                System.out.println("definitionStmt-------" + u.toString());
                                VirtualInvokeExpr vie = (VirtualInvokeExpr) ((DefinitionStmt) u).getRightOp();
                                Local Base = (Local) vie.getBase();
                                SootMethod rsm = ((VirtualInvokeExpr) ((DefinitionStmt) u).getRightOp()).getMethod();
                                if(rsm.hasActiveBody()){
                                    for(Unit rSub: rsm.getActiveBody().getUnits()){
                                        System.out.println("=======definition stmt unit======" + rSub.toString() );
                                        if(rSub instanceof DefinitionStmt){
                                            if(((DefinitionStmt) rSub).getRightOp().toString().contains("@this")){
                                                anderson.mapParametersWithReal((Local)((DefinitionStmt) rSub).getLeftOp(), Base);
                                            }
                                            if(((DefinitionStmt) rSub).getLeftOp().toString().contains("$") && ((DefinitionStmt) rSub).getRightOp() instanceof InstanceFieldRef){
                                                anderson.addTempPointTo(rSub);
                                            }
                                            if(((DefinitionStmt) rSub).getLeftOp() instanceof InstanceFieldRef && ((DefinitionStmt) rSub).getRightOp().toString().contains("$")){
                                                Local temp = (Local) ((DefinitionStmt) rSub).getRightOp();
                                                //Local rightTempOfReal = anderson.assignLocalToReal.get(temp);
                                                Local leftOfReal = anderson.assignLocalToReal.get(((InstanceFieldRef) ((DefinitionStmt) rSub).getLeftOp()).getBase());
                                                SootField leftField = ((InstanceFieldRef) ((DefinitionStmt) rSub).getLeftOp()).getField();
                                                anderson.getPointedFromTemp(leftOfReal, leftField, temp);
                                            }

                                        }
                                        if(rSub instanceof ReturnStmt){
                                            Local temp = (Local) ((ReturnStmt) rSub).getOp();
                                            if(((DefinitionStmt) u).getLeftOp() instanceof Local){
                                                anderson.getPointedFromTemp((Local) ((DefinitionStmt) u).getLeftOp(), temp);
                                            }
                                            if(((DefinitionStmt) u).getLeftOp() instanceof InstanceFieldRef) {
                                                Local b = (Local)((InstanceFieldRef)((DefinitionStmt) u).getLeftOp()).getBase();
                                                SootField f = ((InstanceFieldRef)((DefinitionStmt) u).getLeftOp()).getField();
                                                anderson.getPointedFromTemp(b, f, temp);
                                            }
                                        }
                                    }
                                }

                            }
                        }

                        if (((DefinitionStmt) u).getRightOp() instanceof NewExpr) {
                            //System.out.println("Alloc " + allocId);
                            if (sm.toString().contains("test.")) {
                                //System.out.println("====new assign====="+"S: "+((DefinitionStmt) u).toString() + "==================");
                                //System.out.println("right opr " + ((InstanceFieldRef)(((DefinitionStmt) u).getRightOp())).getField().getName());
                            }
                            anderson.addNewConstraint(allocId, (Local) ((DefinitionStmt) u).getLeftOp());
                            new_Local = (Local) ((DefinitionStmt) u).getLeftOp();

                        }
                        if(((DefinitionStmt) u).getLeftOp()  instanceof  ArrayRef && ((DefinitionStmt) u).getRightOp() instanceof Local) {
                            if (sm.toString().contains("test.")) {
                                System.out.println("==array assign========"+"S: "+((DefinitionStmt) u).toString() + "==================");
                                //System.out.println("right opr " + ((InstanceFieldRef)(((DefinitionStmt) u).getRightOp())).getField().getName());
                            }
                            Local base = (Local) ((ArrayRef)(((DefinitionStmt) u).getLeftOp())).getBase();
                            if(anderson.classes.containsKey((Local) ((DefinitionStmt) u).getRightOp())){
                                anderson.classes.assign((Local) ((DefinitionStmt) u).getRightOp(), base);
                            }
                            anderson.addAssignConstraint((Local) ((DefinitionStmt) u).getRightOp(), base);

                        }
                        if(((DefinitionStmt) u).getLeftOp() instanceof ArrayRef && ((DefinitionStmt) u).getRightOp() instanceof InstanceFieldRef){
                            Local array_Base = (Local) ((ArrayRef)((DefinitionStmt) u).getLeftOp()).getBase();
                            if(sm.toString().contains("test.")) {
                                Local Base = (Local) ((InstanceFieldRef) ((DefinitionStmt) u).getRightOp()).getBase();
                                SootField field = ((InstanceFieldRef) ((DefinitionStmt) u).getRightOp()).getField();
                                anderson.addAssignConstraintFromClassFieldToClass(Base, field, array_Base);
                                /*if (anderson.classes.contains(Base, field)) {
                                    HashSet<Local> t = anderson.classes.getFieldPointedTo(Base, field);
                                    for (Local e : t) {
                                        anderson.addAssignConstraint(e, (Local) ((DefinitionStmt) u).getLeftOp());
                                    }
                                } else {
                                    throw new NullPointerException("can't find base: " + Base.toString() + " field: " + field.toString());
                                }*/
                                //anderson.addAssignConstraintFromClassFieldToClass();

                            }
                        }
                        if(((DefinitionStmt) u).getLeftOp() instanceof ArrayRef && ((DefinitionStmt) u).getRightOp() instanceof ArrayRef){
                            Local leftBase = (Local) ((ArrayRef)((DefinitionStmt) u).getLeftOp()).getBase();
                            Local rightBase = (Local) ((ArrayRef)((DefinitionStmt) u).getRightOp()).getBase();
                            if(anderson.classes.containsKey(rightBase)){
                                anderson.classes.assign(rightBase, leftBase);
                            }
                            anderson.addAssignConstraint(rightBase, leftBase);

                        }

                        if(((DefinitionStmt) u).getLeftOp() instanceof Local && ((DefinitionStmt) u).getRightOp() instanceof ArrayRef){
                            Local leftBase = (Local) ((DefinitionStmt) u).getLeftOp();
                            Local rightBase = (Local) ((ArrayRef)((DefinitionStmt) u).getRightOp()).getBase();
                            if(anderson.classes.containsKey(rightBase)){
                                anderson.classes.assign(rightBase, leftBase);
                            }
                            anderson.addAssignConstraint(rightBase, leftBase);

                        }

                        if(((DefinitionStmt) u).getLeftOp() instanceof InstanceFieldRef && ((DefinitionStmt) u).getRightOp() instanceof ArrayRef){
                            Local left_Base = (Local) ((InstanceFieldRef)((DefinitionStmt) u).getLeftOp()).getBase();
                            if(sm.toString().contains("test.")) {
                                Local Base = (Local) ((ArrayRef) ((DefinitionStmt) u).getRightOp()).getBase();
                                SootField field = ((InstanceFieldRef) ((DefinitionStmt) u).getRightOp()).getField();
                                anderson.addAssignConstraintFromClassFieldToClass(Base, field, left_Base);
                                /*if (anderson.classes.contains(Base, field)) {
                                    HashSet<Local> t = anderson.classes.getFieldPointedTo(Base, field);
                                    for (Local e : t) {
                                        anderson.addAssignConstraint(e, (Local) ((DefinitionStmt) u).getLeftOp());
                                    }
                                } else {
                                    throw new NullPointerException("can't find base: " + Base.toString() + " field: " + field.toString());
                                }*/
                                //anderson.addAssignConstraintFromClassFieldToClass();

                            }
                        }







                        if (((DefinitionStmt) u).getLeftOp() instanceof Local && ((DefinitionStmt) u).getRightOp() instanceof Local) {
                            if (sm.toString().contains("test.")) {
                                System.out.println("==local assign========"+"S: "+((DefinitionStmt) u).toString() + "==================");
                                //System.out.println("right opr " + ((InstanceFieldRef)(((DefinitionStmt) u).getRightOp())).getField().getName());
                            }
                            /*if (anderson.classWithField.containsKey(((DefinitionStmt) u).getRightOp())) {
                                //System.out.println("------local assign right opr"+((DefinitionStmt)u).getRightOp().toString());
                                HashSet<Local> temp = anderson.classWithField.get(((DefinitionStmt) u).getRightOp());
                                if (anderson.classWithField.containsKey(((DefinitionStmt) u).getLeftOp())) {
                                    anderson.classWithField.get(((DefinitionStmt) u).getLeftOp()).addAll(temp);
                                } else {
                                    anderson.classWithField.put((Local) (((DefinitionStmt) u).getLeftOp()), temp);
                                    System.out.println("--------local assign left opr ----" + ((DefinitionStmt) u).getLeftOp().toString());
                                }
                            }*/
                            if(anderson.classes.containsKey((Local) ((DefinitionStmt) u).getRightOp())){
                                anderson.classes.assign((Local) ((DefinitionStmt) u).getRightOp(), (Local) (((DefinitionStmt) u).getLeftOp()));
                            }
                            anderson.addAssignConstraint((Local) ((DefinitionStmt) u).getRightOp(), (Local) ((DefinitionStmt) u).getLeftOp());
                        }
                        if (((DefinitionStmt) u).getLeftOp() instanceof InstanceFieldRef && ((DefinitionStmt) u).getRightOp() instanceof Local) {
                            Local Base = (Local) ((InstanceFieldRef) ((DefinitionStmt) u).getLeftOp()).getBase();
                            SootField field =  ((InstanceFieldRef) ((DefinitionStmt) u).getLeftOp()).getField();
                            anderson.classes.setClasseAndField(Base, field, (Local) ((DefinitionStmt) u).getRightOp());
                        }
                        if (((DefinitionStmt) u).getLeftOp() instanceof InstanceFieldRef && ((DefinitionStmt) u).getRightOp() instanceof InstanceFieldRef) {
                            Local leftBase = (Local) ((InstanceFieldRef) ((DefinitionStmt) u).getLeftOp()).getBase();
                            SootField leftField =  ((InstanceFieldRef) ((DefinitionStmt) u).getLeftOp()).getField();
                            Local rightBase = (Local) ((InstanceFieldRef) ((DefinitionStmt) u).getRightOp()).getBase();
                            SootField rightField = ((InstanceFieldRef) ((DefinitionStmt) u).getRightOp()).getField();
                            HashSet<Local> temp;
                            if(anderson.classes.contains(rightBase, rightField)){
                                temp = anderson.classes.getFieldPointedTo(rightBase, rightField);
                                for(Local e: temp){
                                    anderson.classes.setClasseAndField(leftBase, leftField, e);
                                }
                            }
                            else{
                                throw new NullPointerException("can't find right base: "+ rightBase.toString() + "and it's field" + rightField.toString());
                            }


                        }

                        if (((DefinitionStmt) u).getLeftOp() instanceof Local && ((DefinitionStmt) u).getRightOp() instanceof InstanceFieldRef) {
                            if(sm.toString().contains("test.")) {
                                Local Base = (Local) ((InstanceFieldRef) ((DefinitionStmt) u).getRightOp()).getBase();
                                SootField field = ((InstanceFieldRef) ((DefinitionStmt) u).getRightOp()).getField();
                                anderson.addAssignConstraintFromClassFieldToClass(Base, field, (Local) ((DefinitionStmt) u).getLeftOp());
                                /*if (anderson.classes.contains(Base, field)) {
                                    HashSet<Local> t = anderson.classes.getFieldPointedTo(Base, field);
                                    for (Local e : t) {
                                        anderson.addAssignConstraint(e, (Local) ((DefinitionStmt) u).getLeftOp());
                                    }
                                } else {
                                    throw new NullPointerException("can't find base: " + Base.toString() + " field: " + field.toString());
                                }*/
                               //anderson.addAssignConstraintFromClassFieldToClass();

                            }

                        }
                    }
                }
            }
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
