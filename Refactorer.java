package parsers.SearchBased;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.SecureRandom;

public class Refactorer {
    static final String AB = "ABC";
    static SecureRandom rnd = new SecureRandom();
    private String packagee;
    private String name;
    private CompilationUnit cu;
    private PrintWriter printWriter;
    private ClassOrInterfaceDeclaration classe;

    public Refactorer(String packagee) {
        this.packagee = packagee;
        this.name = randomString(2);
        cu = new CompilationUnit();
        cu.setPackageDeclaration(packagee);
        classe= cu.addClass(name).asClassOrInterfaceDeclaration();
    }

    public void refactor(MethodDeclaration methodDeclaration)
    {
        String methodeName = methodDeclaration.getNameAsString();
        classe.addMethod(methodeName, Modifier.PUBLIC,Modifier.STATIC)
                .setBody(methodDeclaration.getBody().get());


        try {
            printWriter = new PrintWriter(new File("src/main/java/"+packagee+"/test/"+name+".java"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        printWriter.write(cu.toString());
        printWriter.flush();
        printWriter.close();
    }

    public static String randomString( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }
    public CompilationUnit getCu() {
        return cu;
    }
}
