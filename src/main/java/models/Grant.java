package models;

import java.util.List;

public interface Grant {
	
	String getGrantID();
	String getTitle();
	String getStartDate();
	String getEndDate();
	String getReference();
	List<String> getLeaders();
	List<String> getLeadersNames();
	List<String> getLeadersViewURLs();
	List<String> getTeam();
	List<String> getTeamNames();
	List<String> getTeamViewURLs();
	List<String> getFundingOrganizations();
	String getType();
	
	void setGrantID(String grantID);
	void setTitle(String title);
	void setStartDate(String startDate);
	void setEndDate(String endDate);
	void setReference(String reference);
	void setLeaders(List<String> leader);
	void setLeadersNames(List<String> leaderNames);
	void setLeadersViewURLs(List<String> leadersViewURLs);
	void setFundingOrganizations(List<String> fundingOrganizations);
	void setTeam(List<String> team);
	void setTeamNames(List<String> teamNames);
	void setTeamViewURLs(List<String> teamViewURLs);
	void setType(String type);
	void generateID();
	
	
	
}
