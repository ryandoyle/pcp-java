package net.ryandoyle.pcp;

import net.ryandoyle.pcp.pmapi.PmGenericError;
import net.ryandoyle.pcp.pmapi.PmNoPMNSError;
import net.ryandoyle.pcp.pmapi.PmPMNSError;
import org.junit.Test;

import static net.ryandoyle.pcp.PMAPI.raisePmapiException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PMAPIExceptionTest {

    private static final int PM_ERR_GENERIC = -12345;
    private static final int PM_ERR_PMNS = -12346;
    private static final int PM_ERR_NOPMNS = -12347;

    @Test
    public void pmErrGeneric_shouldRaiseA_PmGenericError() {
        assertThatThrownBy(() -> raisePmapiException(PM_ERR_GENERIC))
                .isInstanceOf(PmGenericError.class)
                .hasMessage("Generic error, already reported above");
    }

    @Test
    public void pmErrPMNS_shouldRaise_PmPMNSError() {
        assertThatThrownBy(() -> raisePmapiException(PM_ERR_PMNS))
                .isInstanceOf(PmPMNSError.class)
                .hasMessage("Problems parsing PMNS definitions");
    }

    @Test
    public void pmErrNoPMNS_shouldRaise_PmNoPMNSError() {
        assertThatThrownBy(() -> raisePmapiException(PM_ERR_NOPMNS))
                .isInstanceOf(PmNoPMNSError.class)
                .hasMessage("PMNS not accessible");
    }
}
