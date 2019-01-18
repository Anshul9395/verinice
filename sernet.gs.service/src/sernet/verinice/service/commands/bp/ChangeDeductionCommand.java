/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 * Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.bp;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.common.CnATreeElement;

public class ChangeDeductionCommand extends GenericCommand {

    private static final long serialVersionUID = -7794480684224165301L;

    private static final Logger LOG = Logger.getLogger(ChangeDeductionCommand.class);

    private transient ModelingMetaDao metaDao;

    private transient Set<String> requirementUUIDs;

    private boolean deduceImplementation = false;

    public ChangeDeductionCommand(Set<String> requirementUUIDs, boolean deduceImplementation) {
        super();
        this.requirementUUIDs = requirementUUIDs;
        this.deduceImplementation = deduceImplementation;
    }

    @Override
    public void execute() {
        try {
            for (CnATreeElement requirement : loadRequirements()) {
                handleRequirement((BpRequirement) requirement);
            }
        } catch (Exception e) {
            LOG.error("Error while disabling deduction of implementation", e);
            throw new RuntimeCommandException("Error while creating links", e);
        }
    }

    private void handleRequirement(BpRequirement requirement) {
        requirement.setDeductionOfImplementation(deduceImplementation);
        getDao().merge(requirement);
    }

    private Collection<CnATreeElement> loadRequirements() {
        return getMetaDao().loadElementsWithProperties(requirementUUIDs);
    }

    public ModelingMetaDao getMetaDao() {
        if (metaDao == null) {
            metaDao = new ModelingMetaDao(getDao());
        }
        return metaDao;
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }

}
