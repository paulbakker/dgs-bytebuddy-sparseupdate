package dgs.inputproxy;

import jakarta.annotation.PostConstruct;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InputProxyApplication {

    public static void main(String[] args) {
        ByteBuddyAgent.install();
        TypePool typePool = TypePool.Default.ofSystemLoader();

        new ByteBuddy()
                .rebase(typePool.describe("dgs.inputproxy.codegen.types.SearchInput").resolve(), ClassFileLocator.ForClassLoader.ofSystemLoader())
                .implement(typePool.describe("dgs.inputproxy.FieldSetTracker").resolve())
                .defineField("fieldsSet", typePool.describe("java.util.HashSet").resolve(), Visibility.PRIVATE)
                .defineMethod("isSet", Boolean.class, Visibility.PUBLIC).withParameter(String.class).intercept(MethodDelegation.to(SparseUpdateProxy.class))
                .method(ElementMatchers.nameStartsWith("set"))
                .intercept(MethodDelegation.to(SparseUpdateProxy.class))
                .make()
                .load(InputProxyApplication.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

        SpringApplication.run(InputProxyApplication.class, args);
    }
}
