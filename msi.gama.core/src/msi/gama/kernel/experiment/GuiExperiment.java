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
package msi.gama.kernel.experiment;

import java.util.*;
import msi.gama.common.interfaces.*;
import msi.gama.precompiler.GamlAnnotations.*;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.exceptions.*;
import msi.gama.util.*;
import msi.gaml.compilation.*;
import msi.gaml.descriptions.IDescription;
import msi.gaml.species.ISpecies;
import msi.gaml.types.IType;
import msi.gaml.variables.IVariable;

/**
 * Written by drogoul Modified on 28 mai 2011
 * 
 * @todo Description
 * 
 */
@symbol(name = { IKeyword.EXPERIMENT, IKeyword.GUI_ }, kind = ISymbolKind.EXPERIMENT)
@with_sequence
@facets({
	@facet(name = IKeyword.NAME, type = IType.LABEL, optional = false),
	@facet(name = IKeyword.TYPE, type = IType.LABEL, values = { IKeyword.BATCH, IKeyword.REMOTE,
		IKeyword.GUI_ }, optional = false) })
@inside(symbols = IKeyword.MODEL)
public class GuiExperiment extends AbstractExperiment {

	protected final List<IParameter> regularParameters;

	public GuiExperiment(final IDescription description) {
		super(description);
		regularParameters = new ArrayList();

	}

	@Override
	public IList<IParameter> getParametersToDisplay() {
		IList<IParameter> result = new GamaList();
		result.addAll(regularParameters);
		result.addAll(systemParameters);
		return result;
	}

	@Override
	protected void addOwnParameters() {
		ISpecies world = model.getWorldSpecies();
		for ( IVariable v : world.getVars() ) {
			if ( v.isParameter() ) {
				addRegularParameter(new ExperimentParameter(v));
			}
		}
		String cat = getSystemParametersCategory();
		addSystemParameter(new ExperimentParameter(world.getVar(GAMA._UQR),
			"Use quality rendering", cat, null, false));
		super.addOwnParameters();
	}

	@Override
	public void stopExperiment() {
		if ( currentSimulation != null ) {
			currentSimulation.stop();
		}
	}

	@Override
	public void startExperiment() throws GamaRuntimeException {
		startCurrentSimulation();
	}

	@Override
	public void stepExperiment() {
		if ( currentSimulation != null && !isLoading() ) {
			currentSimulation.step();
		}
	}

	@Override
	public void reloadExperiment() throws GamaRuntimeException, GamlException, InterruptedException {
		boolean wasRunning = isRunning() && !isPaused();
		closeCurrentSimulation(false);
		initializeExperiment();
		if ( wasRunning ) {
			startExperiment();
		}
	}

	@Override
	public boolean isGui() {
		return true;
	}

	@Override
	public void setChildren(final List<? extends ISymbol> children) throws GamlException {
		super.setChildren(children);
		for ( ISymbol s : children ) {
			if ( s instanceof IParameter ) {
				addRegularParameter((IParameter) s);
			}
		}
	}

	public void addRegularParameter(final IParameter p) {
		if ( registerParameter(p) ) {
			regularParameters.add(p);
		}
	}

	@Override
	public IList<String> getParametersNames() {
		final GamaList<String> result = new GamaList<String>();
		for ( final IParameter v : regularParameters ) {
			result.add(v.getName());
		}
		return result;
	}

}
