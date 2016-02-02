/**
 * eGov suite of products aim to improve the internal efficiency,transparency,
   accountability and the service delivery of the government  organizations.

    Copyright (C) <2015>  eGovernments Foundation

    The updated version of eGov suite of products as by eGovernments Foundation
    is available at http://www.egovernments.org

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see http://www.gnu.org/licenses/ or
    http://www.gnu.org/licenses/gpl.html .

    In addition to the terms of the GPL license to be adhered to in using this
    program, the following additional terms are to be complied with:

        1) All versions of this program, verbatim or modified must carry this
           Legal Notice.

        2) Any misrepresentation of the origin of the material is prohibited. It
           is required that all modified versions of this material be marked in
           reasonable ways as different from the original version.

        3) This license does not grant any rights to any user of the program
           with regards to rights under trademark law for use of the trade names
           or trademarks of eGovernments Foundation.

  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.adtax.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.egov.commons.CFinancialYear;
import org.egov.infra.persistence.entity.AbstractAuditable;

@Entity
@Table(name = "EGADTAX_BATCHDEMANDGENERATE")
@SequenceGenerator(name = AdvertisementBatchDemandGenerate.SEQ_BATCHDEMANDGENERATE, sequenceName = AdvertisementBatchDemandGenerate.SEQ_BATCHDEMANDGENERATE, allocationSize = 1)
public class AdvertisementBatchDemandGenerate extends AbstractAuditable {
    private static final long serialVersionUID = -2136324833943230304L;
    public static final String SEQ_BATCHDEMANDGENERATE = "SEQ_BATCHDEMANDGENERATE";
    @Id
    @GeneratedValue(generator = SEQ_BATCHDEMANDGENERATE, strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "financialyear", nullable = false)
    private CFinancialYear financialYear;

    @NotNull
    private Boolean active;

    private Integer totalRecords=0;

    @NotNull
    private String jobName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CFinancialYear getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(CFinancialYear financialYear) {
        this.financialYear = financialYear;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

}
