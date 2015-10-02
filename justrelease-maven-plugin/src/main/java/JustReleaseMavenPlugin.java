import com.justrelease.JustReleaseCLI;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * This goal will say a message.
 *
 * @goal justrelease
 */
public class JustReleaseMavenPlugin extends AbstractMojo {

    /**
     * @parameter expression="${params}"
     */
    private String url;
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println("hello world");
        try {
            new JustReleaseCLI().main(url.split(" "));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
