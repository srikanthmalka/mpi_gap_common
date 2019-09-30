package au.com.michaelpage.gap.common.generator;

public enum Hit {
	CV_RECEIVED("select 1 as v, gap.id as tid, md5hash(a.bb_response_email_address) as cid, md5hash(a.bb_response_email_address) as uid, md5hash(a.bb_response_email_address) as cd2, h.host as dh, " + 
		" a.bb_job_board as cs, 'offline' as cm, 'cv-received' as cn," +
		" rpm.id || '_' || substring_before(a.bb_job_reference, '/') as cd10, '/crm-pageviews/' || rpm.id || '_' || substring_before(a.bb_job_reference, '/') || '/cv-received' as dp, 'pageview' as t, " +
		" '' as cd50, '' as cd51, '' as cd52, str_to_date(bb_date_and_time_received, 'dd MMM yyyy HH:mm') as cd53 " +
		" from resp2 a" +
		" cross join GapProfile gap" + 
		" cross join RPM rpm" + 
		" cross join Host h" + 
		" where gap.type in ('REGIONAL', 'LOCAL') " +
		" and a.bb_job_board in (select name from JobBoard where nonNextGen = true) " +
		" ${brand_condition} " +
		" order by md5hash(a.bb_response_email_address), bb_job_reference",
		DataOrigin.BROADBEAN
	),
	
	NEW_REGISTRATION("select 1 as v, 1 as ni, gap.id as tid, p.md5emailaddress as cid, p.md5EmailAddress as uid, p.md5EmailAddress as cd2, h.host as dh, 'crm' as cs, 'offline' as cm, 'registration' as cn, "
		+ " case when e.opportunityRef is not null then rpm.id || '_' || e.opportunityRef else '' end as cd10, "
		+ "'/crm-pageviews/' || rpm.id || '_' || case when e.opportunityRef is not null then e.opportunityRef else 'general' end || '/registered-new' as dp, 'pageview' as t, "
		+ " rpm.id || '_' || e.eventref as cd50, '' as cd51, case when upper(e.displayname) like '%GREEN%' then 'Green' else 'Amber' end as cd52, e.createtimestamp as cd53 "
		+ " from event e, eventrole er, person p, GapProfile gap, RPM rpm, Host h where gap.type in ('REGIONAL', 'LOCAL') and e.type = 'P02' "
		+ " and (upper(e.displayname) like '%GREEN%' or upper(e.displayname) like '%AMBER%') and er.eventref = e.eventRef and er.type = 'D' and p.personref = er.personref "
		+ " and {fn timestampdiff(SQL_TSI_MINUTE, p.createtimestamp, e.createtimestamp)} between 0 and 10 "
		+ " order by p.md5emailaddress, gap.id, e.opportunityRef, e.createtimestamp",
		DataOrigin.RPM
	),

	REGISTRATION("select 1 as v, 1 as ni, gap.id as tid, p.md5emailaddress as cid, p.md5EmailAddress as uid, p.md5EmailAddress as cd2, h.host as dh, 'crm' as cs, 'offline' as cm, 'registration' as cn, "
			+ " case when e.opportunityRef is not null then rpm.id || '_' || e.opportunityRef else '' end as cd10, "
			+ "'/crm-pageviews/' || rpm.id || '_' || case when e.opportunityRef is not null then e.opportunityRef else 'general' end || '/registered' as dp, 'pageview' as t, "
			+ " rpm.id || '_' || e.eventref as cd50, '' as cd51, case when upper(e.displayname) like '%GREEN%' then 'Green' else 'Amber' end as cd52, e.createtimestamp as cd53 "
			+ " from event e, eventrole er, person p, GapProfile gap, RPM rpm, Host h where gap.type in ('REGIONAL', 'LOCAL') and e.type = 'P02' "
			+ " and (upper(e.displayname) like '%GREEN%' or upper(e.displayname) like '%AMBER%') and er.eventref = e.eventRef and er.type = 'D' and p.personref = er.personref "
			+ " and {fn timestampdiff(SQL_TSI_MINUTE, p.createtimestamp, e.createtimestamp)} > 10"
			+ " order by p.md5emailaddress, gap.id, e.opportunityRef, e.createtimestamp",
		DataOrigin.RPM
	),
	
	SHORT_LIST("select 1 as v, 1 as ni, gap.id as tid, p.md5emailaddress as cid, p.md5EmailAddress as uid, p.md5EmailAddress as cd2, h.host as dh, 'crm' as cs, 'offline' as cm, 'shortlist' as cn, "
			+ " case when e.opportunityRef is not null then rpm.id || '_' || e.opportunityRef else '' end as cd10, "
			+ "'/crm-pageviews/' || rpm.id || '_' || case when e.opportunityRef is not null then e.opportunityRef else 'general' end || '/shortlist' as dp, 'pageview' as t, "
			+ " rpm.id || '_' || e.eventref as cd50, '' as cd51, 'Green' as cd52, e.createtimestamp as cd53 "
			+ " from event e, eventrole er, person p, GapProfile gap, RPM rpm, Host h where gap.type in ('REGIONAL', 'LOCAL') and e.type = 'P02' "
			+ " and upper(e.displayname) like '%GREEN%' and er.eventref = e.eventRef and er.type = 'D' and p.personref = er.personref "
			+ " order by p.md5emailaddress, gap.id, e.opportunityRef, e.createtimestamp",
		DataOrigin.RPM
	),

	CV_SENT("select 1 as v, 1 as ni, gapid as tid, md5emailaddress as cid, md5EmailAddress as uid, md5EmailAddress as cd2, host as dh, 'crm' as cs, 'offline' as cm, 'cv-sent' as cn, "
			+ " case when a.opportunityRef is not null then rpmid || '_' || a.opportunityRef else '' end as cd10, "
			+ " '/crm-pageviews/' || rpmid || '_' || case when a.opportunityRef is not null then a.opportunityRef else 'general' end || '/cv-sent' as dp, 'pageview' as t, "
			+ " rpmid || '_' || e.eventref as cd50, '' as cd51, '' as cd52, e.createtimestamp as cd53 "
			+ " from (select min(e.eventref) eventref, e.opportunityref, gap.id gapid, rpm.id rpmid, h.host host, p.md5emailaddress from event e, eventrole er, person p, GapProfile gap, RPM rpm, Host h "
			+ " where gap.type in ('REGIONAL', 'LOCAL') and ((e.type = 'N23' and er.type = 'D') or (e.type = 'KE02' and er.type = 'K')) "
			+ " and er.eventref = e.eventRef and p.personref = er.personref group by e.opportunityref, gap.id, rpm.id, h.host, p.md5emailaddress) a, event e where a.eventref = e.eventref "
			+ " order by md5emailaddress, gapid, a.opportunityRef, e.createtimestamp",
		DataOrigin.RPM
	),
	
	CLIENT_INTERVIEW("select 1 as v, 1 as ni, gap.id as tid, p.md5emailaddress as cid, p.md5EmailAddress as uid, p.md5EmailAddress as cd2, h.host as dh, 'crm' as cs, 'offline' as cm, 'interview' as cn, "
			+ " case when e.opportunityRef is not null then rpm.id || '_' || e.opportunityRef else '' end as cd10, "
			+ " '/crm-pageviews/' || rpm.id || '_' || case when e.opportunityRef is not null then e.opportunityRef else 'general' end || '/interview-' || substr(e.type, 4) as dp, 'pageview' as t, "
			+ " rpm.id || '_' || e.eventref as cd50, '' as cd51, '' as cd52, e.createtimestamp as cd53 "
			+ " from event e, eventrole er, person p, GapProfile gap, RPM rpm, Host h where gap.type in ('REGIONAL', 'LOCAL') and e.type in ('P701', 'P702', 'P703') "
			+ " and er.eventref = e.eventRef and er.type = 'D' and p.personref = er.personref"
			+ " order by p.md5emailaddress, gap.id, e.opportunityRef, e.createtimestamp",
		DataOrigin.RPM
	),
	
	FEE_PAID_TEMP("select 1 as v, gap.id as tid, p.md5emailaddress as cid, p.md5emailaddress as uid, p.md5EmailAddress as cd2, h.host as dh, 'crm' as cs, 'offline' as cm, 'placement' as cn, "
			+ " rpm.id || '_' || e.opportunityRef as cd10, '/crm-pageviews/' || rpm.id || '_' || e.opportunityRef || '/candidate-placed' as dp, 'transaction' as t, 'purchase' as pa, "
			+ " p.md5emailaddress || '-' || substring_before(replace(cast(e.createtimestamp as varchar(50)), ' |:|-', ''), '.') as ti, "
			+ " case "
			+ " when timeunit = 'H' "
			+ " then trim(cast((tp.rateinvoice - tp.ratepayment) * (case when hoursperday is null then 8 else case when hoursperday > 9 then 8 else hoursperday end end) * "
			+ " (case when (daysperweek is null or daysperweek > 5) then 5 else daysperweek end) * ({fn timestampdiff(SQL_TSI_WEEK, tp.startdate, tp.enddate)} + 1) * gap.exchangeRate as char(30))) "
			+ " when timeunit = 'D' "
			+ " then trim(cast((tp.rateinvoice - tp.ratepayment) * "
			+ " (case when daysperweek is null then 5 else daysperweek end) * ({fn timestampdiff(SQL_TSI_WEEK, tp.startdate, tp.enddate)} + 1) * gap.exchangeRate as char(30))) "
			+ " when timeunit = 'M' "
			+ " then trim(cast((tp.rateinvoice - tp.ratepayment) * ({fn timestampdiff(SQL_TSI_MONTH, tp.startdate, tp.enddate)} + 1) * gap.exchangeRate as char(30))) "
			+ " end as tr, "
			+ " rpm.id || '_' || e.eventref as cd50, 'Temp' as cd51, '' as cd52, e.createtimestamp as cd53 "
			+ " from event e, eventrole er, person p, tempplacement tp, GapProfile gap, RPM rpm, Host h where gap.type in ('REGIONAL', 'LOCAL') and (e.type like 'I%' or e.type = 'H') "
			+ " and er.eventref = e.eventRef and er.type in ('I', 'H') and p.personref = er.personref and tp.eventref = e.eventref and e.opportunityref is not null "
			+ " and {fn timestampdiff(SQL_TSI_WEEK, tp.startdate, tp.enddate)} >= 0 and tp.rateinvoice > tp.ratepayment"
			+ " order by p.md5emailaddress, gap.id, e.opportunityRef, e.createtimestamp",
		DataOrigin.RPM
	),
	
	FEE_PAID_PERM("select 1 as v, gap.id as tid, p.md5emailaddress as cid, p.md5emailaddress as uid, p.md5EmailAddress as cd2, h.host as dh, 'crm' as cs, 'offline' as cm, 'placement' as cn,  "
			+ " rpm.id || '_' || e.opportunityRef as cd10, '/crm-pageviews/' || rpm.id || '_' || e.opportunityRef || '/candidate-placed' as dp, 'transaction' as t, 'purchase' as pa, "
			+ " p.md5emailaddress || '-' || substring_before(replace(cast(e.createtimestamp as varchar(50)), ' |:|-', ''), '.') as ti, trim(cast(cast(pp.feeamount as decimal(17,2)) * gap.exchangeRate as char(20))) as tr, "
			+ " rpm.id || '_' || e.eventref as cd50, case when pp.PlacementType is not null then pp.PlacementType else case e.type when 'IS10' then 'Perm' when 'IS45' then 'Retainer' when 'IS20' then 'Fixed-term' else 'Unknown' end end as cd51, '' as cd52, e.createtimestamp as cd53 "
			+ " from event e, eventrole er, person p, permplacement pp, GapProfile gap, RPM rpm, Host h where gap.type in ('REGIONAL', 'LOCAL') and er.type='I' "
			+ " and er.eventref = e.eventRef and p.personref = er.personref and pp.eventref = e.eventref and e.opportunityref is not null"
			+ " order by p.md5emailaddress, gap.id, e.opportunityRef, e.createtimestamp",
		DataOrigin.RPM
	),
	
	FEE_PAID_TEMP_PAGEVIEW("select 1 as v, 1 as ni, gap.id as tid, p.md5emailaddress as cid, p.md5emailaddress as uid, p.md5EmailAddress as cd2, h.host as dh, 'crm' as cs, 'offline' as cm, 'placement' as cn,  "
			+ " rpm.id || '_' || e.opportunityRef as cd10, '/crm-pageviews/' || rpm.id || '_' || e.opportunityRef || '/candidate-placed' as dp, 'pageview' as t, "
			+ " rpm.id || '_' || e.eventref as cd50, 'Temp' as cd51, '' as cd52, e.createtimestamp as cd53 "
			+ " from event e, eventrole er, person p, tempplacement tp, GapProfile gap, RPM rpm, Host h where gap.type in ('REGIONAL', 'LOCAL') and (e.type like 'I%' or e.type = 'H') "
			+ " and er.eventref = e.eventRef and er.type in ('I', 'H') and p.personref = er.personref and tp.eventref = e.eventref and e.opportunityref is not null"
			+ " order by p.md5emailaddress, gap.id, e.opportunityRef, e.createtimestamp",
		DataOrigin.RPM
	),
	
	FEE_PAID_PERM_PAGEVIEW("select 1 as v, 1 as ni, gap.id as tid, p.md5emailaddress as cid, p.md5emailaddress as uid, p.md5EmailAddress as cd2, h.host as dh, 'crm' as cs, 'offline' as cm, 'placement' as cn,  "
			+ " rpm.id || '_' || e.opportunityRef as cd10, '/crm-pageviews/' || rpm.id || '_' || e.opportunityRef || '/candidate-placed' as dp, 'pageview' as t, "
			+ " rpm.id || '_' || e.eventref as cd50, case when pp.PlacementType is not null then pp.PlacementType else case e.type when 'IS10' then 'Perm' when 'IS45' then 'Retainer' when 'IS20' then 'Fixed-term' else 'Unknown' end end as cd51, '' as cd52, e.createtimestamp as cd53 "
			+ " from event e, eventrole er, person p, permplacement pp, GapProfile gap, RPM rpm, Host h where gap.type in ('REGIONAL', 'LOCAL') and er.type='I' "
			+ " and er.eventref = e.eventRef and p.personref = er.personref and pp.eventref = e.eventref and e.opportunityref is not null"
			+ " order by p.md5emailaddress, gap.id, e.opportunityRef, e.createtimestamp",
		DataOrigin.RPM
	),
	
	;
	
	private String query;
	
	private DataOrigin dataOrigin;
	
	public String getQuery() {
		return query;
	}
	
	public DataOrigin getDataOrigin() {
		return dataOrigin;
	}

	private Hit(String query, DataOrigin dataOrigin) {
		this.query = query;
		this.dataOrigin = dataOrigin;
	}

}