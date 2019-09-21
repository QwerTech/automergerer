import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.MergeCommand
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.api.ResetCommand.ResetType.HARD
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.api.errors.NotMergedException
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.notes.NotesMergeConflictException
import org.eclipse.jgit.transport.JschConfigSessionFactory
import org.eclipse.jgit.transport.SshSessionFactory
import java.io.File
import java.lang.Exception
import java.nio.file.Path

class GitAdapter(private val rootFolder: String, private val repoName: String, private val repoUrl: String) {
    private val currentBranch: String? = null
    private val git: Git by lazy { initGit() }

    private fun initGit(): Git {
        File(rootFolder).mkdirs()
        val repoDir = File(Path.of(rootFolder, repoName).toUri())
        return if (!repoDir.exists()) {
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(repoDir)
                    .call()
        } else {
            Git.open(repoDir)
        }
    }

    fun fetch() {
        git.fetch().call()
    }

    fun checkout(branch: String) {
        git.checkout().setName(branch).call()
    }

    fun pull() {
        git.pull()
//                .setRemoteBranchName(currentBranch)
                .call()
    }


    fun push() {
        git.push()
//                .add(currentBranch)
                .call()
    }

    fun merge(branchFrom: String) {
        val mergeResult = git.merge()
                .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                .include(ObjectId.fromString(branchFrom))
                .call()
        if (!mergeResult.mergeStatus.isSuccessful) {
            git.reset().setMode(HARD).call();
            throw MergeConflictException("Failed to merge from branch:$branchFrom");
        }
    }

    class MergeConflictException(message: String) : Exception(message)
}