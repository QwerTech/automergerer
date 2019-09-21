import ConfigKeys.`repositories-path`
import ConfigKeys.branches
import ConfigKeys.`repository-name`
import ConfigKeys.`repository-url`
import com.natpryce.konfig.*
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import java.io.File

fun main(args: Array<String>) {

    val argProps = parseArgs(args, CommandLineOption(`repositories-path`, short = "rp", description = "local folder where to put repositories"))

    val configuration = argProps.first overriding
            systemProperties() overriding
            EnvironmentVariables() overriding
            ConfigurationProperties.fromOptionalFile(File("~/automergerer.properties")) overriding
            ConfigurationProperties.fromResource("defaults.properties")

    println(configuration[`repositories-path`])
    val gitAdapter = GitAdapter(configuration[`repositories-path`], configuration[`repository-name`], configuration[`repository-url`])
    Mergerer(gitAdapter, configuration).start()
}


class Mergerer(private val gitAdapter: GitAdapter, private val config: Configuration) {

    private val branches = BranchesParser(config).parse()

    fun start() {
        branches.forEach {
            gitAdapter.fetch()
            gitAdapter.checkout(it.second)
            gitAdapter.pull()
            gitAdapter.merge(it.second)
        }
    }

}

object ConfigKeys {
    val `repositories-path` by stringType
    val `repository-url` by stringType
    val `repository-name` by stringType
    val branches by stringType
}

class BranchesParser(private val config: Configuration) {
    fun parse(): Set<Pair<String, String>> {
        val branches = config[branches]
        return branches.split(",")
                .map { it.split(":") }
                .map { it.first() to it.last() }
                .toSet()
    }
}
