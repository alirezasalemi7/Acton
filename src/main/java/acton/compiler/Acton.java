package acton.compiler;

import acton.compiler.main.ast.node.Program;
import acton.compiler.main.compileError.CompileErrorException;
import acton.compiler.main.visitor.codeGenerator.CodeGenerator;
import acton.compiler.main.visitor.nameAnalyser.NameAnalyser;
import acton.compiler.main.visitor.typeChecker.TypeChecker;
import acton.compiler.parsers.actonLexer;
import acton.compiler.parsers.actonParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;

public class Acton {
    public static void main(String[] args) throws IOException {
        CharStream reader = CharStreams.fromFileName(args[0]);
        actonLexer lexer = new actonLexer(reader);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        actonParser parser = new actonParser(tokens);
        Program program = null;
        try{
            program = parser.program().p; // program is starting production rule
            NameAnalyser nameAnalyser = new NameAnalyser();
            nameAnalyser.visit(program);
            if( nameAnalyser.numOfErrors() > 0 )
                throw new CompileErrorException();
            TypeChecker typeChecker = new TypeChecker();
            typeChecker.visit(program);
            ArrayList<String> errors = typeChecker.getErrors();
            for (String error : errors) {
                System.out.println(error);
                System.exit(1);
            }
            CodeGenerator generator = new CodeGenerator();
            generator.visit(program);
            createClassFiles();
        }
        catch(CompileErrorException compileError){
        }
    }

    private static void createClassFiles(){
        try {
            Files.copy(Paths.get("./src/main/resources/jfile/Actor.j"), Paths.get("./output/Actor.j"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get("./src/main/resources/jfile/Message.j"), Paths.get("./output/Message.j"), StandardCopyOption.REPLACE_EXISTING);
            File dir = new File("./output/");
            String[] classes = Arrays.stream(dir.list())
                    .map(s -> "output/".concat(s))
                    .toArray(i -> new String[i]);
            ArrayList<String> options = new ArrayList<>(Arrays.asList(classes));
            options.add("-d");
            options.add("output_classes/");
            jasmin.Main.main(options.toArray(i -> new String[i]));
            removeJasmin();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void removeJasmin() throws IOException{
        Files.walkFileTree(Paths.get("./output"),new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}