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
package msi.gama.metamodel.topology.graph;

import java.util.List;
import msi.gama.common.interfaces.*;
import msi.gama.metamodel.shape.*;
import msi.gama.metamodel.topology.*;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.*;
import msi.gama.util.graph.*;
import org.jgrapht.Graphs;

public class GamaSpatialGraph extends GamaGraph<IShape, IShape> {

	/**
	 * Determines the relationship among two polygons.
	 */
	public static interface VertexRelationship {

		/**
		 * Determines if two vertex geometries are to be treated as related in any way.
		 * @param p1 a geometrical object
		 * @param p2 another geometrical object
		 */
		boolean related(IScope scope, IShape p1, IShape p2);

		boolean equivalent(IShape p1, IShape p2);

	}

	protected VertexRelationship vertexRelation;

	public GamaSpatialGraph(final IScope scope, final IContainer vertices, final boolean byEdge,
		final boolean directed, final VertexRelationship rel) {
		super(scope, vertices, byEdge, directed);
		vertexRelation = rel;
	}

	@Override
	protected Object createNewEdgeObjectFromVertices(final Object v1, final Object v2) {
		if ( v1 instanceof IShape && v2 instanceof IShape ) { return new GamaDynamicLink(
			(IShape) v1, (IShape) v2); }
		return super.createNewEdgeObjectFromVertices(v1, v2);
	}

	@Override
	public GamaSpatialGraph copy() {
		GamaSpatialGraph g =
			new GamaSpatialGraph(scope, GamaList.EMPTY_LIST, true, directed, vertexRelation);
		Graphs.addAllEdges(g, this, this.edgeSet());
		return g;
	}

	@Override
	public IPath computeShortestPathBetween(final ITopology topology, final Object source,
		final Object target) {
		return (IPath) super.computeShortestPathBetween(topology, source, target);
	}

	@Override
	protected IPath pathFromEdges(final ITopology topology, final Object source,
		final Object target, final IList edges) {
		return new GamaPath(topology, (IShape) source, (IShape) target, edges);
	}

	@Override
	protected void buildByVertices(final IContainer<?, IShape> list) {
		super.buildByVertices(list);
		for ( IShape o1 : list ) { // Try to create automatic edges
			for ( IShape o2 : list ) {
				if ( o1 == o2 || vertexRelation.equivalent(o1, o2) ) {
					continue;
				}
				if ( vertexRelation.related(scope, o1, o2) ) {
					addEdge(o1, o2);
				}
			}
		}
	}

	@Override
	protected _SpatialEdge getEdge(final Object e) {
		return (_SpatialEdge) edgeMap.get(e);
	}

	@Override
	protected _SpatialVertex getVertex(final Object v) {
		return (_SpatialVertex) vertexMap.get(v);
	}

	@Override
	protected _SpatialEdge newEdge(final Object e, final Object v1, final Object v2)
		throws GamaRuntimeException {
		return new _SpatialEdge(this, e, v1, v2);
	}

	@Override
	protected _SpatialVertex newVertex(final Object v) throws GamaRuntimeException {
		return new _SpatialVertex(this, v);
	}

	public boolean addVertex(final IShape v) {
		boolean added = super.addVertex(v);
		if ( added && vertexRelation != null ) {
			for ( IShape o : vertexSet() ) {
				if ( vertexRelation.related(scope, v, o) && !vertexRelation.equivalent(v, o) ) {
					addEdge(v, o);
				}
			}
		}
		return added;
	}

	/**
	 * @param gamaPath
	 * @return
	 */
	public double computeWeight(final IPath gamaPath) {
		double result = 0;
		List l = gamaPath.getEdgeList();
		for ( Object o : l ) {
			result += getEdgeWeight(o);
		}
		return result;
	}

	/**
	 * @see msi.gama.common.interfaces.IGraph#isSpatial()
	 */
	@Override
	public boolean isSpatial() {
		return true;
	}

}
