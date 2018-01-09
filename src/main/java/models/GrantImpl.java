package models;

import java.util.List;

public class GrantImpl implements Grant{

	private String grantID;
	private String title;
	private String startDate;
	private String endDate;
	private String reference;
	private List<String> leaders;
	private List<String> leadersNames;
	private List<String> leadersViewURLs;
	private List<String> fundingOrganizations;
	private List<String> team;
	private List<String> teamNames;
	private List<String> teamViewURLs;
	private String type;
	
	
	public GrantImpl() {
		
	}
	
	@Override
	public String getGrantID() {
		return grantID;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getStartDate() {
		return startDate;
	}

	@Override
	public String getEndDate() {
		return endDate;
	}

	@Override
	public String getReference() {
		return reference;
	}

	@Override
	public List<String> getLeaders() {
		return leaders;
	}
	
	@Override
	public List<String> getLeadersNames() {
		return leadersNames;
	}
	
	@Override
	public List<String> getLeadersViewURLs() {
		return leadersViewURLs;
	}
	
	@Override
	public List<String> getFundingOrganizations() {
		return fundingOrganizations;
	}

	@Override
	public List<String> getTeam() {
		return team;
	}
	
	@Override
	public List<String> getTeamNames() {
		return teamNames;
	}
	
	@Override
	public List<String> getTeamViewURLs() {
		return teamViewURLs;
	}
	
	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setGrantID(String grantID) {
		this.grantID = grantID;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	@Override
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	@Override
	public void setReference(String reference) {
		this.reference = reference;
	}

	@Override
	public void setLeaders(List<String> leaders) {
		this.leaders = leaders;
	}
	
	@Override
	public void setLeadersNames(List<String> leadersNames) {
		this.leadersNames = leadersNames;
	}

	@Override
	public void setLeadersViewURLs(List<String> leadersViewURLs) {
		this.leadersViewURLs = leadersViewURLs;
	}
	
	@Override
	public void setFundingOrganizations(List<String> fundingOrganizations) {
		this.fundingOrganizations = fundingOrganizations;
	}

	@Override
	public void setTeam(List<String> team) {
		this.team = team;
	}
	@Override
	public void setTeamNames(List<String> teamNames) {
		this.teamNames = teamNames;
	}
	@Override
	public void setTeamViewURLs(List<String> teamViewURLs) {
		this.teamViewURLs = teamViewURLs;
	}
	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "GrantImpl [grantID=" + grantID + ", title=" + title + ", startDate=" + startDate + ", endDate="
				+ endDate + ", reference=" + reference + ", leaders=" + leaders + ", fundingOrganizations="
				+ fundingOrganizations + ", team=" + team + ", type=" + type + "]";
	}
	
	@Override
	public void generateID(){
		try {
			this.grantID = this.reference.replace("/", "-");
		}catch(NullPointerException e) {
		}
	}
	
	
}
