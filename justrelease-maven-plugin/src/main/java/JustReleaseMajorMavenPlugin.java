import com.justrelease.JustReleaseCLI;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * This goal will say a message.
 *
 * @goal major
 */
public class JustReleaseMajorMavenPlugin extends AbstractMojo {

    /**
     * @parameter expression="${github}"
     */
    private String github;

    /**
     * @parameter expression="${dryRun}"
     */
    private String dryRun;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (dryRun == null) dryRun = "";
            if (dryRun.equals("true")) dryRun = "-dryRun";
            else dryRun = "";
            new JustReleaseCLI().main(new String[]{github, "major", dryRun});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
