package io.cesarcneto.moneytransfer.config;

import static org.mapstruct.ReportingPolicy.ERROR;

@org.mapstruct.MapperConfig(unmappedTargetPolicy = ERROR, implementationPackage = "<PACKAGE_NAME>.impl", disableSubMappingMethodsGeneration = true)
public interface MapperConfig {

}
