package dgs.inputproxy;

import jakarta.annotation.PostConstruct;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.reflect.Type;

@SpringBootApplication
public class InputProxyApplication {

    @PostConstruct
    public void initProxies() {

    }

    public static void main(String[] args) {

        ByteBuddyAgent.install();
        TypePool typePool = TypePool.Default.ofSystemLoader();

//        new AgentBuilder.Default()
//                .type(ElementMatchers.nameContains("TitleInput"))
//                .transform((builder, td, cl, m, p) ->
//                                builder.visit(Advice.to(Interceptor.class).on(ElementMatchers.nameStartsWith("set").and(MethodDescription::isMethod)))).installOnByteBuddyAgent();
//

//        var inputProxy = new ByteBuddy()
//                .redefine(typePool.describe("dgs.inputproxy.codegen.types.SearchInput").resolve(), ClassFileLocator.ForClassLoader.ofSystemLoader())
//                .implement(SparseUpdateProxy.class)
//                .make()
//                .load(InputProxyApplication.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

        var sparseUpdateProxy = new SparseUpdateProxy();

        var inputProxy = new ByteBuddy()
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
