/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.commandline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.reporter.ProvenanceReporter;
import net.sf.taverna.t2.reference.ReferenceService;
/**
 * An InvocationContext used by the command line tool.
 * 
 * @author Stuart Owen
 *
 */
public class CommandLineInvocationContext implements InvocationContext {

	private final ReferenceService referenceService;

	private final ProvenanceReporter provenanceReporter;

	private List<Object> entities = Collections
			.synchronizedList(new ArrayList<Object>());

	public CommandLineInvocationContext(ReferenceService referenceService,
			ProvenanceReporter provenanceReporter) {
		this.referenceService = referenceService;
		this.provenanceReporter = provenanceReporter;
	}

	public void addEntity(Object entity) {
		entities.add(entity);
	}

	public <T extends Object> List<T> getEntities(Class<T> entityType) {
		List<T> entitiesOfType = new ArrayList<T>();
		synchronized (entities) {
			for (Object entity : entities) {
				if (entityType.isInstance(entity)) {
					entitiesOfType.add(entityType.cast(entity));
				}
			}
		}
		return entitiesOfType;
	}

	public ProvenanceReporter getProvenanceReporter() {
		return provenanceReporter;
	}

	public ReferenceService getReferenceService() {
		return referenceService;
	}

	public void removeEntity(Object entity) {
		entities.remove(entity);
	}
}