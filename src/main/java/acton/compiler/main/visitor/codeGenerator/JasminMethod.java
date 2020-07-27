package acton.compiler.main.visitor.codeGenerator;

import acton.compiler.main.ast.node.declaration.VarDeclaration;

import java.util.ArrayList;

public class JasminMethod {

    private int stackLimit;
    private int localsLimit;
    private String methodName;
    private String inputSig;
    private String outputSig;
    private ArrayList<String> byteCodes = new ArrayList<>();
    private SlotMapper mapper;
    boolean staticMthd = false;

    JasminMethod(int stackLimit,int localsLimit,String methodName,String inputSig,String outputSig){
        this.stackLimit = stackLimit;
        this.localsLimit = localsLimit;
        this.methodName = methodName;
        this.inputSig = inputSig;
        this.outputSig = outputSig;
    }

    public int getLocalsLimit() {
        return localsLimit;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getInputSig() {
        return inputSig;
    }

    public String getOutputSig() {
        return outputSig;
    }

    void addByteCode(String code){
        byteCodes.add(code);
    }

    String getMethod(){
        StringBuilder builder = new StringBuilder();
        builder.append(InstructionGenerator.methodDefinition(methodName,inputSig,outputSig,staticMthd));
        builder.append(InstructionGenerator.stackLimit(stackLimit));
        builder.append(InstructionGenerator.localsLimit(localsLimit));
        for(String code : byteCodes){
            builder.append(code);
        }
        builder.append(InstructionGenerator.endMethod());
        return builder.toString();
    }

    void setSlotMapper(ArrayList<VarDeclaration> args,ArrayList<VarDeclaration> locals,boolean init){
        ArrayList<VarDeclaration> newVars = new ArrayList<>();
        newVars.addAll(args);newVars.addAll(locals);
        mapper = new SlotMapper(newVars,init);
    }


    public SlotMapper getMapper() {
        return mapper;
    }

}
