package parsers.SearchBased;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import parsers.MethodCallVisitor;
import parsers.Metrics;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class Simulated_Annealing {
    private Solution next;
    private Solution best;
    private Solution s;
    private double temperature;//temperature
    private String path;
    public Simulated_Annealing(double temperature,String path){
        this.temperature = temperature;
        this.path = path;
        s = new CodeSolution(path); // init our entry point, since we deal with code, we dont have to randomly generate
        // initial solution.
    }

    public void findSolution(){
        best=s;
        while (temperature > 1) {
            // Create new Solution
            next= s.tweak();


            // Get the values
            double currentValue   = best.value();
            double newValue = next.value();
            System.out.println("current"+currentValue+"\t\t new"+newValue);

            double rand = randomDouble();
            if (newValue>currentValue || rand<acceptance(currentValue,newValue,temperature)) {
                s=next;
                temperature--;
            }

            // Keep track of the best solution found
            if (s.value() >best.value()) {
                best = s;
            }
        }

    }
    /**
     * implementation of the Solution Interface to fit our problem
     *
     */
    private class CodeSolution implements Solution{
        Metrics m;
        List<CompilationUnit> cu;
        public CodeSolution(String path) {

            try {
                m = new Metrics(path);
                cu = m.getCus();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        /**
         * randomly tweak the code to attempt improving the results
         */
        @Override
        public Solution tweak() {
         //size of the list of CUs
         int n = cu.size();
         // We randomly select a class to tweak
         int randomClassId = (int)(Math.random() * ((n)));
         //Now we have the class
         CompilationUnit compilationUnit = cu.get(randomClassId);
         // Name of the class
         String nameOfClass= compilationUnit
                 .findAll(ClassOrInterfaceDeclaration.class)
                 .get(0)
                 .getNameAsString();
         // we define the list of all methods defined within the class
         List<MethodDeclaration> declarationList = compilationUnit.findAll(ClassOrInterfaceDeclaration.class).get(0)
                                                   .findAll(MethodDeclaration.class)
                                                    .stream().collect(Collectors.toList());
         MethodCallVisitor m =  new MethodCallVisitor();
         List<MethodDeclaration> mdeclaration = compilationUnit
                 .getClassByName(nameOfClass)
                 .get()
                 .getMethods();
         //we group all the method calls inside each class
         List<String> methods_No_Calls = new LinkedList<>();
         for (MethodDeclaration method : mdeclaration ) {
                System.out.println(method.getName());
                method.accept(m, null);
         }


         /**
          * we group all the methods of the class with no calls to other methods of the same class
          *  a(){b()}: a and b are methods of the class-we can not move them to another class
          **  c(){no calls to methods of the class} so we can take b to another class
          * */
         methods_No_Calls = mdeclaration.stream()
                            .map(p->p.getNameAsString())
                            .filter(f->m.getMap().containsKey(f))
                            .collect(Collectors.toList());

          MethodDeclaration toSend=null;
            Refactorer refactorer=null;
         if (methods_No_Calls.size()>0){
            String name = methods_No_Calls.get(0);
             System.out.println(name);
             toSend = mdeclaration.stream().filter(p->p.getNameAsString().equalsIgnoreCase(name)).findAny().orElse(null);
            refactorer = new Refactorer(this.getClass().getPackage().getName().toString());
            refactorer.refactor(toSend);
         }

            return new CodeSolution(path);
        }
        /*
         */
        @Override
        public double value() {
            return Double.parseDouble(m.getTotalMetrics()+"");
        }

        public Metrics getM() {
            return m;
        }
    }

    public static double acceptance(double oldValue, double newValue,double temperature) {
        return Math.exp((newValue - oldValue) / temperature);
    }
    /**
     * this method returns a random number n such that
     * 0.0 <= n <= 1.0
     * @return random such that 0.0 <= random <= 1.0
     */
    static double randomDouble()
    {
        Random r = new Random();
        return r.nextInt(1000) / 1000.0;
    }
}
