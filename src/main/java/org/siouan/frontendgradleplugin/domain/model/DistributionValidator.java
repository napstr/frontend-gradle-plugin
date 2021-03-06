package org.siouan.frontendgradleplugin.domain.model;

import java.io.IOException;
import javax.annotation.Nonnull;

import org.siouan.frontendgradleplugin.domain.exception.DistributionValidatorException;

/**
 * Interface of a component capable to validate a distribution.
 */
@FunctionalInterface
public interface DistributionValidator {

    /**
     * Validates a downloaded distribution.
     *
     * @param distributionValidatorSettings Settings to validate the distribution.
     * @throws DistributionValidatorException If the distribution is invalid.
     * @throws IOException If an I/O error occurs.
     */
    void execute(@Nonnull DistributionValidatorSettings distributionValidatorSettings)
        throws DistributionValidatorException, IOException;
}
