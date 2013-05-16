package eu.ace_design.teaching.tse.jira


case class Ticket(
	val key: String, 
	val reporter: String,
	val assignee: String,
	val status: String,
	val summary: String,
	val description: String)
	
case class Project(val key: String, val tickets: Seq[Ticket])