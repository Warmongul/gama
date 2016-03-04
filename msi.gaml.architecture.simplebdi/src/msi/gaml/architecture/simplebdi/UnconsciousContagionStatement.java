package msi.gaml.architecture.simplebdi;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.ISymbolKind;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gaml.descriptions.IDescription;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.statements.AbstractStatement;
import msi.gaml.types.IType;

@symbol(name = UnconsciousContagionStatement.UNCONSCIOUSCONTAGION, kind = ISymbolKind.SINGLE_STATEMENT, with_sequence = false,
concept = { IConcept.BDI })
@inside(kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT })
@facets(value = {
	@facet(name = IKeyword.NAME, type = IType.ID, optional = true, doc = @doc("the identifier of the unconscious contagion")),	
	@facet(name = UnconsciousContagionStatement.EMOTION, type = EmotionType.id,optional = false, doc = @doc("the emotion that will be copied with the contagion")),
	@facet(name = UnconsciousContagionStatement.CHARISMA, type = IType.FLOAT,optional = true, doc = @doc("The charisma value of the perceived agent (between 0 and 1)")),
	@facet(name = IKeyword.WHEN, type = IType.BOOL, optional = true, doc = @doc("A boolean value to get the emotion only with a certain condition")),
	@facet(name = UnconsciousContagionStatement.THRESHOLD, type = IType.FLOAT, optional = true, doc = @doc("The threshold value to make the contagion")),
	@facet(name = UnconsciousContagionStatement.RECEPTIVITY, type = IType.FLOAT, optional = true, doc = @doc("The receptivity value of the current agent (between 0 and 1)"))}
,omissible = IKeyword.NAME)
@doc( value = "enables to directly add a belief from the variable of a perceived specie.",
		examples={@example("focus var:speed /*where speed is a variable from a species that is being perceived*/ agent: myself")})

public class UnconsciousContagionStatement extends AbstractStatement {

	public static final String UNCONSCIOUSCONTAGION = "unconscious_contagion";
	public static final String EMOTION = "emotion";
	public static final String CHARISMA = "charisma";
	public static final String RECEPTIVITY = "receptivity";
	public static final String THRESHOLD = "threshold";
	
	final IExpression name;
	final IExpression emotion;
	final IExpression charisma;
	final IExpression when;
	final IExpression receptivity;
	final IExpression threshold;
	
	public UnconsciousContagionStatement(IDescription desc) {
		super(desc);
		name = getFacet(IKeyword.NAME);
		emotion = getFacet(UnconsciousContagionStatement.EMOTION);
		charisma = getFacet(UnconsciousContagionStatement.CHARISMA);
		when = getFacet(IKeyword.WHEN);
		receptivity = getFacet(UnconsciousContagionStatement.RECEPTIVITY);
		threshold = getFacet(UnconsciousContagionStatement.THRESHOLD);
	}

	@Override
	protected Object privateExecuteIn(IScope scope) throws GamaRuntimeException {
		
			final IAgent mySelfAgent = scope.getAgentsStack()[1];
			Double charismaValue = 1.0;
			Double receptivityValue = 1.0;
			Double thresholdValue = 0.25;
			IScope scopeMySelf = null;
			if(mySelfAgent!=null){
				scopeMySelf = mySelfAgent.getScope().copy();
				scopeMySelf.push(mySelfAgent);
			}
		if ( when == null || Cast.asBool(scopeMySelf, when.value(scopeMySelf)) ){
			if(emotion!=null){
				if(SimpleBdiArchitecture.hasEmotion(scope, (Emotion)emotion.value(scope))){
					if(charisma!=null){
						charismaValue = (Double) charisma.value(scope);
					}
					if(receptivity!=null){
						receptivityValue = (Double) receptivity.value(scopeMySelf);
					}
					if(threshold!=null){
						thresholdValue = (Double) threshold.value(scopeMySelf);
					}
					if(charismaValue*receptivityValue >= thresholdValue){
						Emotion tempEmo = SimpleBdiArchitecture.getEmotion(scope, (Emotion)emotion.value(scope));
						Emotion temp;
						if(!tempEmo.getNoIntensity()){
							temp=new Emotion(tempEmo.getName(),tempEmo.getIntensity()*charismaValue*receptivityValue,tempEmo.getAbout(),tempEmo.getDecay());
						}
						else{
							temp=tempEmo;
						}
						SimpleBdiArchitecture.addEmotion(scopeMySelf,temp);
					}
				}
			}
		}
		return null;
	}

}