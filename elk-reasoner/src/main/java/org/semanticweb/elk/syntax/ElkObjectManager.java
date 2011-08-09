/*
 * #%L
 * elk-reasoner
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 Oxford University Computing Laboratory
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
package org.semanticweb.elk.syntax;

import org.semanticweb.elk.syntax.interfaces.ElkObject;

/**
 * Interface for classes that implement ElkObject management. The main task of
 * such classes is to ensure that structurally equivalent ElkObjects are
 * represented by only one object in memory.
 * 
 * @author Markus Kroetzsch
 */
public interface ElkObjectManager {

	/**
	 * Get a canonical reference for the given ElkObject.
	 * 
	 * The method always returns an ElkObject that is structurally equivalent to
	 * the one that was given. However, it might return another Java object in
	 * cases where it already knows about an ElkObject that is structurally
	 * equivalent to the given one. This reduces the memory consumption by
	 * ensuring that only one copy of each (immutable) ElkObject is kept for
	 * each structure.
	 * 
	 * @param object
	 *            the object to get a canonical reference for
	 * @return canonical object representing the input data
	 */
	public ElkObject getCanonicalElkObject(ElkObject object);

}
