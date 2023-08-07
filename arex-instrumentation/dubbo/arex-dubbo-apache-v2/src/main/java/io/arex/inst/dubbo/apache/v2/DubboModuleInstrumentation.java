package io.arex.inst.dubbo.apache.v2;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleDescription;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.agent.bootstrap.model.ComparableVersion;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * DubboModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class DubboModuleInstrumentation extends ModuleInstrumentation {

    public DubboModuleInstrumentation() {
        super("dubbo-apache-v2", ModuleDescription.builder()
                .name("dubbo-all").supportFrom(ComparableVersion.of("2.7")).supportTo(ComparableVersion.of("2.8")).build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return asList(
                new DubboConsumerInstrumentation(),
                new DubboProviderInstrumentation());
    }
}
