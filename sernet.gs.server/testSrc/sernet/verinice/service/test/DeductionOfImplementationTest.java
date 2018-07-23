/*******************************************************************************
 * Copyright (c) 2017 Urs Zeidler.
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
 *     Urs Zeidler uz[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static sernet.verinice.model.bp.DeductionImplementationUtil.IMPLEMENTATION_DEDUCE;
import static sernet.verinice.model.bp.DeductionImplementationUtil.IMPLEMENTATION_STATUS;
import static sernet.verinice.model.bp.DeductionImplementationUtil.IMPLEMENTATION_STATUS_CODE_NO;
import static sernet.verinice.model.bp.DeductionImplementationUtil.IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE;
import static sernet.verinice.model.bp.DeductionImplementationUtil.IMPLEMENTATION_STATUS_CODE_PARTIALLY;
import static sernet.verinice.model.bp.DeductionImplementationUtil.IMPLEMENTATION_STATUS_CODE_YES;
import static sernet.verinice.model.bp.DeductionImplementationUtil.getImplementationStatus;
import static sernet.verinice.model.bp.DeductionImplementationUtil.getImplementationStatusId;
import static sernet.verinice.model.bp.DeductionImplementationUtil.setImplementationStausToRequirement;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.DeductionImplementationUtil;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CreateLink;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.RemoveLink;
import sernet.verinice.service.commands.UpdateElement;

/**
 * Test the deduction of the implementation for a requirement. A
 * {@link BpRequirement} can deduce the implementation status from a linked
 * {@link Safeguard} when the property 'xxx_implementation_deduce' is set.
 *
 * @author uz[at]sernet.de
 *
 */
@TransactionConfiguration(transactionManager = "txManager", defaultRollback = false)
@Transactional
public class DeductionOfImplementationTest extends AbstractModernizedBaseProtection {
    private static final Logger LOG = Logger.getLogger(DeductionOfImplementationTest.class);

    /**
     * Generic dataholder.
     *
     * @author uz[at]sernet.de
     *
     * @param <A>
     * @param <B>
     */
    private class Duo<A, B> {
        A a;
        B b;

        public Duo(A a, B b) {
            super();
            this.a = a;
            this.b = b;
        }
    }

    /**
     * Test the util method.
     *
     * @throws CommandException
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testSetImplementationStausToRequirement() throws CommandException {
        Duo<Safeguard, BpRequirement> duo = createTestElements();
        Safeguard safeguard = duo.a;
        BpRequirement requirement = duo.b;

        updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);
        prepareRequirement(requirement);

        assertTrue(setImplementationStausToRequirement(safeguard, requirement));
        assertFalse(setImplementationStausToRequirement(safeguard, requirement));

        updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        assertEquals(requirement.getTypeId() + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));

        assertTrue(setImplementationStausToRequirement(safeguard, requirement));
        assertEquals(requirement.getTypeId() + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE,
                getImplementationStatus(requirement));

        updateSafeguard(safeguard, null);
        assertEquals(requirement.getTypeId() + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE,
                getImplementationStatus(requirement));

        assertTrue(setImplementationStausToRequirement(safeguard, requirement));
        assertEquals(null, getImplementationStatus(requirement));

    }

    /**
     * Change the implementation_status after the link is created.
     *
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testDefaultDeductionAfterLink() throws CommandException {
        Duo<Safeguard, BpRequirement> duo = createTestElements();
        Safeguard safeguard = duo.a;
        BpRequirement requirement = duo.b;

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD, null);
        createLink = commandService.executeCommand(createLink);
        assertDeduction(safeguard, requirement);
    }

    /**
     * Change the implementation_status after the link is created. Opposite link
     * direction.
     *
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testDefaultDeductionAfterLinkOppositeDirection() throws CommandException {
        Duo<Safeguard, BpRequirement> duo = createTestElements();
        Safeguard safeguard = duo.a;
        BpRequirement requirement = duo.b;

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD, null);
        createLink = commandService.executeCommand(createLink);
        assertDeduction(safeguard, requirement);
    }

    /**
     * Change the implementation_status before the link is created.
     *
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testDefaultDeductionBeforeLink() throws CommandException {
        Duo<Safeguard, BpRequirement> duo = createTestElements();
        Safeguard safeguard = duo.a;
        BpRequirement requirement = duo.b;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_NO);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD, null);
        createLink = commandService.executeCommand(createLink);

        assertEquals("Must be option 'no'.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_YES);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_YES);
        assertEquals("Must be option 'yes'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES,
                getImplementationStatus(requirement));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        assertEquals("Must be option 'not applicable'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE,
                getImplementationStatus(requirement));
    }

    /**
     * Change the implementation_status before the link is created. Opposite
     * link direction.
     *
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testDefaultDeductionBeforeLinkOppositeDirection() throws CommandException {
        Duo<Safeguard, BpRequirement> duo = createTestElements();
        Safeguard safeguard = duo.a;
        BpRequirement requirement = duo.b;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_NO);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD, null);
        createLink = commandService.executeCommand(createLink);

        assertEquals("Must be option 'no'.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_YES);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_YES);
        assertEquals("Must be option 'yes'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES,
                getImplementationStatus(requirement));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        assertEquals("Must be option 'not applicable'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE,
                getImplementationStatus(requirement));
    }

    /**
     * Switch the deduction off.
     *
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testDefaultDeductionSwitchedOff() throws CommandException {
        Duo<Safeguard, BpRequirement> duo = createTestElements();
        Safeguard safeguard = duo.a;
        BpRequirement requirement = duo.b;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_NO);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD, null);
        createLink = commandService.executeCommand(createLink);
        assertDisabledDeduction(safeguard, requirement);
    }

    /**
     * Switch the deduction off. Opposite link direction.
     *
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testDefaultDeductionSwitchedOffOppositeDirection() throws CommandException {
        Duo<Safeguard, BpRequirement> duo = createTestElements();
        Safeguard safeguard = duo.a;
        BpRequirement requirement = duo.b;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_NO);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD, null);
        createLink = commandService.executeCommand(createLink);
        assertDisabledDeduction(safeguard, requirement);
    }

    @Transactional
    @Rollback(true)
    @Test
    public void testDeductionWorksWhenRemovingLink() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();

        BpRequirementGroup requirementGroup = createRequirementGroup(itNetwork);
        CnATreeElement requirement = createBpRequirement(requirementGroup);
        requirement = prepareRequirement((BpRequirement) requirement);

        SafeguardGroup safeguardGroup = createSafeguardGroup(itNetwork);
        Safeguard safeguard1 = createSafeguard(safeguardGroup);
        safeguard1 = updateSafeguard(safeguard1, IMPLEMENTATION_STATUS_CODE_NO);
        Safeguard safeguard2 = createSafeguard(safeguardGroup);
        safeguard2 = updateSafeguard(safeguard2, IMPLEMENTATION_STATUS_CODE_YES);

        CnALink link1 = createLink(requirement, safeguard1,
                BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        createLink(requirement, safeguard2, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        assertEquals("Must be option 'partially'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_PARTIALLY,
                getImplementationStatus(requirement));
        elementDao.flush();
        elementDao.clear();
        RemoveLink removeLink = new RemoveLink(link1);
        removeLink = commandService.executeCommand(removeLink);

        requirement = reloadElement(requirement);
        assertEquals("Must be option 'yes'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES,
                getImplementationStatus(requirement));

    }

    @Transactional
    @Rollback(true)
    @Test
    public void testDeductionWorksWhenRemovingSafeguard() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();

        BpRequirementGroup requirementGroup = createRequirementGroup(itNetwork);
        CnATreeElement requirement = createBpRequirement(requirementGroup);
        requirement = prepareRequirement((BpRequirement) requirement);

        SafeguardGroup safeguardGroup = createSafeguardGroup(itNetwork);
        Safeguard safeguard1 = createSafeguard(safeguardGroup);
        safeguard1 = updateSafeguard(safeguard1, IMPLEMENTATION_STATUS_CODE_NO);
        Safeguard safeguard2 = createSafeguard(safeguardGroup);
        safeguard2 = updateSafeguard(safeguard2, IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);

        createLink(requirement, safeguard1, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        createLink(requirement, safeguard2, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        assertEquals("Must be option 'no'.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));
        elementDao.flush();
        elementDao.clear();

        RemoveElement<Safeguard> removeSafeguard = new RemoveElement<>(safeguard1);

        removeSafeguard = commandService.executeCommand(removeSafeguard);

        requirement = reloadElement(requirement);
        assertEquals("Must be option 'n/a'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE,
                getImplementationStatus(requirement));

    }

    @Transactional
    @Rollback(true)
    @Test
    public void testDeductionWorksWhenRemovingSafeguardGroup() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();

        BpRequirementGroup requirementGroup = createRequirementGroup(itNetwork);
        CnATreeElement requirement = createBpRequirement(requirementGroup);
        requirement = prepareRequirement((BpRequirement) requirement);

        SafeguardGroup safeguardGroup1 = createSafeguardGroup(itNetwork);
        Safeguard safeguard1 = createSafeguard(safeguardGroup1);
        safeguard1 = updateSafeguard(safeguard1, IMPLEMENTATION_STATUS_CODE_NO);
        SafeguardGroup safeguardGroup2 = createSafeguardGroup(itNetwork);
        Safeguard safeguard2 = createSafeguard(safeguardGroup2);
        safeguard2 = updateSafeguard(safeguard2, IMPLEMENTATION_STATUS_CODE_YES);

        createLink(requirement, safeguard1, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        createLink(requirement, safeguard2, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        assertEquals("Must be option 'partially'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_PARTIALLY,
                getImplementationStatus(requirement));

        RemoveElement<Safeguard> removeSafeguardgroup = new RemoveElement<>(safeguardGroup2);
        elementDao.flush();
        elementDao.clear();
        removeSafeguardgroup = commandService.executeCommand(removeSafeguardgroup);

        requirement = reloadElement(requirement);
        assertEquals("Must be option 'no'.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));

    }

    /**
     * Two requirements linked to one safeguard. Opposite link direction.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testOneSafeGuardTwoRequirements() throws Exception {
        ItNetwork itNetwork = createNewBPOrganization();

        BpRequirementGroup requirementGroup = createRequirementGroup(itNetwork);
        BpRequirement requirement1 = createBpRequirement(requirementGroup);
        BpRequirement requirement2 = createBpRequirement(requirementGroup);
        SafeguardGroup safeguardGroup = createSafeguardGroup(itNetwork);
        Safeguard safeguard = createSafeguard(safeguardGroup);
        requirement1 = prepareRequirement(requirement1);
        requirement2 = prepareRequirement(requirement2);

        createLink(requirement1, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        createLink(requirement2, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);

        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);
        assertDeduction(safeguard, requirement1);
        assertDeduction(safeguard, requirement2);
    }

    /**
     * Two requirements linked to one safeguard .
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testOneSafeGuardTwoRequirementsOppositeDirection() throws Exception {
        ItNetwork itNetwork = createNewBPOrganization();

        BpRequirementGroup requirementGroup = createRequirementGroup(itNetwork);
        BpRequirement requirement1 = createBpRequirement(requirementGroup);
        BpRequirement requirement2 = createBpRequirement(requirementGroup);
        SafeguardGroup safeguardGroup = createSafeguardGroup(itNetwork);
        Safeguard safeguard = createSafeguard(safeguardGroup);
        requirement1 = prepareRequirement(requirement1);
        requirement2 = prepareRequirement(requirement2);

        createLink(requirement1, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        createLink(requirement2, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);

        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);
        assertDeduction(safeguard, requirement1);
        assertDeduction(safeguard, requirement2);
    }

    /**
     * Test one requirement and n safeguards, safeguard with all the same value.
     *
     * @throws Exception
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testOneRequirementNSafeguards() throws Exception {
        Duo<BpRequirement, List<Safeguard>> duo = createNSafeguards(5);
        BpRequirement requirement = duo.a;
        List<Safeguard> safeGuards = duo.b;
        assertNull("Must be unset", getImplementationStatus(requirement));

        safeGuards = updateSafeguards(safeGuards, IMPLEMENTATION_STATUS_CODE_NO);
        assertEquals("Must be option 'no'.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));

        safeGuards = updateSafeguards(safeGuards, IMPLEMENTATION_STATUS_CODE_YES);
        assertEquals("Must be option 'yes'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES,
                getImplementationStatus(requirement));

        safeGuards = updateSafeguards(safeGuards, IMPLEMENTATION_STATUS_CODE_PARTIALLY);
        assertEquals("Must be option 'partial'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_PARTIALLY,
                getImplementationStatus(requirement));

        safeGuards = updateSafeguards(safeGuards, IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        assertEquals("Must be option 'not applicable'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE,
                getImplementationStatus(requirement));
    }

    /**
     * Test one requirement and n safeguards, some safeguard with yes all others
     * with na.
     *
     * @throws Exception
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testOneRequirementNSafeguards_Yes() throws Exception {
        Duo<BpRequirement, List<Safeguard>> duo = createNSafeguards(5);
        BpRequirement requirement = duo.a;
        List<Safeguard> safeGuards = duo.b;
        assertNull("Must be unset", getImplementationStatus(requirement));

        safeGuards = updateSafeguards(safeGuards, IMPLEMENTATION_STATUS_CODE_YES);
        assertEquals("Must be option 'yes'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES,
                getImplementationStatus(requirement));

        Safeguard safeGuard = updateSafeguard(safeGuards.get(3),
                IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        safeGuards.set(3, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(4), IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        safeGuards.set(4, safeGuard);
        assertEquals("Must be option 'yes'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES,
                getImplementationStatus(requirement));

        safeGuards = updateSafeguards(safeGuards, IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        assertEquals("Must be option 'na'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE,
                getImplementationStatus(requirement));

        safeGuard = updateSafeguard(safeGuards.get(0), IMPLEMENTATION_STATUS_CODE_YES);
        safeGuards.set(0, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(1), IMPLEMENTATION_STATUS_CODE_YES);
        safeGuards.set(1, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(2), IMPLEMENTATION_STATUS_CODE_YES);
        safeGuards.set(2, safeGuard);
        assertEquals("Must be option 'yes'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES,
                getImplementationStatus(requirement));
    }

    /**
     * Test one requirement and n safeguards, some safeguard with no all others
     * with na.
     *
     * @throws Exception
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testOneRequirementNSafeguards_No() throws Exception {
        Duo<BpRequirement, List<Safeguard>> duo = createNSafeguards(5);
        BpRequirement requirement = duo.a;
        List<Safeguard> safeGuards = duo.b;
        assertNull("Must be unset", getImplementationStatus(requirement));

        safeGuards = updateSafeguards(safeGuards, IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        assertEquals("Must be option 'na'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE,
                getImplementationStatus(requirement));

        Safeguard safeGuard = updateSafeguard(safeGuards.get(0), IMPLEMENTATION_STATUS_CODE_NO);
        safeGuards.set(0, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(1), IMPLEMENTATION_STATUS_CODE_NO);
        safeGuards.set(1, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(2), IMPLEMENTATION_STATUS_CODE_NO);
        safeGuards.set(2, safeGuard);
        assertEquals("Must be option 'no'.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));

        safeGuard = updateSafeguard(safeGuards.get(1), IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        safeGuards.set(1, safeGuard);
        assertEquals("Must be option 'no'.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));

        safeGuard = updateSafeguard(safeGuards.get(0), IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        safeGuards.set(0, safeGuard);
        assertEquals("Must be option 'no'.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));

        safeGuard = updateSafeguard(safeGuards.get(1), IMPLEMENTATION_STATUS_CODE_NO);
        safeGuards.set(1, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(0), IMPLEMENTATION_STATUS_CODE_YES);
        safeGuards.set(0, safeGuard);
        assertEquals("Must be option 'no'.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));
    }

    /**
     * Test one requirement and n safeguards, some safeguard with no all others
     * with na.
     *
     * @throws Exception
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testOneRequirementNSafeguards_No_with_no_not_applicable() throws Exception {
        Duo<BpRequirement, List<Safeguard>> duo = createNSafeguards(5);
        BpRequirement requirement = duo.a;
        List<Safeguard> safeGuards = duo.b;
        assertNull("Must be unset", getImplementationStatus(requirement));

        safeGuards = updateSafeguards(safeGuards, IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        assertEquals("Must be option 'na'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE,
                getImplementationStatus(requirement));

        Safeguard safeGuard = updateSafeguard(safeGuards.get(0), IMPLEMENTATION_STATUS_CODE_YES);
        safeGuards.set(0, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(1), IMPLEMENTATION_STATUS_CODE_YES);
        safeGuards.set(1, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(2), IMPLEMENTATION_STATUS_CODE_YES);
        safeGuards.set(2, safeGuard);
        assertEquals("Must be option 'no'.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES,
                getImplementationStatus(requirement));

        safeGuard = updateSafeguard(safeGuards.get(0), IMPLEMENTATION_STATUS_CODE_NO);
        safeGuards.set(0, safeGuard);
        assertEquals("Must be option 'partial'.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_PARTIALLY,
                getImplementationStatus(requirement));
        
        safeGuard = updateSafeguard(safeGuards.get(1), IMPLEMENTATION_STATUS_CODE_NO);
        safeGuards.set(1, safeGuard);
        assertEquals("Must be option 'no'.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));
    }

    
    
    /**
     * Test one requirement and n safeguards, some safeguard with no all others
     * with na.
     *
     * @throws Exception
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testOneRequirementNSafeguards_NoHalf() throws Exception {
        Duo<BpRequirement, List<Safeguard>> duo = createNSafeguards(10);
        BpRequirement requirement = duo.a;
        List<Safeguard> safeGuards = duo.b;
        assertNull("Must be unset", getImplementationStatus(requirement));

        safeGuards = updateSafeguards(safeGuards, IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        assertEquals("Must be option 'na'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE,
                getImplementationStatus(requirement));

        Safeguard safeGuard = updateSafeguard(safeGuards.get(0), IMPLEMENTATION_STATUS_CODE_NO);
        safeGuards.set(0, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(1), IMPLEMENTATION_STATUS_CODE_NO);
        safeGuards.set(1, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(2), IMPLEMENTATION_STATUS_CODE_NO);
        safeGuards.set(2, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(3), IMPLEMENTATION_STATUS_CODE_PARTIALLY);
        safeGuards.set(3, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(4), IMPLEMENTATION_STATUS_CODE_PARTIALLY);
        safeGuards.set(4, safeGuard);
        assertEquals("Must be option 'no'.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));// 3/5->no

        safeGuard = updateSafeguard(safeGuards.get(2), IMPLEMENTATION_STATUS_CODE_YES);
        safeGuards.set(2, safeGuard);
        assertEquals("Must be option 'partially'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_PARTIALLY,
                getImplementationStatus(requirement));// 2/5->pa

        safeGuard = updateSafeguard(safeGuards.get(5), IMPLEMENTATION_STATUS_CODE_NO);
        safeGuards.set(5, safeGuard);
        assertEquals("Must be option 'no'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_PARTIALLY,
                getImplementationStatus(requirement));// 3/6->pa

        safeGuard = updateSafeguard(safeGuards.get(6), IMPLEMENTATION_STATUS_CODE_NO);
        safeGuards.set(6, safeGuard);
        assertEquals("Must be option 'no'.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));// 4/7->no

        safeGuard = updateSafeguard(safeGuards.get(6), IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        safeGuards.set(6, safeGuard);
        assertEquals("Must be option 'partially'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_PARTIALLY,
                getImplementationStatus(requirement));// 3/6->pa

        safeGuard = updateSafeguard(safeGuards.get(2), IMPLEMENTATION_STATUS_CODE_NO);
        safeGuards.set(2, safeGuard);
        assertEquals("Must be option 'no'.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));// 4/6->no
    }

    private List<Safeguard> updateSafeguards(List<Safeguard> safeGuards, String option)
            throws CommandException {
        List<Safeguard> list = new ArrayList<Safeguard>(safeGuards.size());
        for (Safeguard safeguard : safeGuards) {
            Safeguard updateSafeguard = updateSafeguard(safeguard, option);
            list.add(updateSafeguard);
        }
        return list;
    }

    private Duo<BpRequirement, List<Safeguard>> createNSafeguards(int safeGuardNumber)
            throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();

        BpRequirementGroup requirementGroup = createRequirementGroup(itNetwork);
        BpRequirement requirement = createBpRequirement(requirementGroup);
        SafeguardGroup safeguardGroup = createSafeguardGroup(itNetwork);
        requirement = prepareRequirement(requirement);

        List<Safeguard> safeGuards = new ArrayList<>(safeGuardNumber);
        for (int i = 0; i < safeGuardNumber; i++) {
            Safeguard safeguard = createSafeguard(safeguardGroup);
            createLink(requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
            safeguard = updateSafeguard(safeguard, null);
            safeGuards.add(safeguard);
        }
        return new Duo<BpRequirement, List<Safeguard>>(requirement, safeGuards);
    }

    /**
     * Create the test elements. Create a new it network and the necessary
     * groups for the target test objects. Returns the two objects under test.
     *
     */
    private Duo<Safeguard, BpRequirement> createTestElements() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();

        BpRequirementGroup requirementGroup = createRequirementGroup(itNetwork);
        BpRequirement requirement = createBpRequirement(requirementGroup);
        SafeguardGroup safeguardGroup = createSafeguardGroup(itNetwork);
        Safeguard safeguard = createSafeguard(safeguardGroup);
        requirement = prepareRequirement(requirement);

        return new Duo<Safeguard, BpRequirement>(safeguard, requirement);
    }

    /**
     * Assert the deduction of the implementation value.
     *
     */
    private void assertDeduction(Safeguard safeguard, BpRequirement requirement)
            throws CommandException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_NO);
        }
        updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);
        assertEquals("Must be option 'no'.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_YES);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_YES);
        assertEquals("Must be option 'yes'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES,
                getImplementationStatus(requirement));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        assertEquals("Must be option 'not applicable'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE,
                getImplementationStatus(requirement));
    }

    /**
     * Disables the deduction and assert the implementation state don't change.
     *
     */
    private void assertDisabledDeduction(Safeguard safeguard, BpRequirement requirement)
            throws CommandException {

        assertEquals("Must be option 'no'.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_PARTIALLY);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_PARTIALLY);
        assertEquals("Must be option 'partially'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_PARTIALLY,
                getImplementationStatus(requirement));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Switch deduction off for the requirement.");
        }
        requirement.setPropertyValue(requirement.getTypeId() + IMPLEMENTATION_DEDUCE, "0");
        requirement.setSimpleProperty(getImplementationStatusId(requirement),
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES);
        UpdateElement<BpRequirement> command1 = new UpdateElement<>(requirement, true, null);
        commandService.executeCommand(command1);
        requirement = command1.getElement();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_PARTIALLY);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);
        assertEquals("Must be option 'yes'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES,
                getImplementationStatus(requirement));
    }

    /**
     * Will set implementation_status and update the safeguard.
     *
     */
    private Safeguard updateSafeguard(Safeguard safeguard, String option) throws CommandException {
        String value = option != null ? Safeguard.TYPE_ID + option : null;

        safeguard.setSimpleProperty(safeguard.getTypeId() + IMPLEMENTATION_STATUS, value);
        UpdateElement<Safeguard> command = new UpdateElement<>(safeguard, true, null);
        commandService.executeCommand(command);
        safeguard = command.getElement();

        return safeguard;
    }

    /**
     * Prepare a requirement for the test. The field 'implementation_status'
     * will be set to yes and deduction of the implementation is enabled.
     *
     */
    private BpRequirement prepareRequirement(BpRequirement requirement) throws CommandException {
        requirement.setSimpleProperty(requirement.getTypeId() + IMPLEMENTATION_STATUS,
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES);
        UpdateElement<BpRequirement> command1 = new UpdateElement<>(requirement, true, null);
        commandService.executeCommand(command1);
        requirement = command1.getElement();
        assertEquals("Must be option 'yes'.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES,
                getImplementationStatus(requirement));

        assertTrue("Deduction should be enabled.",
                DeductionImplementationUtil.isDeductiveImplementationEnabled(requirement));
        return requirement;
    }

}
