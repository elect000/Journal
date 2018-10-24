package chap13;
import static chap13.Opcode.*;
import static javassist.gluonj.GluonJ.revise;

import java.util.List;

import javassist.gluonj.*;
import stone.StoneException;
import stone.Token;
import stone.ast.*;
import chap11.EnvOptimizer;
import chap6.BasicEvaluator.ASTreeEx;
import chap6.Environment;
import chap7.FuncEvaluator;

@Require(EnvOptimizer.class)
@Reviser public class VmDisassembler {
    private static void emitNoIndent(Code c, String s) {
        System.out.printf("%04d: %s%n", c.position(), s);
    }
    public static void emitLabel(Code c, String label) {
        emitNoIndent(c, label + ":");
    }
    public static void emit(Code c, String s) {
        emitNoIndent(c, "  " + s);
    }
    private static int labelCounter = 0;
    public static String newLabel(String prefix) { return prefix + labelCounter++; }
    @Reviser public static interface EnvEx3 extends EnvOptimizer.EnvEx2 {
        StoneVM stoneVM();
        Code code();
    }
    @Reviser public static abstract class ASTreeVmEx extends ASTree {
        public void compile(Code c) {}
    }
    @Reviser public static class ASTListEx extends ASTList {
        public ASTListEx(List<ASTree> c) { super(c); }
        public void compile(Code c) {
            for (ASTree t: this)
                ((ASTreeVmEx)t).compile(c);
        }
    }
    @Reviser public static class DefStmntVmEx extends EnvOptimizer.DefStmntEx {
        public DefStmntVmEx(List<ASTree> c) { super(c); }
        @Override public Object eval(Environment env) {
            String funcName = name();
            EnvEx3 vmenv = (EnvEx3)env;
            Code code = vmenv.code();
            int entry = code.position();
            compile(code);
            ((EnvEx3)env).putNew(funcName, new VmFunction(parameters(), body(),
                                                          env, entry));
            return funcName;
        }
        public void compile(Code c) {
            c.nextReg = 0;
            c.frameSize = size + StoneVM.SAVE_AREA_SIZE;
            emit(c, "save " + size);
            c.add(SAVE);
            c.add(encodeOffset(size));
            ((ASTreeVmEx)revise(body())).compile(c);
            emit(c, "move r" + (c.nextReg - 1) + " " + 0);
            c.add(MOVE);
            c.add(encodeRegister(c.nextReg - 1));
            c.add(encodeOffset(0));
            emit(c, "restore " + size);
            c.add(RESTORE);
            c.add(encodeOffset(size));
            emit(c, "return");
            c.add(RETURN);
        }
    }
    @Reviser public static class ParamsEx2 extends EnvOptimizer.ParamsEx {
        public ParamsEx2(List<ASTree> c) { super(c); }
        @Override public void eval(Environment env, int index, Object value) {
            StoneVM vm = ((EnvEx3)env).stoneVM();
            vm.stack()[offsets[index]] = value;
        }
    }
    @Reviser public static class NumberEx extends NumberLiteral {
        public NumberEx(Token t) { super(t); }
        public void compile(Code c) {
            int v = value();
            if (Byte.MIN_VALUE <= v && v <= Byte.MAX_VALUE) {
                emit(c, "bconst " + v + " r" + c.nextReg);
                c.add(BCONST);
                c.add((byte)v);
            }
            else {
                emit(c,  "iconst " + v + " r" + c.nextReg);
                c.add(ICONST);
                c.add(v);
            }
            c.add(encodeRegister(c.nextReg++));
        }
    }
    @Reviser public static class StringEx extends StringLiteral {
        public StringEx(Token t) { super(t); }
        public void compile(Code c) {
            int i = c.record(value());
            emit(c, "sconst " + i + " r" + c.nextReg);
            c.add(SCONST);
            c.add(encodeShortOffset(i));
            c.add(encodeRegister(c.nextReg++));
        }
    }
    @Reviser public static class NameEx2 extends EnvOptimizer.NameEx {
        public NameEx2(Token t) { super(t); }
        public void compile(Code c) {
            if (nest > 0) {
                emit(c,  "gmove " + index + " r" + c.nextReg + "   // " + name());
                c.add(GMOVE);
                c.add(encodeShortOffset(index));
                c.add(encodeRegister(c.nextReg++));
            }
            else {
                emit(c, "move " + index + " r" + c.nextReg + "   // " + name());
                c.add(MOVE);
                c.add(encodeOffset(index));
                c.add(encodeRegister(c.nextReg++));
            }
        }
        public void compileAssign(Code c) {
            if (nest > 0) {
                emit(c,  "gmove r" + (c.nextReg - 1) + " " + index + "   // " + name());
                c.add(GMOVE);
                c.add(encodeRegister(c.nextReg - 1));
                c.add(encodeShortOffset(index));
            }
            else {
                emit(c, "move r" + (c.nextReg - 1) + " " + index + "   // " + name());
                c.add(MOVE);
                c.add(encodeRegister(c.nextReg - 1));
                c.add(encodeOffset(index));
            }
        }
    }
    @Reviser public static class NegativeEx extends NegativeExpr {
        public NegativeEx(List<ASTree> c) { super(c); }
        public void compile(Code c) {
            ((ASTreeVmEx)operand()).compile(c);
            emit(c, "neg r" + (c.nextReg - 1));
            c.add(NEG);
            c.add(encodeRegister(c.nextReg - 1));   
        }
    }
    @Reviser public static class BinaryEx extends BinaryExpr {
        public BinaryEx(List<ASTree> c) { super(c); }
        public void compile(Code c) {
            String op = operator();
            if (op.equals("=")) {
                ASTree l = left();
                if (l instanceof Name) {
                    ((ASTreeVmEx)right()).compile(c);
                    ((NameEx2)l).compileAssign(c);
                }
                else
                    throw new StoneException("bad assignment", this);
            }
            else {
                ((ASTreeVmEx)left()).compile(c);
                ((ASTreeVmEx)right()).compile(c);
                String opname = getOpName(op);
                emit(c, "" + opname + " r" + (c.nextReg - 2) + " r" + (c.nextReg - 1));
                c.add(getOpcode(op));
                c.add(encodeRegister(c.nextReg - 2));
                c.add(encodeRegister(c.nextReg - 1));
                c.nextReg--;
            }
        }
        protected byte getOpcode(String op) {
            if (op.equals("+"))
                return ADD;
            else if (op.equals("-"))
                return SUB;
            else if (op.equals("*"))
                return MUL;
            else if (op.equals("/"))
                return DIV;
            else if (op.equals("%"))
                return REM;
            else if (op.equals("=="))
                return EQUAL;
            else if (op.equals(">"))
                return MORE;
            else if (op.equals("<"))
                return LESS;
            else
                throw new StoneException("bad operator", this);
        }
        protected String getOpName(String op) {
            if (op.equals("+"))
                return "add";
            else if (op.equals("-"))
                return "sub";
            else if (op.equals("*"))
                return "mul";
            else if (op.equals("/"))
                return "div";
            else if (op.equals("%"))
                return "rem";
            else if (op.equals("=="))
                return "equal";
            else if (op.equals(">"))
                return "more";
            else if (op.equals("<"))
                return "less";
            else
                throw new StoneException("bad operator", this);
        }    
    }
    @Reviser public static class PrimaryVmEx extends FuncEvaluator.PrimaryEx {
        public PrimaryVmEx(List<ASTree> c) { super(c); }
        public void compile(Code c) {
            compileSubExpr(c, 0);
        }
        public void compileSubExpr(Code c, int nest) {
            if (hasPostfix(nest)) {
                compileSubExpr(c, nest + 1);
                ((ASTreeVmEx)revise(postfix(nest))).compile(c);
            }
            else
                ((ASTreeVmEx)operand()).compile(c);
        }
    }
    @Reviser public static class ArgumentsEx extends Arguments {
        public ArgumentsEx(List<ASTree> c) { super(c); }
        public void compile(Code c) {
            int newOffset = c.frameSize;
            int numOfArgs = 0;
            for (ASTree a: this) {
                ((ASTreeVmEx)a).compile(c);
                emit(c, "move r" + (c.nextReg - 1) + " " + newOffset);
                c.add(MOVE);
                c.add(encodeRegister(--c.nextReg));
                c.add(encodeOffset(newOffset++));
                numOfArgs++;
            }
            emit(c, "call " + (c.nextReg - 1) + " " + numOfArgs);
            c.add(CALL);
            c.add(encodeRegister(--c.nextReg));
            c.add(encodeOffset(numOfArgs));
            emit(c, "move " + c.frameSize + " r" + c.nextReg);
            c.add(MOVE);
            c.add(encodeOffset(c.frameSize));
            c.add(encodeRegister(c.nextReg++));
        }
        public Object eval(Environment env, Object value) {
            if (!(value instanceof VmFunction))
                throw new StoneException("bad function", this);
            VmFunction func = (VmFunction)value;
            ParameterList params = func.parameters();
            if (size() != params.size())
                throw new StoneException("bad number of arguments", this);
            int num = 0;
            for (ASTree a: this)
                ((ParamsEx2)params).eval(env, num++, ((ASTreeEx)a).eval(env)); 
            StoneVM svm = ((EnvEx3)env).stoneVM();
            svm.run(func.entry());
            return svm.stack()[0];
        }
    }
    @Reviser public static class BlockEx extends BlockStmnt {
        public BlockEx(List<ASTree> c) { super(c); }
        public void compile(Code c) {
            if (this.numChildren() > 0) {
                int initReg = c.nextReg;
                for (ASTree a: this) {
                    c.nextReg = initReg;
                    ((ASTreeVmEx)a).compile(c);
                }
            }
            else {
                emit(c, "bconst 0 " + c.nextReg);
                c.add(BCONST);
                c.add((byte)0);
                c.add(encodeRegister(c.nextReg++));
            }
        }
    }
    @Reviser public static class IfEx extends IfStmnt {
        public IfEx(List<ASTree> c) { super(c); }
        public void compile(Code c) {
            ((ASTreeVmEx)condition()).compile(c);
            int pos = c.position();
            String lab = newLabel("L_IF");
            emit(c, "ifzero r" + (c.nextReg - 1) + " " + lab);
            c.add(IFZERO);
            c.add(encodeRegister(--c.nextReg));
            c.add(encodeShortOffset(0));
            int oldReg = c.nextReg;
            ((ASTreeVmEx)thenBlock()).compile(c);
            int pos2 = c.position();
            String lab2 = newLabel("L_IF");
            emit(c, "goto " + lab2);
            c.add(GOTO);
            c.add(encodeShortOffset(0));
            c.set(encodeShortOffset(c.position() - pos), pos + 2);
            emitLabel(c, lab);
            ASTree b = elseBlock();
            c.nextReg = oldReg;
            if (b != null)
                ((ASTreeVmEx)b).compile(c);
            else {
                emit(c,  "bconst 0 r" + c.nextReg);
                c.add(BCONST);
                c.add((byte)0);
                c.add(encodeRegister(c.nextReg++));
            }
            emitLabel(c, lab2);
            c.set(encodeShortOffset(c.position() - pos2), pos2 + 1);
        }
    }
    @Reviser public static class WhileEx extends WhileStmnt {
        public WhileEx(List<ASTree> c) { super(c); }
        public void compile(Code c) {
            int oldReg = c.nextReg;
            emit(c, "bconst 0 r" + c.nextReg);
            c.add(BCONST);
            c.add((byte)0);
            c.add(encodeRegister(c.nextReg++));
            int pos = c.position();
            String lab = newLabel("L_WHILE");
            emitLabel(c, lab);
            ((ASTreeVmEx)condition()).compile(c);
            int pos2 = c.position();
            String lab2 = newLabel("L_WHILE");
            emit(c, "ifzero r" + (c.nextReg - 1) + " " + lab2);
            c.add(IFZERO);
            c.add(encodeRegister(--c.nextReg));
            c.add(encodeShortOffset(0));
            c.nextReg = oldReg;
            ((ASTreeVmEx)body()).compile(c);
            int pos3= c.position();
            emit(c, "goto " + lab);
            c.add(GOTO);
            c.add(encodeShortOffset(pos - pos3));
            c.set(encodeShortOffset(c.position() - pos2), pos2 + 2);
            emitLabel(c, lab2);
        }
    }
}


DisasmRunner.java
package final1;
import javassist.gluonj.util.Loader;
import chap13.VmDisassembler;
import chap13.VmInterpreter;
import chap8.NativeEvaluator;

public class DisasmRunner {
    public static void main(String[] args) throws Throwable {
        Loader.run(VmInterpreter.class, args, VmDisassembler.class,
                                              NativeEvaluator.class);
    }
}



Cへのトランスレート
VmCTranslator.java
package chap13;
import java.util.List;
import stone.StoneException;
import stone.Token;
import chap11.EnvOptimizer;
import chap6.Environment;
import chap6.BasicEvaluator.ASTreeEx;
import chap7.FuncEvaluator;
import javassist.gluonj.*;
import static chap13.Opcode.*;
import static javassist.gluonj.GluonJ.revise;
import stone.ast.*;

@Require(EnvOptimizer.class)
@Reviser public class VmCTranslator {
    public static void emitNoIndent(String s) {
        System.out.printf("%s%n", s);
    }
    public static void emit(String s) {
        emitNoIndent("  " + s);
    }

    @Reviser public static interface EnvEx3 extends EnvOptimizer.EnvEx2 {
        StoneVM stoneVM();
        Code code();
    }
    @Reviser public static abstract class ASTreeVmEx extends ASTree {
        public void compile(Code c) {}
    }
    @Reviser public static class ASTListEx extends ASTList {
        public ASTListEx(List<ASTree> c) { super(c); }
        public void compile(Code c) {
            for (ASTree t: this)
                ((ASTreeVmEx)t).compile(c);
        }
    }
    @Reviser public static class DefStmntVmEx extends EnvOptimizer.DefStmntEx {
        public DefStmntVmEx(List<ASTree> c) { super(c); }
        @Override public Object eval(Environment env) {
            String funcName = name();
            EnvEx3 vmenv = (EnvEx3)env;
            Code code = vmenv.code();
            int entry = code.position();
            compile(code);
            ((EnvEx3)env).putNew(funcName, new VmFunction(parameters(), body(),
                                                          env, entry));
            return funcName;
        }
        public void compile(Code c) {
            c.nextReg = 0;
            c.frameSize = size + StoneVM.SAVE_AREA_SIZE;
            emit("// save " + size);
            emitNoIndent("#include <stdio.h>");
            emitNoIndent("int main(void) {");
            emit("int registers[6], stack[1000], heap[1000], *sp = stack, *fp = stack;");

            c.add(SAVE);
            c.add(encodeOffset(size));
            ((ASTreeVmEx)revise(body())).compile(c);
            emit("fp[0] = registers[" + (c.nextReg - 1) + "];");
            c.add(MOVE);
            c.add(encodeRegister(c.nextReg - 1));
            c.add(encodeOffset(0));
            emit("// restore " + size);
            emit("printf(\"%d\\n\", fp[0]);");

            c.add(RESTORE);
            c.add(encodeOffset(size));
            emit("// return");
            emit("return 0;");
            emitNoIndent("}");
            c.add(RETURN);
        }
    }
    @Reviser public static class ParamsEx2 extends EnvOptimizer.ParamsEx {
        public ParamsEx2(List<ASTree> c) { super(c); }
        @Override public void eval(Environment env, int index, Object value) {
            StoneVM vm = ((EnvEx3)env).stoneVM();
            vm.stack()[offsets[index]] = value;
        }
    }
    @Reviser public static class NumberEx extends NumberLiteral {
        public NumberEx(Token t) { super(t); }
        public void compile(Code c) {
            int v = value();
            emit("registers[" + c.nextReg + "] = " + v + ";");
            if (Byte.MIN_VALUE <= v && v <= Byte.MAX_VALUE) {
                c.add(BCONST);
                c.add((byte)v);
            }
            else {
                c.add(ICONST);
                c.add(v);
            }
            c.add(encodeRegister(c.nextReg++));
        }
    }
    @Reviser public static class StringEx extends StringLiteral {
        public StringEx(Token t) { super(t); }
        public void compile(Code c) {
            int i = c.record(value());
            emit("// sconst " + i + " r" + c.nextReg);
            c.add(SCONST);
            c.add(encodeShortOffset(i));
            c.add(encodeRegister(c.nextReg++));
        }
    }
    @Reviser public static class NameEx2 extends EnvOptimizer.NameEx {
        public NameEx2(Token t) { super(t); }
        public void compile(Code c) {
            if (nest > 0) {
                emit("registers[" + c.nextReg + "] = heapMemory[" + index + "];   // " + name());
                c.add(GMOVE);
                c.add(encodeShortOffset(index));
                c.add(encodeRegister(c.nextReg++));
            }
            else {
                emit("registers[" + c.nextReg + "] = fp[" + index + "];   // " + name());
                c.add(MOVE);
                c.add(encodeOffset(index));
                c.add(encodeRegister(c.nextReg++));
            }
        }
        public void compileAssign(Code c) {
            if (nest > 0) {
                emit("heapMemory[" + index + "] = registers[" + (c.nextReg - 1) + "];   // " + name());
                c.add(GMOVE);
                c.add(encodeRegister(c.nextReg - 1));
                c.add(encodeShortOffset(index));
            }
            else {
                emit("fp[" + index + "] = registers[" + (c.nextReg - 1) + "];   // " + name());
                c.add(MOVE);
                c.add(encodeRegister(c.nextReg - 1));
                c.add(encodeOffset(index));
            }
        }
    }
    @Reviser public static class NegativeEx extends NegativeExpr {
        public NegativeEx(List<ASTree> c) { super(c); }
        public void compile(Code c) {
            ((ASTreeVmEx)operand()).compile(c);
            emit("registers[" + (c.nextReg - 1) + "] *= -1;");
            c.add(NEG);
            c.add(encodeRegister(c.nextReg - 1));   
        }
    }
    @Reviser public static class BinaryEx extends BinaryExpr {
        public BinaryEx(List<ASTree> c) { super(c); }
        public void compile(Code c) {
            String op = operator();
            if (op.equals("=")) {
                ASTree l = left();
                if (l instanceof Name) {
                    ((ASTreeVmEx)right()).compile(c);
                    ((NameEx2)l).compileAssign(c);
                }
                else
                    throw new StoneException("bad assignment", this);
            }
            else {
                ((ASTreeVmEx)left()).compile(c);
                ((ASTreeVmEx)right()).compile(c);
                emit("registers[" + (c.nextReg - 2) + "] = registers[" + (c.nextReg - 2) + "] " + op + " registers[" + (c.nextReg - 1) + "];");
                c.add(getOpcode(op));
                c.add(encodeRegister(c.nextReg - 2));
                c.add(encodeRegister(c.nextReg - 1));
                c.nextReg--;
            }
        }
        protected byte getOpcode(String op) {
            if (op.equals("+"))
                return ADD;
            else if (op.equals("-"))
                return SUB;
            else if (op.equals("*"))
                return MUL;
            else if (op.equals("/"))
                return DIV;
            else if (op.equals("%"))
                return REM;
            else if (op.equals("=="))
                return EQUAL;
            else if (op.equals(">"))
                return MORE;
            else if (op.equals("<"))
                return LESS;
            else
                throw new StoneException("bad operator", this);
        }
    }
    @Reviser public static class PrimaryVmEx extends FuncEvaluator.PrimaryEx {
        public PrimaryVmEx(List<ASTree> c) { super(c); }
        public void compile(Code c) {
            compileSubExpr(c, 0);
        }
        public void compileSubExpr(Code c, int nest) {
            if (hasPostfix(nest)) {
                compileSubExpr(c, nest + 1);
                ((ASTreeVmEx)revise(postfix(nest))).compile(c);
            }
            else
                ((ASTreeVmEx)operand()).compile(c);
        }
    }
    @Reviser public static class ArgumentsEx extends Arguments {
        public ArgumentsEx(List<ASTree> c) { super(c); }
        public void compile(Code c) {
            int newOffset = c.frameSize;
            int numOfArgs = 0;
            for (ASTree a: this) {
                ((ASTreeVmEx)a).compile(c);
                emit("fp[" + newOffset + "] = registers[" + (c.nextReg - 1) + "];");
                c.add(MOVE);
                c.add(encodeRegister(--c.nextReg));
                c.add(encodeOffset(newOffset++));
                numOfArgs++;
            }
            emit("// call " + (c.nextReg - 1) + " " + numOfArgs);
            c.add(CALL);
            c.add(encodeRegister(--c.nextReg));
            c.add(encodeOffset(numOfArgs));
            emit("registers[" + c.nextReg + "] = fp[" + c.frameSize + "]");
            c.add(MOVE);
            c.add(encodeOffset(c.frameSize));
            c.add(encodeRegister(c.nextReg++));
        }
        public Object eval(Environment env, Object value) {
            if (!(value instanceof VmFunction))
                throw new StoneException("bad function", this);
            VmFunction func = (VmFunction)value;
            ParameterList params = func.parameters();
            if (size() != params.size())
                throw new StoneException("bad number of arguments", this);
            int num = 0;
            for (ASTree a: this)
                ((ParamsEx2)params).eval(env, num++, ((ASTreeEx)a).eval(env)); 
            StoneVM svm = ((EnvEx3)env).stoneVM();
            svm.run(func.entry());
            return svm.stack()[0];
        }
    }
    @Reviser public static class BlockEx extends BlockStmnt {
        public BlockEx(List<ASTree> c) { super(c); }
        public void compile(Code c) {
            if (this.numChildren() > 0) {
                int initReg = c.nextReg;
                for (ASTree a: this) {
                    c.nextReg = initReg;
                    ((ASTreeVmEx)a).compile(c);
                }
            }
            else {
                emit("registers[" + c.nextReg + "] = 0;");
                c.add(BCONST);
                c.add((byte)0);
                c.add(encodeRegister(c.nextReg++));
            }
        }
    }
    @Reviser public static class IfEx extends IfStmnt {
        public IfEx(List<ASTree> c) { super(c); }
        static int ifLabel = 0;
        public void compile(Code c) {
            ((ASTreeVmEx)condition()).compile(c);
            int pos = c.position();
            int lab = ifLabel++;
            emit("if (registers[" + (c.nextReg - 1) + "] == 0) goto L_if" + lab + ";");
            c.add(IFZERO);
            c.add(encodeRegister(--c.nextReg));
            c.add(encodeShortOffset(0));
            int oldReg = c.nextReg;
            ((ASTreeVmEx)thenBlock()).compile(c);
            int pos2 = c.position();
            int lab2 = ifLabel++;
            emit("goto L_IF" + lab2 + ";");
            c.add(GOTO);
            c.add(encodeShortOffset(0));
            c.set(encodeShortOffset(c.position() - pos), pos + 2);
            emitNoIndent("L_IF" + lab + ":");
            ASTree b = elseBlock();
            c.nextReg = oldReg;
            if (b != null)
                ((ASTreeVmEx)b).compile(c);
            else {
                emit("registers[" + c.nextReg + "] = 0;");
                c.add(BCONST);
                c.add((byte)0);
                c.add(encodeRegister(c.nextReg++));
            }
            emitNoIndent("L_IF" + lab2 + ":");
            c.set(encodeShortOffset(c.position() - pos2), pos2 + 1);
        }
    }
    @Reviser public static class WhileEx extends WhileStmnt {
        public WhileEx(List<ASTree> c) { super(c); }
        static int whileLabel = 0;
        public void compile(Code c) {
            int oldReg = c.nextReg;
            emit("registers[" + c.nextReg + "] = 0;");
            c.add(BCONST);
            c.add((byte)0);
            c.add(encodeRegister(c.nextReg++));
            int pos = c.position();
            int lab = whileLabel++;
            emitNoIndent("L_WHILE" + lab + ":");
            ((ASTreeVmEx)condition()).compile(c);
            int pos2 = c.position();
            int lab2 = whileLabel++;
            emit("if (registers[" + (c.nextReg - 1) + "] == 0) goto L_WHILE" + lab2 + ";");
            c.add(IFZERO);
            c.add(encodeRegister(--c.nextReg));
            c.add(encodeShortOffset(0));
            c.nextReg = oldReg;
            ((ASTreeVmEx)body()).compile(c);
            int pos3= c.position();
            emit("goto L_WHILE" + lab + ";");
            c.add(GOTO);
            c.add(encodeShortOffset(pos - pos3));
            c.set(encodeShortOffset(c.position() - pos2), pos2 + 2);
            emitNoIndent("L_WHILE" + lab2 + ":");
        }
    }
}



CTransRunner.java
package final2;
import javassist.gluonj.util.Loader;
import chap13.VmCTranslator;
import chap13.VmInterpreter;
import chap8.NativeEvaluator;

public class CTransRunner {
    public static void main(String[] args) throws Throwable {
        Loader.run(VmInterpreter.class, args, VmCTranslator.class,
                                              NativeEvaluator.class);
    }
}



Stone言語の拡張

FinalReportParser.java
// switch文，for文，labelとbreak文の文法拡張を行うパーサ



package stone;
import static stone.Parser.rule;
import stone.ast.*;

public class FinalReportParser extends ClosureParser {
    private void addSwitchRules() { // switch文の文法ルールを加える
        Parser clause = rule(CaseClause.class).sep("case").number(NumberLiteral.class).ast(block);
        Parser clauses = rule(ClauseList.class).sep("{").option(clause).
                repeat(rule().sep(";", Token.EOL).option(clause)).sep("}");
        statement.insertChoice(rule(SwitchStmnt.class).sep("switch").ast(expr).ast(clauses));        
    }
    private void addForRules() { // for文の文法ルールを加える
        statement.insertChoice(rule(ForStmnt.class).sep("for").ast(expr).sep(";").ast(expr).sep(";").ast(expr).ast(block));
    }
    private void addBreakRules() { // break文とlabelの文法ルールを加える
        statement.insertChoice(rule(BreakStmnt.class).sep("break").identifier(Name.class, reserved).sep(",").ast(expr));
        block.insertChoice(rule(LabeledBlockStmnt.class).sep("label").identifier(Name.class, reserved).sep(":").ast(block));
    }

    public FinalReportParser() {
        addSwitchRules();
        addForRules();
        addBreakRules();
    }

}




ExtInterpreter.java
// FinalReportParserを用いるインタプリタ．

package final3;

import chap7.NestedEnv;
import chap8.NativeInterpreter;
import chap8.Natives;
import stone.ParseException;
import stone.FinalReportParser;

public class ExtInterpreter extends NativeInterpreter {

    public static void main(String[] args) throws ParseException {
        run(new FinalReportParser(),
                new Natives().environment(new NestedEnv()));
    }
}



ExtRunner.java
//すべての拡張を実現するRunner

package final3;

import chap7.ClosureEvaluator;
import chap8.NativeEvaluator;
import javassist.gluonj.util.Loader;

public class ExtRunner {
    public static void main(String[] args) throws Throwable {
        Loader.run(ExtInterpreter.class, args, 
                ClosureEvaluator.class, NativeEvaluator.class, SwitchEvaluator.class, ForEvaluator.class, BreakEvaluator.class);
    }
}


// switch文


SwitchStmnt.java
package stone.ast;

import java.util.List;

public class SwitchStmnt extends ASTList {
    public SwitchStmnt(List<ASTree> list) {
        super(list);
    }
    public ASTree expression() {
        return child(0);
    }
    public ASTree clauses() {
        return child(1);
    }
    public String toString() {
        return "(switch " + expression() + " " + clauses() + ")";
    }
    

}



ClauseList.java
package stone.ast;

import java.util.List;

public class ClauseList extends ASTList {
    public ClauseList(List<ASTree> list) {
        super(list);
    }
}



CaseClause.java
package stone.ast;

import java.util.List;

public class CaseClause extends ASTList {
    public CaseClause(List<ASTree> list) {
        super(list);
    }
    public ASTree constant() { return child(0); }
    public ASTree block() { return child(1); }
    public String toString() {
        return "(case " + constant() + " " + block() + ")";
    }
}



SwitchEvaluator.java
package final3;

import java.util.List;

import chap6.BasicEvaluator;
import chap6.Environment;
import chap8.NativeEvaluator;
import javassist.gluonj.Require;
import javassist.gluonj.Reviser;
import stone.ast.ASTree;
import stone.ast.CaseClause;
import stone.ast.SwitchStmnt;

@Require(NativeEvaluator.class)
@Reviser
public class SwitchEvaluator {
    @Reviser
    public static class SwitchStmntEx extends SwitchStmnt {
        public SwitchStmntEx(List<ASTree> c) {
            super(c);
        }
        public Object eval(Environment env) {
            Object key = ((BasicEvaluator.ASTreeEx)expression()).eval(env);
            for (ASTree clause : clauses()) {
                CaseClause c = (CaseClause)clause;
                if (((BasicEvaluator.ASTreeEx)c.constant()).eval(env).equals(key)) {
                    return ((BasicEvaluator.ASTreeEx)c.block()).eval(env);
                }
            }
            return null;
        }
    }
}



// for文



ForStmnt.java
package stone.ast;

import java.util.List;

public class ForStmnt extends ASTList {

    public ForStmnt(List<ASTree> list) {
        super(list);
    }
    public ASTree initExpr() { return child(0); }
    public ASTree condExpr() { return child(1); }
    public ASTree stepExpr() { return child(2); }
    public ASTree block() { return child(3); }

}



ForEvaluator.java
package final3;

import java.util.List;

import chap6.Environment;
import chap8.NativeEvaluator;
import javassist.gluonj.Require;
import javassist.gluonj.Reviser;
import stone.ast.ASTree;
import stone.ast.ForStmnt;
import chap6.BasicEvaluator.ASTreeEx;

@Require(NativeEvaluator.class)
@Reviser
public class ForEvaluator {
    @Reviser
    public static class ForStmntEx extends ForStmnt {
        public ForStmntEx(List<ASTree> c) {
            super(c);
        }
        public Object eval(Environment env) {
            ((ASTreeEx)initExpr()).eval(env);
            ASTreeEx cond = (ASTreeEx)condExpr();
            ASTreeEx step = (ASTreeEx)stepExpr();
            ASTreeEx block = (ASTreeEx)block();
            Object lastValue = null;
            while (true) {
                Object c = cond.eval(env);
                if (c.equals(0)) {
                    return lastValue;
                }
                lastValue = block.eval(env);
                step.eval(env);
            }
        }
    }

}



// break文とラベル付きブロック

BreakStmnt.java
package stone.ast;

import java.util.List;

public class BreakStmnt extends ASTList {

    public BreakStmnt(List<ASTree> list) {
        super(list);
    }
    public ASTree label() { return child(0); }
    public ASTree expr() { return child(1); }

}



LabeledBlockStmnt.java
package stone.ast;

import java.util.List;

public class LabeledBlockStmnt extends BlockStmnt {

    public LabeledBlockStmnt(List<ASTree> c) {
        super(c);
    }
    public ASTree label() {
        return child(0);
    }
    public ASTree block() {
        return child(1);
    }

}



BreakEvaluator.java
package final3;

import java.util.List;

import chap6.BasicEvaluator.ASTreeEx;
import chap6.Environment;
import chap8.NativeEvaluator;
import javassist.gluonj.*;
import stone.ast.*;

@Require(NativeEvaluator.class)
@Reviser
public class BreakEvaluator {
    public static class StoneBreakException extends RuntimeException {
        protected String label;
        protected Object value;
        public StoneBreakException(String label, Object value) {
            this.label = label;
            this.value = value;
        }
        public String getLabel() { return label; }
        public Object getValue() { return value; }
    }
    @Reviser
    public static class BreakStmntEx extends BreakStmnt {
        public BreakStmntEx(List<ASTree> c) {
            super(c);
        }
        public Object eval(Environment env) {
            String label = ((Name)label()).name();
            Object value = ((ASTreeEx)expr()).eval(env);
            throw new StoneBreakException(label, value);
        }
    }
    @Reviser
    public static class LabeledBlockStmntEx extends LabeledBlockStmnt {

        public LabeledBlockStmntEx(List<ASTree> c) {
            super(c);
        }
        public Object eval(Environment env) {
            String label = ((Name)label()).name();
            try {
                return ((ASTreeEx)block()).eval(env);
            } catch (StoneBreakException e) {
                if (e.getLabel().equals(label)) {
                    return e.getValue();
                } else {
                    throw e;
                }
            }
        }
    }
}


