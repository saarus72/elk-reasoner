/**
 * 
 */
package org.semanticweb.elk.reasoner.saturation.inferences.visitors;

import org.semanticweb.elk.reasoner.saturation.conclusions.implementation.ConclusionBaseFactory;

/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2014 Department of Computer Science, University of Oxford
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.ClassConclusion;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.Conclusion;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.ObjectPropertyConclusion;
import org.semanticweb.elk.reasoner.saturation.conclusions.visitors.DummyClassConclusionVisitor;
import org.semanticweb.elk.reasoner.saturation.conclusions.visitors.DummyObjectPropertyConclusionVisitor;
import org.semanticweb.elk.reasoner.saturation.inferences.ClassInference;
import org.semanticweb.elk.reasoner.saturation.inferences.ComposedBackwardLink;
import org.semanticweb.elk.reasoner.saturation.inferences.ComposedConjunction;
import org.semanticweb.elk.reasoner.saturation.inferences.ComposedDecomposition;
import org.semanticweb.elk.reasoner.saturation.inferences.ComposedDefinition;
import org.semanticweb.elk.reasoner.saturation.inferences.ComposedDisjunction;
import org.semanticweb.elk.reasoner.saturation.inferences.ComposedExistential;
import org.semanticweb.elk.reasoner.saturation.inferences.ComposedForwardLink;
import org.semanticweb.elk.reasoner.saturation.inferences.ContradictionFromDisjointSubsumers;
import org.semanticweb.elk.reasoner.saturation.inferences.ContradictionFromNegation;
import org.semanticweb.elk.reasoner.saturation.inferences.ContradictionFromOwlNothing;
import org.semanticweb.elk.reasoner.saturation.inferences.DecomposedDefinition;
import org.semanticweb.elk.reasoner.saturation.inferences.DecomposedExistentialBackwardLink;
import org.semanticweb.elk.reasoner.saturation.inferences.DecomposedExistentialForwardLink;
import org.semanticweb.elk.reasoner.saturation.inferences.DecomposedFirstConjunct;
import org.semanticweb.elk.reasoner.saturation.inferences.DecomposedReflexiveBackwardLink;
import org.semanticweb.elk.reasoner.saturation.inferences.DecomposedReflexiveForwardLink;
import org.semanticweb.elk.reasoner.saturation.inferences.DecomposedSecondConjunct;
import org.semanticweb.elk.reasoner.saturation.inferences.DisjointSubsumerFromSubsumer;
import org.semanticweb.elk.reasoner.saturation.inferences.GeneratedPropagation;
import org.semanticweb.elk.reasoner.saturation.inferences.InitializationSubsumer;
import org.semanticweb.elk.reasoner.saturation.inferences.ObjectHasSelfPropertyRangeSubsumer;
import org.semanticweb.elk.reasoner.saturation.inferences.PropagatedContradiction;
import org.semanticweb.elk.reasoner.saturation.inferences.ReversedForwardLink;
import org.semanticweb.elk.reasoner.saturation.inferences.SubClassOfSubsumer;
import org.semanticweb.elk.reasoner.saturation.inferences.SuperReversedForwardLink;
import org.semanticweb.elk.reasoner.saturation.inferences.properties.ObjectPropertyInference;
import org.semanticweb.elk.reasoner.saturation.inferences.properties.ObjectPropertyInferenceVisitor;
import org.semanticweb.elk.reasoner.saturation.inferences.properties.SubPropertyChainExpanded;
import org.semanticweb.elk.reasoner.saturation.inferences.properties.SubPropertyChainInit;

/**
 * Visits all premises for the given {@link ClassInference} or
 * {@link ObjectPropertyInference}. Each premise implements {@link ClassConclusion}
 * or {@link ObjectPropertyConclusion}.
 * 
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 */
public class ClassInferencePremiseVisitor<I, O> implements
		ClassInferenceVisitor<I, O>, ObjectPropertyInferenceVisitor<I, O> {

	private final ClassConclusion.Visitor<I, O> classPremiseVisitor_;

	private final ObjectPropertyConclusion.Visitor<I, O> propertyPremiseVisitor_;
	
	private final Conclusion.Factory factory_ = new ConclusionBaseFactory();

	public ClassInferencePremiseVisitor(
			ClassConclusion.Visitor<I, O> classPremiseVisitor,
			ObjectPropertyConclusion.Visitor<I, O> propertyPremiseVisitor) {
		classPremiseVisitor_ = classPremiseVisitor;
		propertyPremiseVisitor_ = propertyPremiseVisitor;
	}

	public ClassInferencePremiseVisitor(
			ClassConclusion.Visitor<I, O> classPremiseVisitor) {
		this(classPremiseVisitor,
				new DummyObjectPropertyConclusionVisitor<I, O>());
	}

	public ClassInferencePremiseVisitor(
			ObjectPropertyConclusion.Visitor<I, O> propertyPremiseVisitor) {
		this(new DummyClassConclusionVisitor<I, O>(), propertyPremiseVisitor);
	}

	@Override
	public O visit(InitializationSubsumer conclusion, I input) {
		return null;
	}

	@Override
	public O visit(SubClassOfSubsumer conclusion, I input) {
		conclusion.getPremise(factory_).accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(ComposedConjunction conclusion, I input) {
		conclusion.getFirstPremise(factory_).accept(classPremiseVisitor_, input);
		conclusion.getSecondPremise(factory_).accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(DecomposedFirstConjunct conclusion, I input) {
		conclusion.getPremise(factory_).accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(DecomposedSecondConjunct conclusion, I input) {
		conclusion.getPremise(factory_).accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(ComposedExistential conclusion, I input) {
		conclusion.getFirstPremise(factory_).accept(classPremiseVisitor_, input);
		conclusion.getSecondPremise(factory_).accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(ComposedBackwardLink conclusion, I input) {
		conclusion.getFirstPremise(factory_).accept(classPremiseVisitor_, input);
		conclusion.getSecondPremise(factory_).accept(propertyPremiseVisitor_, input);
		conclusion.getThirdPremise(factory_).accept(classPremiseVisitor_, input);
		conclusion.getFourthPremise(factory_).accept(propertyPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(ComposedForwardLink conclusion, I input) {
		conclusion.getFirstPremise(factory_).accept(classPremiseVisitor_, input);
		conclusion.getSecondPremise(factory_).accept(propertyPremiseVisitor_, input);
		conclusion.getThirdPremise(factory_).accept(classPremiseVisitor_, input);
		conclusion.getFourthPremise(factory_).accept(propertyPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(ReversedForwardLink conclusion, I input) {
		conclusion.getPremise(factory_).accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(SuperReversedForwardLink conclusion, I input) {
		conclusion.getFirstPremise(factory_).accept(classPremiseVisitor_, input);
		conclusion.getSecondPremise(factory_).accept(propertyPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(DecomposedExistentialBackwardLink conclusion, I input) {
		conclusion.getPremise(factory_).accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(DecomposedExistentialForwardLink conclusion, I input) {
		conclusion.getPremise(factory_).accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(DecomposedReflexiveBackwardLink conclusion, I input) {
		conclusion.getPremise(factory_).accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(DecomposedReflexiveForwardLink conclusion, I input) {
		conclusion.getPremise(factory_).accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(GeneratedPropagation conclusion, I input) {
		conclusion.getFirstPremise(factory_).accept(classPremiseVisitor_, input);
		conclusion.getSecondPremise(factory_).accept(propertyPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(ContradictionFromDisjointSubsumers conclusion, I input) {
		conclusion.getFirstPremise(factory_).accept(classPremiseVisitor_, input);
		conclusion.getSecondPremise(factory_).accept(classPremiseVisitor_, input);		

		return null;
	}

	@Override
	public O visit(ContradictionFromNegation conclusion, I input) {
		conclusion.getPremise(factory_).accept(classPremiseVisitor_, input);
		conclusion.getNegatedPremise(factory_).accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(ContradictionFromOwlNothing conclusion, I input) {
		conclusion.getPremise(factory_).accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(PropagatedContradiction conclusion, I input) {
		conclusion.getLinkPremise(factory_).accept(classPremiseVisitor_, input);
		conclusion.getContradictionPremise(factory_)
				.accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(DisjointSubsumerFromSubsumer conclusion, I input) {
		conclusion.getPremise(factory_).accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(ComposedDisjunction conclusion, I input) {
		conclusion.getPremise(factory_).accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(ComposedDecomposition inference, I input) {
		inference.getPremise(factory_).accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(ComposedDefinition inference, I input) {
		inference.getPremise(factory_).accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(DecomposedDefinition inference, I input) {
		inference.getPremise(factory_).accept(classPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(SubPropertyChainInit conclusion, I input) {
		// no premises
		return null;
	}

	@Override
	public O visit(SubPropertyChainExpanded inference, I input) {
		inference.getPremise(factory_).accept(propertyPremiseVisitor_, input);
		return null;
	}

	@Override
	public O visit(ObjectHasSelfPropertyRangeSubsumer inference, I input) {
		inference.getPremise(factory_).accept(classPremiseVisitor_, input);
		// TODO: process the property range premise
		return null;
	}

}
