package eu.ace_design.teaching.tse.jira

import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory
import com.atlassian.jira.rest.client.NullProgressMonitor
import com.atlassian.jira.rest.client.ProgressMonitor
import com.atlassian.jira.rest.client.JiraRestClient
import com.atlassian.jira.rest.client.IssueRestClient
import java.net.URI

import scala.collection.JavaConversions._

class JiraImporter(val username: String, password: String) extends Importer {

  val START = 0
  val MAX_TICKETS = 2000
  
  def load(key: String): Project = {
    log.info("Initializing project [%s]", key)
    val (root, monitor) = initialize
    val builder = buildTicket(root.getIssueClient(), monitor) _
    
    log.info("Retrieving issues for project [%s]", key)
    val project = root.getSearchClient().searchJql(s"project=${key}", MAX_TICKETS, START, monitor)
    val issues =  for (issue <- project.getIssues())
      yield builder(issue.getKey)
    
    Project(key, issues.toList)
  }
  
  
  def buildTicket(client: IssueRestClient, monitor: ProgressMonitor)(key: String): Ticket = {
    log.info("Handling issue [%s]", key)
    val issue = client.getIssue(key, monitor)
    Ticket( key = key, 
            reporter = issue.getReporter().getName, 
            assignee = issue.getAssignee().getName, 
            status = issue.getStatus().getName,
            summary = issue.getSummary,
            description = issue.getDescription)
            
  }
  
  def initialize: (JiraRestClient, ProgressMonitor) = {
    log.info("Connecting to Atlassian Jira Server")
    val factory = new JerseyJiraRestClientFactory 
    val uri = new URI("http://atlassian.polytech.unice.fr:8080");
    val client = factory.createWithBasicHttpAuthentication(uri, username, password);
    val monitor = new NullProgressMonitor
    (client, monitor)
  }
}


trait Importer {
  import com.twitter.logging.Logger
  import com.twitter.logging.config._
  
  protected val log = Logger.get(getClass)
  
  def load(key: String): Project

}
