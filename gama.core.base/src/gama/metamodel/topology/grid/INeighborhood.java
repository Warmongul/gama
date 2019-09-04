/*******************************************************************************************************
 *
 * gama.metamodel.topology.grid.INeighborhood.java, in plugin gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.metamodel.topology.grid;

import java.util.Set;

import gama.common.interfaces.IAgent;
import gama.runtime.scope.IScope;

/**
 * Class INeighborhood.
 *
 * @author drogoul
 * @since 19 mai 2013
 *
 */
public interface INeighborhood {

	public abstract Set<IAgent> getNeighborsIn(IScope scope, final int placeIndex, final int radius);

	public abstract boolean isVN();

	/**
	 * @param placeIndex
	 * @param range
	 * @return
	 */
	public abstract int[] getRawNeighborsIncluding(IScope scope, int placeIndex, int range);

	/**
	 * @param placeIndex
	 * @param n
	 * @return
	 */
	public abstract int neighborsIndexOf(IScope scope, int placeIndex, int n);

	/**
	 *
	 */
	public abstract void clear();

}