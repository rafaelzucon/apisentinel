val csvFileName: String = (project.findProperty("csvName") as String?) ?: "input.csv"
val apisentinelCron: String = (project.findProperty("apisentinelCron") as String?) ?: "*/15 * * * * *"

extra["csvFileName"] = csvFileName
extra["apisentinelCron"] = apisentinelCron