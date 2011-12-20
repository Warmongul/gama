/*
 * GAMA - V1.4  http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2012
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2012
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen  (Batch, GeoTools & JTS), 2009-2012
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2012
 * - Phan Huy Cuong, DREAM team, Univ. Can Tho (XText-based GAML), 2012
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gaml.commands;

import java.util.Map;
import msi.gama.common.interfaces.*;
import msi.gama.precompiler.GamlAnnotations.*;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.*;
import msi.gaml.compilation.*;
import msi.gaml.descriptions.*;
import msi.gaml.species.ISpecies;
import msi.gaml.types.IType;

/**
 * Written by drogoul Modified on 7 févr. 2010
 * 
 * @todo Description
 * 
 */
@symbol(name = { IKeyword.DO, IKeyword.REPEAT }, kind = ISymbolKind.SINGLE_COMMAND)
@inside(kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_COMMAND })
@facets({ @facet(name = IKeyword.ACTION, type = IType.ID, optional = false),
	@facet(name = IKeyword.WITH, type = IType.MAP_STR, optional = true) })
@with_args
@no_scope
public class DoCommand extends AbstractCommandSequence implements ICommand.WithArgs {

	Arguments args;

	public DoCommand(final IDescription desc) {
		super(desc);
		setName(getLiteral(IKeyword.ACTION));
	}

	public Arguments getArgs() {
		return args;
	}

	@Override
	public void setFormalArgs(final Arguments args) throws GamlException {
		verifyArgs(args);
		this.args = args;
	}

	@Override
	public Object privateExecuteIn(final IScope stack) throws GamaRuntimeException {
		ISpecies context = stack.getAgentScope().getSpecies(); // TODO change to
																// getAgentScope.getExecutionContext???
		ICommand.WithArgs executer = context.getAction(name);
		executer.setRuntimeArgs(args);
		Object result = executer.executeOn(stack);
		return result;
	}

	public void verifyArgs(final Map<String, ?> args) throws GamlException {
		CommandDescription executer =
			((ExecutionContextDescription) description.getDescriptionDeclaringAction(name))
				.getAction(name);
		if ( executer == null ) { throw new GamlException("Unknown action " + getName()); }
		executer.verifyArgs(args.keySet());
	}

	@Override
	public void setRuntimeArgs(final Arguments args) {}

	@Override
	public IType getReturnType() {
		CommandDescription executer =
			(CommandDescription) description.getSpeciesContext().getAction(name);
		return executer.getReturnType();
	}

	@Override
	public IType getReturnContentType() {
		CommandDescription executer =
			(CommandDescription) description.getSpeciesContext().getAction(name);
		return executer.getReturnContentType();
	}
}
