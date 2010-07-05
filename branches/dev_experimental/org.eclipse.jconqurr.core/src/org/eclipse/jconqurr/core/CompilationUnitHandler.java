package org.eclipse.jconqurr.core;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class CompilationUnitHandler implements ICompilationUnitHandler {
	private ICompilationUnit compilationUnit;
	private ICompilationUnitFilter cuFilter;
	private CompilationUnit convertedUnit; 
	
	@Override
	public void convertToParallel() throws NullPointerException, JavaModelException {
		cuFilter.filter();
	}

	@Override
	public ICompilationUnit getConvertedCompilationUnit() {
		return (ICompilationUnit)this.convertedUnit;
	}

	@Override
	public void setCompilationUnit(ICompilationUnit cu) {
		this.compilationUnit = cu;
		if(cuFilter == null) {
			cuFilter = new CompilationUnitFilter();
		}
		cuFilter.setCompilationUnit(parse(this.compilationUnit));
	}
	
	/**
	 * Returns the compilation unit for a given ICompilationUnit reference
	 * 
	 * @param unit
	 * 		the ICompilationUnit reference to be parsed
	 * @return CompilationUnit
	 * 		the CompilationUnit object that has been passed successfully
	 */
	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

}
