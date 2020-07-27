package acton.compiler.main.visitor.codeGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class JasminFile {

    private String className;
    private String superName;
    private ArrayList<String> fields = new ArrayList<>();
    private ArrayList<JasminMethod> methods = new ArrayList<>();

    JasminFile(String className,String superName){
        this.className = className;
        this.superName = superName;
    }

    void addMethod(JasminMethod method){
        methods.add(method);
    }

    void addField(String field){
        fields.add(field);
    }

    void writeFile(){
        try {
            if (!Files.exists(Paths.get("./output/"))){
                Files.createDirectory(Paths.get("./output"));
            }
            File file = new File("./output/"+className+".j");
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            StringBuilder builder = new StringBuilder();
            builder.append(InstructionGenerator.classDefinition(className));
            builder.append(InstructionGenerator.superClassDefifintion(superName));
            for(String field : fields){
                builder.append(field);
            }
            for(JasminMethod method : methods){
                builder.append(method.getMethod());
            }
            writer.write(builder.toString());
            writer.close();
        }
        catch (IOException e){
            System.err.println("cannot open file: " + "jasmin/"+className+".j");
            System.exit(1);
        }
    }
}
