package au.com.michaelpage.gap.common.generator;

public enum Dimension {

	BROADBEAN_JOBS("select rpm.id || '_' || SUBSTRING_BEFORE(BB_REFERENCE,'/') as job_id, BB_user as job_consultant, BB_Team as job_consultant_team, "
			+ "BB_office as job_consultant_office, '${brand}' as job_brand, '${country_code}' as Job_consultant_country "
			+ "from LIVEJOBS l "
			+ "cross join RPM rpm "
			+ "where not exists (select 1 from LIVEJOBS_PREV lp where lp.BB_REFERENCE = l.BB_REFERENCE "
			+ "and lp.BB_user = l.BB_user and lp.BB_Team = l.BB_Team and lp.BB_office = l.BB_office) "
			+ "and l.BB_office <> 'zombie' and l.BB_Team <> 'zombie'",
			DataOrigin.BROADBEAN),

	NEXTGEN_JOBS("select rpm.id || '_' || job_id as job_id, job_sector_code, job_sector_name, job_subsector_code, job_subsector_name, "
			+ "case job_type when '1' then 'Perm - 1' else 'Non-Perm - ' || job_type end as job_type, "
			+ "job_location_suburb_code, job_location_suburb_name, job_location_city_code, job_location_city_name, job_location_state_code, "
			+ "job_location_state_name, job_location_country_code, "
			+ "job_location_country_name, job_salary_min, job_salary_max, job_title, job_employer_ref, job_employer_name, "
			+ "case job_client_paid when true then 'Yes' else 'No' end as job_client_paid, job_first_published_date, case job_logo when true then 'Yes' else 'No' end as job_logo "
			+ "from nextgen_extract n cross join RPM rpm " 
			+ "where not exists (select 1 from nextgen_extract_PREV np where np.job_id = n.job_id) ",
			DataOrigin.NEXTGEN),

	RPM_JOBS("select job_id, job_consultant, job_brand, job_consultant_country, staffbroadbeanlogin_without_consultant, " 
			+ "SUBSTR(staffbroadbeanlogin_without_consultant, 1, ORDINAL_INDEXOF(staffbroadbeanlogin_without_consultant, '.', 1) - 1) as job_consultant_team, " 
			+ "SUBSTR(staffbroadbeanlogin_without_consultant, ORDINAL_INDEXOF(staffbroadbeanlogin_without_consultant, '.', 1) + 1, ORDINAL_INDEXOF(staffbroadbeanlogin_without_consultant, '.', 2) - " 
			+ "ORDINAL_INDEXOF(staffbroadbeanlogin_without_consultant, '.', 1) - 1) as job_consultant_office " 
			+ "from (select rpm.id || '_' || opportunityref as job_id, staffdiscipline, SUBSTRING_BEFORE(staffbroadbeanlogin, '@') as job_consultant, "  
			+ "SUBSTR(staffbroadbeanlogin, LOCATE('@', staffbroadbeanlogin)+1) as staffbroadbeanlogin_without_consultant, " 
			+ "host.brand as job_brand, host.countryCode as Job_consultant_country " 
			+ "from opportunity " 
			+ "cross join RPM rpm "
			+ "cross join Host host "
			+ "where length(staffbroadbeanlogin) > 10 and newopportunity = true) a "
			+ "where LOCATE('.', staffbroadbeanlogin_without_consultant) > 0 and LOCATE('.', staffbroadbeanlogin_without_consultant, LOCATE('.', staffbroadbeanlogin_without_consultant) + 1) > 0 ",
			DataOrigin.RPM),
			
	;
	
	private String query;
	
	private DataOrigin dataOrigin;
	
	public String getQuery() {
		return query;
	}
	
	public DataOrigin getDataOrigin() {
		return dataOrigin;
	}

	private Dimension(String query, DataOrigin dataOrigin) {
		this.query = query;
		this.dataOrigin = dataOrigin;
	}
	
	
}
