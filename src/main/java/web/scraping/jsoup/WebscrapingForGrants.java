package web.scraping.jsoup;

import java.util.List;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import models.Grant;
import models.GrantImpl;

public class WebscrapingForGrants {

	/**
	 * Web scraping from SISIUS page, for Grants
	 */
	private static final String URL_BASE = "https://investigacion.us.es/sisius/";
	private static final String URL_BASE2 = "https://investigacion.us.es";
	private static final String URL_GRANTS = URL_BASE + "sis_proyecto.php?idproy=";

	public static void main(String[] args) throws MalformedURLException, IOException {

		List<Grant> grantList = new ArrayList<>();

		List<String> grantsLink = new ArrayList<>();

		// Get all project urls from SISIUS
		org.jsoup.nodes.Document grants = Jsoup.connect(URL_BASE).data("text2search", "%%%").data("en", "4")
				.data("inside", "1").maxBodySize(10 * 1024 * 1024).post();

		Elements elements = grants.select("td.data a");

		for (Iterator<Element> iterator = elements.iterator(); iterator.hasNext();) {

			Element researcher = iterator.next();

			String link = researcher.attr("href");

			if (link.contains("sis_proyecto.php")) {

				grantsLink.add(link);
			}

		}

		Grant grant = new GrantImpl();

		int count = 0;
		int total = 0;
		for (String grantID : grantsLink) { // Pages Iteration

			// if(count == 200) break;

			org.jsoup.nodes.Document doc = Jsoup.parse(new URL(URL_BASE2 + grantID), 10000);

			// Verification of Grant page:
			Elements projectType = doc.getElementsByTag("h3");
			if (projectType.size() == 0) {
				// System.out.println("Empty project" + URL_BASE2 + grantID );
				total++;
				continue;
			} else if (!projectType.get(0).toString().contains("Ayudas")) {
				// System.out.println("Not a grant: " + URL_BASE2 + grantID);
				continue;
			}
			/* GET Grant title */
			Elements titleTag = doc.getElementsByTag("h5");

			if (titleTag.size() == 0) {
				// System.out.println("Empty grant: " + URL_BASE2 + grantID);
				total++;
				continue;
			}

			total++;

			String title = titleTag.get(0).text();

			grant = generateGrant(title);

			/* Obtain grant main data */
			Elements mainDataInList = doc.getElementsByAttributeValue("align", "left");

			if (mainDataInList.size() != 1) {
				System.out.println("There are more than one element with align:left property");
			} else {
				Element mainData = mainDataInList.get(0);

				List<String> leaders = new ArrayList<>();
				List<String> leadersNames = new ArrayList<>();
				List<String> leadersViewURLs = new ArrayList<>();
				String reference = "";
				String startDate = "";
				String endDate = "";
				String type = "";

				Boolean containsFundingOrganization = false;

				/* Obtain non-list data */
				for (Iterator<Element> it = mainData.getElementsByTag("p").iterator(); it.hasNext();) {
					Element e = it.next();
					// System.out.println("ITEM \n " + e.toString() + "\n");
					/* GET Grant basic information */
					if (e.hasText() && e.text().contains("Referencia")) {

						String[] basicInformation = e.toString().split("<br>");

						for (int i = 0; i < basicInformation.length; i++) {
							/* GET Grant leaders */
							if (basicInformation[i].length() != 0) {
								if (basicInformation[i].contains("Responsable")) {
									String leader = "";
									String leaderURL = "";
									String researchName = "";
									if (basicInformation[i].contains(": ")
											&& basicInformation[i].split(": ").length > 1) {
										leader = basicInformation[i].split(": ")[1];
										leaderURL = leader.split("<a href=\"")[1].split("\">")[0];
										
										// We search now in the research page for integration resource field
										
										org.jsoup.nodes.Document researcherPage = Jsoup
												.parse(new URL(URL_BASE2 + leaderURL), 10000);
										Element researcherMainInformation = researcherPage
												.getElementsByAttributeValue("align", "left").get(0);

										try {
											researchName = researcherMainInformation.getElementsByTag("p").get(0)
													.toString().split("<")[1].split(">")[1];
										} catch (IndexOutOfBoundsException e2) {
											researchName = leader.split("\">")[1].split("</")[0];
										}
										Elements urlsInResearchInfo = researcherMainInformation.getElementsByTag("a");

										Boolean hasORCID = false;
										for (Iterator<Element> urlIt = urlsInResearchInfo.iterator(); urlIt
												.hasNext();) {
											Element urlInfo = urlIt.next();
											String urlHref = urlInfo.attr("href");
											if (urlHref.contains("orcid")) {
												leaders.add(
														"https://si1718-dfr-researchers.herokuapp.com/api/v1/researchers/"
																+ urlInfo.text());
												leadersViewURLs.add(
														"https://si1718-dfr-researchers.herokuapp.com/#!/researchers/"
																+ urlInfo.text() + "/view");
												hasORCID = true;
												leadersNames.add(researchName);
												break;
											}
										}
										if (!hasORCID) {
											String leaderStr = leaderURL.split("idpers=")[1];
											leaders.add(
													"https://si1718-dfr-researchers.herokuapp.com/api/v1/researchers/"
															+ leaderStr);
											leadersViewURLs
													.add("https://si1718-dfr-researchers.herokuapp.com/#!/researchers/"
															+ leaderStr + "/view");
											leadersNames.add(researchName);
										}
									}
								} else if (basicInformation[i].contains("Referencia")) {
									reference = basicInformation[i].split(": ")[1];
								} else if (basicInformation[i].contains("Fecha de Inicio")) {
									startDate = basicInformation[i].split(": ")[1];
								} else if (basicInformation[i].contains("Fecha de Finalización")) {
									endDate = basicInformation[i].split(": ")[1];
								} else if (basicInformation[i].contains("Tipo de Proyecto/Ayuda")) {
									type = basicInformation[i].split(": ")[1];
								}
							}
						}

						grant.setLeaders(leaders);
						grant.setLeadersNames(leadersNames);
						grant.setLeadersViewURLs(leadersViewURLs);
						grant.setEndDate(endDate);
						grant.setStartDate(startDate);
						grant.setReference(reference);
						System.out.println("La referencia es: " + reference);
						grant.setType(type);
					} else if (e.hasText() && e.text().contains("Empresa/Organismo financiador/es")) {
						containsFundingOrganization = true;
					}
				}

				/* Obtain list data (Team members and funding organizations) */
				Boolean fundingListProcessed = false;
				Boolean teamListProcessed = false;
				List<String> fundingOrganizations = new ArrayList<>();
				List<String> team = new ArrayList<>();
				List<String> teamNames = new ArrayList<>();
				List<String> teamViewURLs = new ArrayList<>();
				for (Iterator<Element> it2 = mainData.getElementsByTag("ul").iterator(); it2.hasNext();) {

					Element e2 = it2.next();

					// System.out.println("Unordered List item: \n" + e2 + "\n");

					if (e2.hasText() && containsFundingOrganization && !fundingListProcessed) {
						Elements fundingOrganizationElements = e2.getElementsByTag("li");
						fundingOrganizations = fundingOrganizationElements.eachText();
						fundingListProcessed = true;
					} else if (e2.hasText() && !teamListProcessed) {
						Elements teamElements = e2.children();
						// System.out.println(teamElements);
						for (Iterator<Element> it2_1 = teamElements.iterator(); it2_1.hasNext();) {
							Elements teamElementsByRole = it2_1.next().getElementsByTag("a");
							for (Iterator<Element> it2_1_1 = teamElementsByRole.iterator(); it2_1_1.hasNext();) {
								Element e2_1_1 = it2_1_1.next();

								Boolean hasORCID = false;

								String urlResearcher = e2_1_1.attr("href");
								if (urlResearcher.substring(0) != "/")
									urlResearcher = "/" + urlResearcher;
								org.jsoup.nodes.Document researcherPage = Jsoup
										.parse(new URL(URL_BASE2 + e2_1_1.attr("href")), 10000);
								String researchName = "";
								try {
									Element researcherMainInformation = researcherPage
											.getElementsByAttributeValue("align", "left").get(0);
									researchName = researcherMainInformation.getElementsByTag("p").get(0).toString()
											.split("<")[1].split(">")[1];
									
									// We search now in the research page for integration resource field

									
									Elements urlsInResearchInfo = researcherMainInformation.getElementsByTag("a");

									for (Iterator<Element> urlIt = urlsInResearchInfo.iterator(); urlIt.hasNext();) {
										Element urlInfo = urlIt.next();
										String urlHref = urlInfo.attr("href");
										if (urlHref.contains("orcid")) {
											team.add("https://si1718-dfr-researchers.herokuapp.com/api/v1/researchers/"
													+ urlInfo.text());
											teamViewURLs
													.add("https://si1718-dfr-researchers.herokuapp.com/#!/researchers/"
															+ urlInfo.text() + "/view");
											hasORCID = true;
											teamNames.add(researchName);
											break;
										}
									}
								} catch (IndexOutOfBoundsException e3) {
									researchName = e2_1_1.text();
								}

								if (!hasORCID) {
									String teamStr = e2_1_1.attr("href").split("idpers=")[1];
									team.add("https://si1718-dfr-researchers.herokuapp.com/api/v1/researchers/"
											+ teamStr);
									teamViewURLs.add("https://si1718-dfr-researchers.herokuapp.com/#!/researchers/"
											+ teamStr + "/view");
									teamNames.add(researchName);
								}
							}
						}
						teamListProcessed = true;

					}

				}
				grant.setFundingOrganizations(fundingOrganizations);
				grant.setTeam(team);
				grant.setTeamNames(teamNames);
				grant.setTeamViewURLs(teamViewURLs);
			}

			grant.generateID();

			grantList.add(grant);
			count++;

			System.out.println("Number of grants detected until now: " + count);
		}

		// DB access used in the microservice secure section
		MongoClientURI uri = new MongoClientURI(
				"mongodb://admin:passwordCurro@ds129386.mlab.com:29386/si1718-flp-grants-secure");

		// DB access used in the microservice basic section
		// MongoClientURI uri = new
		// MongoClientURI("mongodb://curro:curro@ds149855.mlab.com:49855/si1718-flp-grants");

		MongoClient client = new MongoClient(uri);
		try {
			MongoDatabase db = client.getDatabase(uri.getDatabase());
			MongoCollection<org.bson.Document> docResearchers = db.getCollection("grants");

			List<org.bson.Document> grantSet = new ArrayList<>();
			for (int index = 0; index < count; index++) {

				BasicDBObject document = new BasicDBObject();

				org.bson.Document grantForMongoDB = getGrantInJson(grantList.get(index));

				// document.put("detail", grantForMongoDB);

				grantSet.add(grantForMongoDB);
				// docResearchers.insertOne(grantForMongoDB);
			}
			docResearchers.insertMany(grantSet);

			System.out.println("///////////////RESULTS////////////////////");
			System.out.println("Total number of grants: " + total);
			System.out.println("Not empty grants detected: " + count);
		} finally {

			client.close();
		}
	}

	public static Grant generateGrant(String title) {

		GrantImpl grant = new GrantImpl();

		grant.setTitle(title);

		return grant;
	}

	// Insert the data obtained in web scraping in a model with all the information
	// required for the resource
	public static Document getGrantInJson(Grant grant) {
		Document documentDetail = new Document();

		documentDetail.put("idGrant", grant.getGrantID());
		documentDetail.put("reference", grant.getReference());
		documentDetail.put("title", grant.getTitle());
		documentDetail.put("leaders", grant.getLeaders());
		documentDetail.put("leadersName", grant.getLeadersNames());
		documentDetail.put("leadersViewURL", grant.getLeadersViewURLs());
		documentDetail.put("teamMembers", grant.getTeam());
		documentDetail.put("teamMembersName", grant.getTeamNames());
		documentDetail.put("teamMembersViewURL", grant.getTeamViewURLs());
		documentDetail.put("type", grant.getType());
		documentDetail.put("startDate", grant.getStartDate());
		documentDetail.put("endDate", grant.getEndDate());
		documentDetail.put("fundingOrganizations", grant.getFundingOrganizations());
		documentDetail.put("viewURL", "https://si1718-flp-grants.herokuapp.com/#!/viewgrant/" + grant.getGrantID());
		documentDetail.put("keywords",
				generateKeywords(grant.getTitle(), grant.getFundingOrganizations(), grant.getType()));

		return documentDetail;
	}

	public static List<String> generateKeywords(String grantTitle, List<String> grantFundingOrganizations,
			String grantType) {
		// NOTE: there are some grants with a very common structure.
		// These grants only contains a useful keyword. We recover the funding
		// organization too as a keyword
		List<String> res = new ArrayList<>();
		if (grantTitle.contains("Ayuda a la Consolidación del Grupo de Investigación")
				|| grantTitle.contains("Incentivo al Grupo de Investigación")) {

			if (grantTitle.contains("Incentivo al Grupo de Investigación"))
				res.add("Incentivo");

			String[] keywordFromTitleSplit = grantTitle.split("-")[0].split(" ");
			res.add(keywordFromTitleSplit[keywordFromTitleSplit.length - 1]);
			for (String grantFundingOrganization : grantFundingOrganizations) {
				if (grantFundingOrganization.contains("Consejería de Innovación, Ciencia y Empresas"))
					res.add("Consejería de Innovación, Ciencia y Empresas");
				else if (grantFundingOrganization.contains("Plan Andaluz de Investigación"))
					res.add("Plan Andaluz de Investigación");
				else if (grantFundingOrganization.contains("Junta de Andalucía (")) {
					res.add(grantFundingOrganization.split("\\(")[1].split("\\)")[0]);
				} else if (grantFundingOrganization.contains("Junta de Andalucía")
						&& grantFundingOrganization.length() < 22) {
					res.add("Junta de Andalucía");
				} else {
					String[] fundingOrganizationSplit = grantFundingOrganization.split(" ");

					if (fundingOrganizationSplit.length > 2) {
						for (int j = 2; j < fundingOrganizationSplit.length; j++) {
							String possibleKeyword = fundingOrganizationSplit[j];
							if (Character.isUpperCase(possibleKeyword.charAt(0)) && possibleKeyword.length() > 3
									&& !Character.isUpperCase(possibleKeyword.charAt(1))
									&& !Character.isDigit(possibleKeyword.charAt(1))) {
								String keywordProccessed = possibleKeyword.replace(",", "");
								keywordProccessed = keywordProccessed.replace(":", "");
								keywordProccessed = keywordProccessed.replace(".", "");
								res.add(keywordProccessed);
								break;
							}
						}
					}
				}
			}

		} else {
			// Extract one keyword from grant Title if it's possible
			if (grantTitle.length() > 10) {
				String[] grantTitleSplit = grantTitle.split(" ");
				if (grantTitleSplit.length > 1) {
					for (int i = 1; i < grantTitleSplit.length; i++) {
						String possibleKeyword = grantTitleSplit[i];
						if (Character.isUpperCase(possibleKeyword.charAt(0)) && possibleKeyword.length() > 3
								&& !Character.isUpperCase(possibleKeyword.charAt(1))) {
							String keywordProccessed = possibleKeyword.replace(",", "");
							keywordProccessed = keywordProccessed.replace(":", "");
							keywordProccessed = keywordProccessed.replace(".", "");
							res.add(keywordProccessed);
							break;
						}
					}
				}
			}

			// Extract one keyword for each funding organization if it's possible
			if (grantFundingOrganizations != null && grantFundingOrganizations.size() != 0) {
				for (String grantFundingOrganization : grantFundingOrganizations) {
					String[] fundingOrganizationSplit = grantFundingOrganization.split(" ");

					if (fundingOrganizationSplit.length > 2) {
						for (int j = 2; j < fundingOrganizationSplit.length; j++) {
							String possibleKeyword = fundingOrganizationSplit[j];
							if (Character.isUpperCase(possibleKeyword.charAt(0)) && possibleKeyword.length() > 3
									&& !Character.isUpperCase(possibleKeyword.charAt(1))) {
								String keywordProccessed = possibleKeyword.replace(",", "");
								keywordProccessed = keywordProccessed.replace(":", "");
								keywordProccessed = keywordProccessed.replace(".", "");
								res.add(keywordProccessed);
								break;
							}
						}
					}
				}
			}
			if (grantType != null && grantType.length() != 0) {
				String[] grantTypeSplit = grantType.split(" ");
				if (grantTypeSplit.length > 1) {
					for (int i = 1; i < grantTypeSplit.length; i++) {
						String possibleKeyword = grantTypeSplit[i];
						if (Character.isUpperCase(possibleKeyword.charAt(0)) && possibleKeyword.length() > 3
								&& !Character.isUpperCase(possibleKeyword.charAt(1))) {
							String keywordProccessed = possibleKeyword.replace(",", "");
							keywordProccessed = keywordProccessed.replace(":", "");
							keywordProccessed = keywordProccessed.replace(".", "");
							res.add(keywordProccessed);
							break;
						}
					}
				}
			}
		}
		return res;
	}

}
