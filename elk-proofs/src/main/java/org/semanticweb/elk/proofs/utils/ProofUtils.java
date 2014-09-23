/**
 * 
 */
package org.semanticweb.elk.proofs.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.owl.interfaces.ElkObjectInverseOf;
import org.semanticweb.elk.owl.interfaces.ElkObjectProperty;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyExpression;
import org.semanticweb.elk.owl.visitors.ElkObjectPropertyExpressionVisitor;
import org.semanticweb.elk.proofs.expressions.Explanation;
import org.semanticweb.elk.proofs.expressions.Expression;
import org.semanticweb.elk.proofs.expressions.MultiAxiomExpression;
import org.semanticweb.elk.util.collections.Operations;
import org.semanticweb.elk.util.collections.Operations.Transformation;

/**
 * @author Pavel Klinov
 *
 * pavel.klinov@uni-ulm.de
 */
public class ProofUtils {

	public static ElkObjectProperty asObjectProperty(ElkObjectPropertyExpression expr) {
		return expr.accept(new ElkObjectPropertyExpressionVisitor<ElkObjectProperty>() {

			@Override
			public ElkObjectProperty visit(ElkObjectInverseOf elkObjectInverseOf) {
				throw new IllegalArgumentException("Inverses aren't in EL");
			}

			@Override
			public ElkObjectProperty visit(ElkObjectProperty elkObjectProperty) {
				return elkObjectProperty;
			}
			
		});
	}
	
	public static Expression fromInferenceConclusions(Iterable<Iterable<Explanation>> explanations) {
		return new MultiAxiomExpression(Operations.concat(explanations));
	}
	
	// merging a list of expressions (represented as iterables over their explanations) into one, computing the n-ary cartesian product
	public static Expression fromPremiseExplanations(List<Iterable<Explanation>> explanations) {
		// TODO not optimized
		List<Iterable<Explanation>> cartesian = cartesian(explanations);
		// merging iterables of explanations into explanations
		List<Explanation> merged = new ArrayList<Explanation>(cartesian.size());
		
		for (Iterable<Explanation> toMerge : cartesian) {
			// first transforming explanations into iterables over axioms
			Iterable<Iterable<ElkAxiom>> axioms = Operations.map(toMerge, new Transformation<Explanation, Iterable<ElkAxiom>>() {

				@Override
				public Iterable<ElkAxiom> transform(Explanation element) {
					return element.getAxioms();
				}
				
			});
			// second concat'ing the axioms
			merged.add(new Explanation(Operations.concat(axioms)));
		}
		
		return new MultiAxiomExpression(merged);
	}
	
	protected static List<Iterable<Explanation>> cartesian(List<Iterable<Explanation>> lists) {
		List<Iterable<Explanation>> resultLists = new ArrayList<Iterable<Explanation>>();

		if (lists.size() == 0) {
			resultLists.add(new ArrayList<Explanation>());
			return resultLists;
		}

		Iterable<Explanation> firstList = lists.get(0);
		List<Iterable<Explanation>> remainingLists = cartesian(lists.subList(1, lists.size()));
		
		for (Explanation explanation : firstList) {
			for (Iterable<Explanation> remainingList : remainingLists) {
				resultLists.add(Operations.concat(Collections.singletonList(explanation), remainingList));
			}
		}

		return resultLists;
	}
}