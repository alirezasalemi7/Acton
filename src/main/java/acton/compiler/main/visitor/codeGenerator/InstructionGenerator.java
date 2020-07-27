package acton.compiler.main.visitor.codeGenerator;

public class InstructionGenerator {

    enum IfConditionMode {
        eq,ne,ge,gt,le,lt,nonNull,Null,aeq,ane,multiOperandEq,
        multiOperandNe,multiOperandGe,multiOperandGt,multiOperandLe,multiOperandLt
    }

    private final static String ENDLINE = "\n";

    static String iaload(){
        return "iaload"+ENDLINE;
    }

    static String stackLimit(int size){
        return ".limit stack "+ size + ENDLINE;
    }

    static String localsLimit(int size){
        return ".limit locals "+ size + ENDLINE;
    }

    static String classDefinition(String clsName){
        return ".class public " + clsName + ENDLINE;
    }

    static String superClassDefifintion(String clsName){
        return ".super " + clsName + ENDLINE;
    }

    static String endMethod(){
        return ".end method"+ENDLINE;
    }

    static String methodDefinition(String mthdName,String inputSig,String outputSig,boolean staticMthd){
        return ".method public " + ((staticMthd)?("static "):("")) + mthdName + "("+inputSig + ")" + outputSig + ENDLINE;
    }

    static String fieldDefinition(String name,String descriptor,String init){
        return ".field public " + name + " " + descriptor + ((init!=null)? " = " + init + ENDLINE : ENDLINE);
    }

    static String gotoLabel(String label){
        return "goto_w " + label + ENDLINE;
    }

    static String branchIf(String label,IfConditionMode mode){
        String cond;
        switch (mode){
            case eq: cond = "eq";break;
            case ge: cond = "ge";break;
            case gt: cond = "gt";break;
            case le: cond = "le";break;
            case lt: cond = "lt";break;
            case ne: cond = "ne";break;
            case Null: cond = "null";break;
            case nonNull: cond = "nonnull";break;
            case aeq: cond = "_acmpeq";break;
            case ane: cond = "_acmpne";break;
            case multiOperandEq: cond = "_icmpeq";break;
            case multiOperandNe: cond = "_icmpne";break;
            case multiOperandGe: cond = "_icmpge";break;
            case multiOperandGt: cond = "_icmpgt";break;
            case multiOperandLe: cond = "_icmple";break;
            case multiOperandLt: cond = "_icmplt";break;
            default: cond = "";
        }
        return  "if" + cond + " " + label + ENDLINE;
    }

    static String aload(int slot){
        return "aload " + slot + ENDLINE;
    }

    static String astore(int slot){
        return "astore " + slot + ENDLINE;
    }

    static String iload(int slot){
        return "iload " + slot + ENDLINE;
    }

    static String istore(int slot){
        return "istore " + slot + ENDLINE;
    }

    static String returnVoid(){
        return "return" + ENDLINE;
    }

    static String bipush(int value){
        return "ldc " + value + ENDLINE;
    }

    static String iinc(int slot,int value){
        return "iinc " + slot + " " + value + ENDLINE;
    }

    static String anew(String cls){
        return "new " + cls + ENDLINE;
    }

    static String inewarray(){
        return "newarray int" + ENDLINE;
    }

    static String pushString(String str){
        return "ldc " + str + ENDLINE;
    }

    static String iadd(){
        return "iadd" + ENDLINE;
    }

    static String isub(){
        return "isub" + ENDLINE;
    }

    static String imul(){
        return "imul" + ENDLINE;
    }

    static String idiv(){
        return "idiv" + ENDLINE;
    }

    static String irem(){
        return "irem" + ENDLINE;
    }

    static String ineg(){
        return "ineg" + ENDLINE;
    }

    static String iand(){
        return "iand" + ENDLINE;
    }

    static String ior(){
        return "ior" + ENDLINE;
    }

    static String getField(String name,String descriptor){
        return "getfield " + name + " " + descriptor + ENDLINE;
    }

    static String getStatic(String name,String descriptor){
        return "getstatic " + name + " " + descriptor + ENDLINE;
    }

    static String putField(String name,String descriptor){
        return "putfield " + name + " " + descriptor + ENDLINE;
    }

    static String putStatic(String name,String descriptor){
        return "getstatic " + name + " " + descriptor + ENDLINE;
    }

    static String invokeVirtual(String mthdName,String inputs,String returnType){
        return "invokevirtual " + mthdName + "(" + inputs + ")" + returnType + ENDLINE;
    }

    static String invokeStatic(String mthdName,String inputs,String returnType){
        return "invokestatic " + mthdName + "(" + inputs + ")" + returnType + ENDLINE;
    }

    static String invokeSpecial(String mthdName,String inputs,String returnType){
        return "invokespecial " + mthdName + "(" + inputs + ")" + returnType + ENDLINE;
    }

    static String dup(){
        return "dup" + ENDLINE;
    }

    static String iastore(){
        return "iastore"+ENDLINE;
    }

    static String mainMethod(){
        return ".method public static ";
    }
}
