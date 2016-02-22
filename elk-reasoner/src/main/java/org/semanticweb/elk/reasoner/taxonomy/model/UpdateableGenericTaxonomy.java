package org.semanticweb.elk.reasoner.taxonomy.model;

/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2016 Department of Computer Science, University of Oxford
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

import java.util.Collection;

import org.semanticweb.elk.owl.interfaces.ElkEntity;

/**
 * Generic taxonomy that can be modified.
 * 
 * @author Peter Skocovsky
 *
 * @param <T>
 *            The type of members of the nodes of this taxonomy.
 * @param <N>
 *            The type of nodes in this taxonomy.
 */
public interface UpdateableGenericTaxonomy<T extends ElkEntity, N extends UpdateableGenericTaxonomyNode<T, N>>
		extends GenericTaxonomy<T, N> {

	N getCreateNode(Collection<? extends T> members);

	boolean setCreateDirectSupernodes(N subNode,
			Iterable<? extends Collection<? extends T>> superMemberSets);

	/**
	 * Removes the node containing the specified member from the taxonomy.
	 * 
	 * @param member
	 *            The member whose node should be removed.
	 * @return <code>true</code> if and only if some node was removed.
	 */
	boolean removeNode(T member);

	/**
	 * Adds the specified member to the bottom node of this taxonomy.
	 * 
	 * @param member
	 *            The member to add.
	 * @return Whether the bottom node changed.
	 */
	boolean addToBottomNode(T member);

	/**
	 * Removes the specified member from the bottom node of this taxonomy.
	 * 
	 * @param member
	 *            The node to remove.
	 * @return Whether the bottom node changed.
	 */
	boolean removeFromBottomNode(T member);

}