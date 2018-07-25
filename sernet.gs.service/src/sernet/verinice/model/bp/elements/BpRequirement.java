/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.bp.elements;

import static sernet.verinice.model.bp.DeductionImplementationUtil.isDeductiveImplementationEnabled;
import static sernet.verinice.model.bp.DeductionImplementationUtil.setImplementationStausToRequirement;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import sernet.hui.common.connect.IIdentifiableElement;
import sernet.hui.common.connect.ITaggableElement;
import sernet.verinice.interfaces.IReevaluator;
import sernet.verinice.model.bp.DeductionImplementationUtil;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bp.ISecurityLevelProvider;
import sernet.verinice.model.bp.Reevaluator;
import sernet.verinice.model.bp.SecurityLevel;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.AbstractLinkChangeListener;
import sernet.verinice.model.common.CascadingTransaction;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ILinkChangeListener;
import sernet.verinice.model.common.TransactionAbortedException;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class BpRequirement extends CnATreeElement
        implements IBpElement, IIdentifiableElement, ITaggableElement, ISecurityLevelProvider {

    private static final long serialVersionUID = 6621062615495040741L;

    public static final String TYPE_ID = "bp_requirement"; //$NON-NLS-1$

    public static final String PROP_ABBR = "bp_requirement_abbr"; //$NON-NLS-1$
    public static final String PROP_OBJECTBROWSER = "bp_requirement_objectbrowser_content"; //$NON-NLS-1$
    public static final String PROP_NAME = "bp_requirement_name"; //$NON-NLS-1$
    public static final String PROP_ID = "bp_requirement_id"; //$NON-NLS-1$
    public static final String PROP_TAG = "bp_requirement_tag"; //$NON-NLS-1$
    public static final String PROP_LAST_CHANGE = "bp_requirement_last_change"; //$NON-NLS-1$
    public static final String PROP_CONFIDENTIALITY = "bp_requirement_value_method_confidentiality";//$NON-NLS-1$
    public static final String PROP_INTEGRITY = "bp_requirement_value_method_integrity";//$NON-NLS-1$
    public static final String PROP_AVAILABILITY = "bp_requirement_value_method_availability";//$NON-NLS-1$
    public static final String PROP_QUALIFIER = "bp_requirement_qualifier"; //$NON-NLS-1$
    // These keys shall not be used for localization but only to identify which
    // ENUM value shall be used. Use the ENUMs getLabel() instead.
    private static final String PROP_QUALIFIER_BASIC = "bp_requirement_qualifier_basic"; //$NON-NLS-1$
    private static final String PROP_QUALIFIER_STANDARD = "bp_requirement_qualifier_standard"; //$NON-NLS-1$
    private static final String PROP_QUALIFIER_HIGH = "bp_requirement_qualifier_high"; //$NON-NLS-1$

    public static final String PROP_IMPLEMENTATION_DEDUCE = "bp_requirement_implementation_deduce"; //$NON-NLS-1$
    public static final String PROP_IMPLEMENTATION_STATUS = "bp_requirement_implementation_status"; //$NON-NLS-1$
    public static final String PROP_IMPLEMENTATION_STATUS_NO = "bp_requirement_implementation_status_no"; //$NON-NLS-1$
    public static final String PROP_IMPLEMENTATION_STATUS_YES = "bp_requirement_implementation_status_yes"; //$NON-NLS-1$
    public static final String PROP_IMPLEMENTATION_STATUS_PARTIALLY = "bp_requirement_implementation_status_partially"; //$NON-NLS-1$
    public static final String PROP_IMPLEMENTATION_STATUS_NOT_APPLICABLE = "bp_requirement_implementation_status_na"; //$NON-NLS-1$

    public static final String REL_BP_REQUIREMENT_BP_THREAT = "rel_bp_requirement_bp_threat"; //$NON-NLS-1$
    public static final String REL_BP_REQUIREMENT_BP_SAFEGUARD = "rel_bp_requirement_bp_safeguard"; //$NON-NLS-1$
    public static final String REL_BP_REQUIREMENT_BP_ITNETWORK = "rel_bp_requirement_bp_itnetwork"; //$NON-NLS-1$
    public static final String REL_BP_REQUIREMENT_BP_BUSINESSPROCESS = "rel_bp_requirement_bp_businessprocess"; //$NON-NLS-1$
    public static final String REL_BP_REQUIREMENT_BP_APPLICATION = "rel_bp_requirement_bp_application"; //$NON-NLS-1$
    public static final String REL_BP_REQUIREMENT_BP_ITSYSTEM = "rel_bp_requirement_bp_itsystem"; //$NON-NLS-1$
    public static final String REL_BP_REQUIREMENT_BP_ICSSYSTEM = "rel_bp_requirement_bp_icssystem"; //$NON-NLS-1$
    public static final String REL_BP_REQUIREMENT_BP_DEVICE = "rel_bp_requirement_bp_device"; //$NON-NLS-1$
    public static final String REL_BP_REQUIREMENT_BP_NETWORK = "rel_bp_requirement_bp_network"; //$NON-NLS-1$
    public static final String REL_BP_REQUIREMENT_BP_ROOM = "rel_bp_requirement_bp_room"; //$NON-NLS-1$

    private static final String TRUE = "1"; //$NON-NLS-1$
    private static final String FALSE = "0"; //$NON-NLS-1$

    private final IReevaluator protectionRequirementsProvider = new Reevaluator(this);
    private final ILinkChangeListener linkChangeListener = new AbstractLinkChangeListener() {

        private static final long serialVersionUID = -3220319074711927103L;

        @Override
        public void determineValue(CascadingTransaction ta) throws TransactionAbortedException {
            if (!isDeductiveImplementationEnabled(BpRequirement.this)
                    || ta.hasBeenVisited(BpRequirement.this)) {
                return;
            }
            List<CnATreeElement> safeGuards = BpRequirement.this.getLinksDown().stream().filter(
                    DeductionImplementationUtil::isRelevantLinkForImplementationStateDeduction)
                    .map(CnALink::getDependency).collect(Collectors.toList());

            if (!safeGuards.isEmpty()) {
                setImplementationStausToRequirement(safeGuards, BpRequirement.this);
            }
        }
    };

    protected BpRequirement() {
    }

    public BpRequirement(CnATreeElement parent) {
        super(parent);
        init();
    }

    @Override
    public ILinkChangeListener getLinkChangeListener() {
        return linkChangeListener;
    }

    @Override
    public IReevaluator getProtectionRequirementsProvider() {
        return protectionRequirementsProvider;
    }

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public boolean canContain(Object object) {
        return object instanceof BpThreat;
    }

    public String getObjectBrowserDescription() {
        return getEntity().getPropertyValue(PROP_OBJECTBROWSER);
    }

    public void setObjectBrowserDescription(String description) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_OBJECTBROWSER),
                description);
    }

    public String getAbbreviation() {
        return getEntity().getPropertyValue(PROP_ABBR);
    }

    public void setAbbreviation(String abbreviation) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ABBR), abbreviation);
    }

    @Override
    public String getTitle() {
        return getEntity().getPropertyValue(PROP_NAME);
    }

    @Override
    public void setTitel(String name) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
    }

    public void setTitle(String title) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), title);
    }

    @Override
    public String getIdentifier() {
        return getEntity().getPropertyValue(PROP_ID);
    }

    public void setIdentifier(String id) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ID), id);
    }

    /**
     * Stores the appropriate property value id to PROP_QUALIFIER.
     */
    public void setSecurityLevel(SecurityLevel level) {
        String qualifier = null;
        switch (level) {
        case BASIC:
            qualifier = PROP_QUALIFIER_BASIC;
            break;
        case STANDARD:
            qualifier = PROP_QUALIFIER_STANDARD;
            break;
        case HIGH:
            qualifier = PROP_QUALIFIER_HIGH;
            break;
        }
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_QUALIFIER), qualifier);
    }

    /**
     * @return The Security level level represented by property PROP_QUALIFIER
     */
    @Override
    public SecurityLevel getSecurityLevel() {
        // Parsing the string as SecurityLevel should actually be done
        // in Proceeding. But every class has different
        // localization keys. If unique keys, e.g. "QUALIFIER_BASIC"
        // would be used everywhere this code can and should be moved to
        // SecurityLevel.ofLocalizationKey.
        String qualifier = getEntity().getRawPropertyValue(PROP_QUALIFIER);
        if (qualifier == null) {
            return null;
        }
        switch (qualifier) {
        case PROP_QUALIFIER_BASIC:
            return SecurityLevel.BASIC;
        case PROP_QUALIFIER_STANDARD:
            return SecurityLevel.STANDARD;
        case PROP_QUALIFIER_HIGH:
            return SecurityLevel.HIGH;
        case "":
            return null;
        default:
            throw new IllegalStateException("Unknown security level '" + qualifier + "'");
        }
    }

    public void setDeductionOfImplementation(boolean active) {
        String value = (active) ? TRUE : FALSE;
        getEntity().setPropertyValue(PROP_IMPLEMENTATION_DEDUCE, value);
    }

    public boolean isDeductionOfImplementation() {
        return TRUE.equals(getEntity().getPropertyValue(PROP_IMPLEMENTATION_DEDUCE));
    }

    public Date getLastChange() {
        return getEntity().getDate(PROP_LAST_CHANGE);
    }

    public void setLastChange(Date date) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_LAST_CHANGE),
                String.valueOf(date.getTime()));
    }

    public void setIsAffectsConfidentiality(boolean affectsConfidentiality) {
        this.setNumericProperty(PROP_CONFIDENTIALITY, (affectsConfidentiality) ? 1 : 0);
    }

    public boolean IsAffectsConfidentiality() {
        return ((this.getNumericProperty(PROP_CONFIDENTIALITY) == 1) ? true : false);
    }

    public void setIsAffectsIntegrity(boolean affectsIntegrity) {
        this.setNumericProperty(PROP_INTEGRITY, (affectsIntegrity) ? 1 : 0);
    }

    public boolean IsAffectsIntegrity() {
        return ((this.getNumericProperty(PROP_INTEGRITY) == 1) ? true : false);
    }

    public void setIsAffectsAvailability(boolean affectsAvailability) {
        this.setNumericProperty(PROP_AVAILABILITY, (affectsAvailability) ? 1 : 0);
    }

    public boolean IsAffectsAvailability() {
        return ((this.getNumericProperty(PROP_AVAILABILITY) == 1) ? true : false);
    }

    public String getImplementationStatus() {
        return getEntity().getRawPropertyValue(PROP_IMPLEMENTATION_STATUS);
    }

    public static String getIdentifierOfRequirement(CnATreeElement requirement) {
        return requirement.getEntity().getPropertyValue(PROP_ID);
    }

    public static boolean isBpRequirement(CnATreeElement element) {
        if (element == null) {
            return false;
        }
        return TYPE_ID.equals(element.getTypeId());
    }

    @Override
    public String getFullTitle() {
        return joinPrefixAndTitle(getIdentifier(), getTitle());
    }

    @Override
    public Collection<String> getTags() {
        return TagHelper.getTags(getEntity().getPropertyValue(PROP_TAG));
    }

}
