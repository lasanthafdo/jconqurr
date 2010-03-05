package org.eclipse.jconqurr.core.gpu;

import java.io.*;
import java.util.List;

import org.eclipse.jconqurr.core.ast.visitors.ForLoopVisitor;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class GPUHandler implements IGPUHandler{
	
private MethodDeclaration method;

	@Override
	public void process() {
		System.out.println("Called the process.......................");
		
		 ForLoopVisitor forLoopVisitor = new ForLoopVisitor();
		 this.method.accept(forLoopVisitor);
		 List<ForStatement> forLoops = forLoopVisitor.getForLoops();
		 for(ForStatement s : forLoops){
			 System.out.println(s.toString());
			 writeToCUFile(s);
		 }
		 System.out.println("end the process.......................");
	}

	@Override
	public void setMethod(MethodDeclaration method) {
		// TODO Auto-generated method stub
		this.method = method;
	}
	
	private void writeToCUFile(ForStatement s) {
		FileOutputStream fout;

		try
		{
		    fout = new FileOutputStream ("F:\\aaa77.cu");
		    new PrintStream(fout).println (s.toString());
		    fout.close();		
		}
		catch (IOException e)
		{
			System.err.println("Unable to write to file: " + e.getMessage());
		}
		
		/*try
		{
			FileWriter fstream = new FileWriter("C:\\Users\\Lahiru\\Desktop\\abbb77.txt");
			BufferedWriter out = new BufferedWriter(fstream);
		    out.write("Hello Java");
		    out.close();
		}
		catch (Exception e)
		{
		      System.err.println("Error: " + e.getMessage());
		}*/
		
		

	}

}
