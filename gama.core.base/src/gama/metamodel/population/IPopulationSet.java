/*******************************************************************************************************
 *
 * gama.metamodel.population.IPopulationSet.java, in plugin gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.metamodel.population;

import java.util.Collection;

import gama.common.interfaces.IAgent;
import gama.common.interfaces.IContainer;
import gama.metamodel.topology.filter.IAgentFilter;
import gama.runtime.scope.IScope;
import one.util.streamex.StreamEx;

/**
 * Class IPopulationSet. An interface common to ISpecies, IPopulation and
 * MetaPopulation
 * 
 * @author drogoul
 * @since 9 déc. 2013
 * 
 */
public interface IPopulationSet<T extends IAgent> extends IContainer<Integer, T>, IAgentFilter {

	Collection<? extends IPopulation<? extends IAgent>> getPopulations(IScope scope);

	@Override
	StreamEx<T> stream(final IScope scope);

}