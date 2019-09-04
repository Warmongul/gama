/*******************************************************************************************************
 *
 * gaml.expressions.IExpressionCompiler.java, in plugin gama.core, is part of the source code of the GAMA
 * modeling and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.expressions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import gama.common.interfaces.IDisposable;
import gama.runtime.scope.IExecutionContext;
import gama.util.map.GamaMapFactory;
import gama.util.map.IMap;
import gaml.descriptions.ActionDescription;
import gaml.descriptions.IDescription;
import gaml.descriptions.IExpressionDescription;
import gaml.descriptions.OperatorProto;
import gaml.statements.Arguments;
import gaml.types.Signature;

/**
 * Written by drogoul Modified on 28 d�c. 2010
 *
 * @todo Description
 *
 */
public interface IExpressionCompiler<T> extends IDisposable {

	IMap<String, IMap<Signature, OperatorProto>> OPERATORS = GamaMapFactory.createUnordered(500);
	Set<String> ITERATORS = new HashSet<>();

	IExpression compile(final IExpressionDescription s, final IDescription parsingContext);

	IExpression compile(final String expression, final IDescription parsingContext, IExecutionContext tempContext);

	Arguments parseArguments(ActionDescription action, EObject eObject, IDescription context, boolean compileArgValues);

	/**
	 * @param context
	 * @param facet
	 * @return
	 */

	List<IDescription> compileBlock(final String string, final IDescription actionContext,
			IExecutionContext tempContext);

}